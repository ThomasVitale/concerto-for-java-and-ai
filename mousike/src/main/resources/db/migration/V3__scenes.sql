-- Scenes: what the director asked for at each time marker.
CREATE TABLE scene (
    id          UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    production  VARCHAR(255),          -- the film, series, game or show; null when unknown
    title       VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    medium      VARCHAR(16) NOT NULL,
    genre       VARCHAR(64),
    transcript  TEXT,
    created_at  TIMESTAMPTZ NOT NULL
);

CREATE TABLE scene_marker (
    scene       UUID NOT NULL REFERENCES scene(id) ON DELETE CASCADE,
    scene_key   INT NOT NULL,
    time_marker VARCHAR(16) NOT NULL,
    comment     TEXT NOT NULL,
    PRIMARY KEY (scene, scene_key)
);
