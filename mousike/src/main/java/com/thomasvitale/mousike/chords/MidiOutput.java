package com.thomasvitale.mousike.chords;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Optional;

/**
 * Optional second output: plays a progression on a virtual MIDI port (macOS IAC Driver) so Logic Pro records or
 * monitors it. Reconnects on a schedule because the port may appear after the app started.
 */
@Component
@EnableConfigurationProperties(MidiProperties.class)
public class MidiOutput {

    private static final Logger log = LoggerFactory.getLogger(MidiOutput.class);

    private final String portName;
    private volatile @Nullable MidiDevice device;

    public MidiOutput(MidiProperties properties) {
        this.portName = properties.outputPortName();
    }

    public boolean isAvailable() {
        return device != null;
    }

    public String portName() {
        return portName;
    }

    /** Plays the MIDI file on the port, blocking until it ends. */
    public void play(byte[] midi) {
        MidiDevice output = device;
        if (output == null) {
            throw new IllegalStateException("No MIDI port named '%s' is online".formatted(portName));
        }
        try {
            Sequencer sequencer = MidiSystem.getSequencer(false);
            try (sequencer; output) {
                sequencer.open();
                if (!output.isOpen()) {
                    output.open();
                }
                sequencer.getTransmitter().setReceiver(output.getReceiver());
                sequencer.setSequence(new ByteArrayInputStream(midi));
                sequencer.start();
                while (sequencer.isRunning()) {
                    Thread.sleep(50);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            throw new IllegalStateException("Could not play on MIDI port '%s'".formatted(portName), ex);
        }
    }

    @Scheduled(initialDelay = 2_000, fixedDelay = 30_000)
    void reconnect() {
        if (device != null && device.isOpen()) {
            return;
        }
        Optional<MidiDevice> found = Arrays.stream(MidiSystem.getMidiDeviceInfo())
                .filter(info -> info.getName().contains(portName) || info.getDescription().contains(portName))
                .map(info -> {
                    try {
                        return MidiSystem.getMidiDevice(info);
                    }
                    catch (Exception ex) {
                        return null;
                    }
                })
                .filter(candidate -> candidate != null && candidate.getMaxReceivers() != 0)
                .findFirst();
        if (found.isPresent() && device == null) {
            log.info("MIDI output port '{}' is online; progressions can be sent to Logic Pro", portName);
        }
        device = found.orElse(null);
    }

}
