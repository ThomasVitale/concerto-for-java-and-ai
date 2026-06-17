package com.thomasvitale.mousike.midi;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

@Service
public class MidiService {

    private final ChatClient chatClient;
    private final MidiTools midiTools;

    public MidiService(ChatClient.Builder chatClientBuilder, MidiTools midiTools) {
        this.chatClient = chatClientBuilder.build();
        this.midiTools = midiTools;
    }

    @Nullable
    public String answer(String question) {
        return chatClient.prompt()
                .system("""
                    You are an expert at MIDI music that can use tools to perform the operations
                    requested by a musician and composer. When asked to play some MIDI music,
                    use the appropriate tool to perform the operation. If asked to play a chord
                    progression, before calling any tool, first determine the notes that should
                    be played for each chord. Then, each chord should be played in sequence
                    like a pianist with both hands, leveraging inversion techniques to create
                    a rich and engaging sound.

                    If not explicitly provided, use these defaults:
                    - Velocity (loudness, 0-127): 90
                    - Duration (how long each chord plays): 2 seconds
                    - Delay between chords: 0.5 seconds.

                    Never ask for additional input from the user.
                    """)
                .user(question)
                .tools(midiTools)
                .options(ChatOptions.builder()
                        .model("mistral-large-latest"))
                .call()
                .content();
    }

}
