-- chord progressions rendered as MIDI, and saved composition plans.
CREATE TABLE chord_progression (
    id              UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    key_signature   VARCHAR(32) NOT NULL,
    tempo_bpm       INT NOT NULL,
    beats_per_chord INT NOT NULL,
    chords          TEXT NOT NULL,
    voicing         TEXT NOT NULL,
    midi            BYTEA NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL
);

CREATE TABLE composition_plan (
    id         UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    scene_id   UUID NOT NULL REFERENCES scene(id) ON DELETE CASCADE,
    model      VARCHAR(128),
    plan       JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_composition_plan_scene_created ON composition_plan(scene_id, created_at DESC);
