-- The composer's notebook: notes classified by the decision model.
CREATE TABLE note (
    id               UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    content          TEXT NOT NULL,
    kind             VARCHAR(32) NOT NULL,
    tagged_by        VARCHAR(128),          -- null when classified by hand
    tagging_duration VARCHAR(32),           -- ISO-8601, e.g. PT0.182S
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_note_kind ON note(kind);
CREATE INDEX idx_note_created_at ON note(created_at DESC);
