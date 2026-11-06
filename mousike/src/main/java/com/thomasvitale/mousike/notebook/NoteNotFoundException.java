package com.thomasvitale.mousike.notebook;

import java.util.UUID;

public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(UUID id) {
        super("Note %s does not exist".formatted(id));
    }

}
