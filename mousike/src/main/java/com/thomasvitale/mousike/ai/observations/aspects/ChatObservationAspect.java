package com.thomasvitale.mousike.ai.observations.aspects;

import com.thomasvitale.mousike.ai.observations.chat.ChatObservationContext;
import com.thomasvitale.mousike.ai.observations.chat.DefaultChatObservationConvention;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.lang.Nullable;

/**
 * Generate observations for {@link ChatModel} calls.
 */
@Aspect
public class ChatObservationAspect {

    private final ObservationRegistry observationRegistry;

    public ChatObservationAspect(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    @Around("execution(org.springframework.ai.chat.model.ChatResponse org.springframework.ai.chat.model.ChatModel+.call(org.springframework.ai.chat.prompt.Prompt))")
    @Nullable
    public Object observe(ProceedingJoinPoint proceedingJoinPoint) {
        var methodArguments = proceedingJoinPoint.getArgs();
        var observationConvention = new DefaultChatObservationConvention();
        var observationContext = new ChatObservationContext();
        if (methodArguments[0] instanceof Prompt prompt) {
            observationContext.setPrompt(prompt);
        }

        return Observation.createNotStarted(observationConvention, () -> observationContext, this.observationRegistry).observe(() -> {
            Object response;
            try {
                response = proceedingJoinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }

            if (response instanceof ChatResponse chatResponse) {
                observationContext.setUsage(chatResponse.getMetadata().getUsage());
            }

            return response;
        });
    }

}
