package com.thomasvitale.mousike.scenes;

import com.thomasvitale.mousike.scenes.internal.RecordingTranscriber;
import com.thomasvitale.mousike.scenes.internal.SceneExtractor;
import com.thomasvitale.mousike.scenes.internal.SceneRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SceneService {

    private final SceneRepository scenes;
    private final RecordingTranscriber transcriber;
    private final SceneExtractor extractor;

    SceneService(SceneRepository scenes, RecordingTranscriber transcriber, SceneExtractor extractor) {
        this.scenes = scenes;
        this.transcriber = transcriber;
        this.extractor = extractor;
    }

    public Transcript transcribe(byte[] audio, String filename) {
        return transcriber.transcribe(audio, filename);
    }

    public SceneDraft draft(String transcript, @Nullable Medium medium) {
        return extractor.extract(transcript, medium);
    }

    @Transactional
    public Scene save(Scene scene) {
        return scenes.save(scene);
    }

    public List<Scene> list() {
        return scenes.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Scene> findById(UUID id) {
        return scenes.findById(id);
    }

    public long count() {
        return scenes.count();
    }

    @Transactional
    public void delete(UUID id) {
        scenes.deleteById(id);
    }

}
