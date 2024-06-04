package com.thomasvitale.mousike.ai.observations.aspects;

import com.thomasvitale.mousike.ai.observations.embedding.DefaultEmbeddingObservationConvention;

import com.thomasvitale.mousike.ai.observations.embedding.EmbeddingObservationContext;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.lang.Nullable;

/**
 * Generate observations for {@link EmbeddingModel} calls.
 */
@Aspect
public class EmbeddingObservationAspect {

    private final ObservationRegistry observationRegistry;

    public EmbeddingObservationAspect(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    @Around("execution(org.springframework.ai.embedding.EmbeddingResponse org.springframework.ai.embedding.EmbeddingModel+.call(org.springframework.ai.embedding.EmbeddingRequest))")
    @Nullable
    public Object observe(ProceedingJoinPoint proceedingJoinPoint) {
        var observationConvention = new DefaultEmbeddingObservationConvention();
        var observationContext = new EmbeddingObservationContext();

        return Observation.createNotStarted(observationConvention, () -> observationContext, this.observationRegistry).observe(() -> {
            Object response;
            try {
                response = proceedingJoinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return response;
        });
    }

}
