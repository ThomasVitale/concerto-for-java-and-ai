package com.thomasvitale.mousike.midi;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MidiTools {

    private static final Logger logger = LoggerFactory.getLogger(MidiTools.class);

    private static final String OUTPUT_PORT = "MCP Midi Output";

    private MidiDevice outputDevice;
    private Receiver receiver;
    private boolean connected;

    // CONNECTION

    @PostConstruct
    public void connect() {
        MidiDevice.Info[] devicesInfo = MidiSystem.getMidiDeviceInfo();

        Optional<MidiDevice.Info> outputDeviceInfo = findMidiDeviceInfo(devicesInfo, OUTPUT_PORT);
        if (outputDeviceInfo.isPresent()) {
            try {
                outputDevice = MidiSystem.getMidiDevice(outputDeviceInfo.get());
                if (!outputDevice.isOpen()) {
                    outputDevice.open();
                }
                receiver = outputDevice.getReceiver();
                connected = true;
                logger.info("Connected to MIDI output device: " + outputDevice.getDeviceInfo().getName());
            } catch (Exception e) {
                logger.error("Failed to open MIDI output device: " + e.getMessage());
                connected = false;
            }
        } else {
            logger.warn("MIDI output device not found: " + OUTPUT_PORT);
            connected = false;
        }

    }

    @PreDestroy
    public void disconnect() {
        if (receiver != null) {
            receiver.close();
        }
        if (outputDevice != null && outputDevice.isOpen()) {
            outputDevice.close();
        }
        connected = false;
    }

    public void reconnect() {
        disconnect();
        connect();
    }

    public boolean isConnected() {
        return connected && outputDevice != null && outputDevice.isOpen();
    }

    private Optional<MidiDevice.Info> findMidiDeviceInfo(MidiDevice.Info[] devicesInfo, String name) {
        return Arrays.stream(devicesInfo)
                .filter(info -> info.getName().contains(name))
                .findFirst();
    }

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    void checkConnection() {
        try {
            if (!isConnected()) {
                logger.info("MIDI connection lost, attempting reconnect...");
                reconnect();
                if (isConnected()) {
                    logger.info("MIDI reconnection successful");
                } else {
                    logger.warn("MIDI reconnection failed");
                }
            }
        } catch (Exception e) {
            logger.error("Error during MIDI connection check", e);
        }
    }

    // MESSAGES

    public void send(MidiMessage message) {
        if (!isConnected() || receiver == null) {
            throw new IllegalStateException("MIDI device not connected or receiver is null");
        }
        receiver.send(message, -1);
        logger.info("MIDI message sent: " + message);
    }

    public void send(int status, int data1, int data2) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(status, data1, data2);
        } catch (InvalidMidiDataException e) {
            logger.error("Failed to create MIDI message: " + e.getMessage());
            return;
        }
        logger.info("Sending MIDI message: status=" + status + ", data1=" + data1 + ", data2=" + data2);
        send(message);
    }

    @Tool(description = "Play a note on the configured virtual instrument via MIDI")
    public String playNote(
            @ToolParam(description = "The MIDI channel") int channel,
            @ToolParam(description = "The MIDI note to play") int note,
            @ToolParam(description = "The MIDI velocity of the note to play") int velocity,
            @ToolParam(description = "Duration in milliseconds for how long the note should play") long durationInMillis
    ) {
        ShortMessage noteOn = new ShortMessage();
        ShortMessage noteOff = new ShortMessage();
        try {
            noteOn.setMessage(ShortMessage.NOTE_ON, channel, note, velocity);
            // Using NOTE_OFF, velocity for NOTE_OFF is often 0 or irrelevant
            noteOff.setMessage(ShortMessage.NOTE_OFF, channel, note, 0);
        } catch (InvalidMidiDataException e) {
            logger.error("Failed to create MIDI message: " + e.getMessage(), e);
            return "Failed to create MIDI message: " + e.getMessage();
        }

        try {
            send(noteOn);
            if (durationInMillis > 0) {
                sleep(durationInMillis);
                send(noteOff);
            }
        } catch (IllegalStateException e) {
            logger.error("Failed to send MIDI message for note " + note + ": " + e.getMessage(), e);
            return "Failed to send MIDI message: " + e.getMessage();
        }
        return "Note played: channel=" + channel + ", note=" + note + ", velocity=" + velocity + ", duration=" + durationInMillis + "ms";
    }

    @Tool(description = "Play a chord on the configured virtual instrument via MIDI")
    public String playChord(
            @ToolParam(description = "The MIDI channel") int channel,
            @ToolParam(description = "The MIDI notes to play (comma-separated)") String notes,
            @ToolParam(description = "The MIDI velocity of the notes to play") int velocity,
            @ToolParam(description = "Duration in milliseconds for how long the chord should play") long durationInMillis
    ) {
        String[] noteStrings = notes.split(",");
        int[] midiNotes = new int[noteStrings.length];
        for (int i = 0; i < noteStrings.length; i++) {
            try {
                midiNotes[i] = Integer.parseInt(noteStrings[i].trim());
            } catch (NumberFormatException e) {
                logger.error("Invalid note format: " + noteStrings[i], e);
                return "Invalid note format: " + noteStrings[i];
            }
        }

        // Send all NOTE_ON messages first
        for (int note : midiNotes) {
            ShortMessage noteOn = new ShortMessage();
            try {
                noteOn.setMessage(ShortMessage.NOTE_ON, channel, note, velocity);
                send(noteOn);
            } catch (InvalidMidiDataException e) {
                logger.error("Failed to create MIDI NOTE_ON message for note " + note + ": " + e.getMessage(), e);
                // Optionally, decide if we should stop or continue
            } catch (IllegalStateException e) {
                logger.error("Failed to send MIDI NOTE_ON message for note " + note + ": " + e.getMessage(), e);
                return "Failed to send MIDI message: " + e.getMessage();
            }
        }

        // Wait for the duration
        if (durationInMillis > 0) {
            sleep(durationInMillis);
        }

        // Send all NOTE_OFF messages
        for (int note : midiNotes) {
            ShortMessage noteOff = new ShortMessage();
            try {
                noteOff.setMessage(ShortMessage.NOTE_OFF, channel, note, 0); // Velocity for NOTE_OFF is typically 0
                send(noteOff);
            } catch (InvalidMidiDataException e) {
                logger.error("Failed to create MIDI NOTE_OFF message for note " + note + ": " + e.getMessage(), e);
            } catch (IllegalStateException e) {
                logger.error("Failed to send MIDI NOTE_OFF message for note " + note + ": " + e.getMessage(), e);
                // Potentially return an error, or log and continue
            }
        }
        return "Chord played: channel=" + channel + ", notes=" + notes + ", velocity=" + velocity + ", duration=" + durationInMillis + "ms";
    }

    @Tool(description = "Play a sequence of four chords on the configured virtual instrument via MIDI")
    public String playChordProgression(
            @ToolParam(description = "The MIDI channel") int channel,
            @ToolParam(description = "Comma-separated MIDI notes for the first chord") String chord1Notes,
            @ToolParam(description = "Comma-separated MIDI notes for the second chord") String chord2Notes,
            @ToolParam(description = "Comma-separated MIDI notes for the third chord") String chord3Notes,
            @ToolParam(description = "Comma-separated MIDI notes for the fourth chord") String chord4Notes,
            @ToolParam(description = "The MIDI velocity for all chords") int velocity,
            @ToolParam(description = "Duration in milliseconds for how long each chord should play (at least 2 seconds)") long durationInMillis,
            @ToolParam(description = "Delay in milliseconds between chords") long delayBetweenChordsInMillis
    ) {
        String result1 = playChord(channel, chord1Notes, velocity, durationInMillis);
        if (result1.startsWith("Failed") || result1.startsWith("Invalid")) return "Failed at chord 1: " + result1;
        //if (delayBetweenChordsInMillis > 0) sleep(delayBetweenChordsInMillis);

        String result2 = playChord(channel, chord2Notes, velocity, durationInMillis);
        if (result2.startsWith("Failed") || result2.startsWith("Invalid")) return "Failed at chord 2: " + result2;
        //if (delayBetweenChordsInMillis > 0) sleep(delayBetweenChordsInMillis);

        String result3 = playChord(channel, chord3Notes, velocity, durationInMillis);
        if (result3.startsWith("Failed") || result3.startsWith("Invalid")) return "Failed at chord 3: " + result3;
        //if (delayBetweenChordsInMillis > 0) sleep(delayBetweenChordsInMillis);

        String result4 = playChord(channel, chord4Notes, velocity, durationInMillis);
        if (result4.startsWith("Failed") || result4.startsWith("Invalid")) return "Failed at chord 4: " + result4;

        return "Chord progression played successfully. Chord 1: " + chord1Notes + ", Chord 2: " + chord2Notes + ", Chord 3: " + chord3Notes + ", Chord 4: " + chord4Notes + " each for " + durationInMillis + "ms with " + delayBetweenChordsInMillis + "ms delay.";
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.warn("Chord progression sleep interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

}
