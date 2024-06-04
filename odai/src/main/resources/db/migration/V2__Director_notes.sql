CREATE TABLE DIRECTOR_NOTE (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    movie VARCHAR(255) NOT NULL,
    scene_description TEXT
);

CREATE INDEX idx_movie ON DIRECTOR_NOTE(movie);

CREATE TABLE MARKER (
    director_note UUID,
    director_note_key VARCHAR(255),
    time TEXT NOT NULL,
    note TEXT,
    PRIMARY KEY (director_note, director_note_key),
    FOREIGN KEY (director_note) REFERENCES DIRECTOR_NOTE(id) ON DELETE CASCADE
);
