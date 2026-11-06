package com.thomasvitale.mousike.chords;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * The MIDI port that sends chord progressions to Logic Pro.
 *
 * @param outputPortName text to look for in the port's name, e.g. an IAC Driver port set up in Audio MIDI Setup
 */
@ConfigurationProperties(prefix = "mousike.midi")
public record MidiProperties(
        @DefaultValue("MCP Midi Output") String outputPortName
) {}
