package com.thomasvitale.mousike.agent;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.stereotype.Component;

/**
 * A small model sometimes calls a tool by a name it made up, {@code searchInstruments} for {@code search_instruments},
 * and that would fail the whole request. Spring AI asks this resolver about any name the request does not offer
 * ({@code spring.ai.tools.resolution.fallback.enabled}); the answer tells the model to pick a real tool and carry on.
 */
@Component
class UnknownToolResolver implements ToolCallbackResolver {

    @Override
    public ToolCallback resolve(String toolName) {
        ToolDefinition definition = ToolDefinition.builder()
                .name(toolName)
                .description("Not a tool.")
                .inputSchema("{\"type\": \"object\", \"properties\": {}}")
                .build();
        String answer = "There is no tool named '%s'. Call only the tools you were given, with their exact names.".formatted(toolName);
        return new ToolCallback() {

            @Override
            public ToolDefinition getToolDefinition() {
                return definition;
            }

            @Override
            public String call(String toolInput) {
                return answer;
            }

            @Override
            public String call(String toolInput, @Nullable ToolContext toolContext) {
                return answer;
            }

        };
    }

}
