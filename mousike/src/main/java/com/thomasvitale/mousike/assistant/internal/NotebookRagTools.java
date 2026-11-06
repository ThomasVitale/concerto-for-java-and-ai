package com.thomasvitale.mousike.assistant.internal;

import com.thomasvitale.mousike.assistant.internal.NotebookRagAgent.NotebookAnswer;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * RAG as a tool: the assistant delegates questions about the notes to the {@link NotebookRagAgent}, a chat client of its
 * own, and the answer with its citations goes straight back (returnDirect), not through another model call.
 */
@Component
public class NotebookRagTools {

    public static final String TOOL_NAME = "askNotebook";

    private final NotebookRagAgent notebookRagAgent;

    public NotebookRagTools(NotebookRagAgent notebookRagAgent) {
        this.notebookRagAgent = notebookRagAgent;
    }

    @Tool(name = TOOL_NAME, returnDirect = true, description = """
            Ask the composer's own notebook. Use it for anything about their notes and ideas. The composer reads the \
            answer as it is.""")
    public NotebookAnswer askNotebook(
            @ToolParam(description = "One short question in plain words that stands on its own, with whatever earlier messages established, e.g. 'What do my notes say about action scenes?'. Never several questions: the notebook does not see this conversation") String question) {
        return notebookRagAgent.answer(question);
    }

}
