package com.thomasvitale.mousike.library;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The app's view of the Kontakt MCP server. Every call tolerates the server being down: the composition planner
 * then plans without preset names instead of failing, and the UI says so.
 */
@Service
public class InstrumentLibrary {

    private static final Logger log = LoggerFactory.getLogger(InstrumentLibrary.class);

    private final List<McpSyncClient> clients;
    private final ObjectProvider<SyncMcpToolCallbackProvider> toolCallbacks;
    private final JsonMapper json;

    InstrumentLibrary(List<McpSyncClient> clients, ObjectProvider<SyncMcpToolCallbackProvider> toolCallbacks, JsonMapper json) {
        this.clients = clients;
        this.toolCallbacks = toolCallbacks;
        this.json = json;
    }

    /** Asks the server for its tools; the first call also performs the MCP handshake. */
    public LibraryStatus status() {
        if (clients.isEmpty()) {
            return LibraryStatus.offline("No Kontakt MCP server is configured");
        }
        McpSyncClient client = clients.getFirst();
        try {
            List<String> tools = client.listTools().tools().stream().map(McpSchema.Tool::name).toList();
            McpSchema.Implementation server = client.getServerInfo();
            String name = server == null ? "instrument library" : server.name() + " " + server.version();
            return new LibraryStatus(true, tools, "%s online with %d tools".formatted(name, tools.size()));
        }
        catch (RuntimeException ex) {
            return LibraryStatus.offline("Instrument library unreachable: " + rootMessage(ex));
        }
    }

    /** The library's tools for a chat client, or none when the server is down. */
    public ToolCallback[] toolCallbacks() {
        SyncMcpToolCallbackProvider provider = toolCallbacks.getIfAvailable();
        // The provider caches its tools: asking the server first is what notices that it went away
        if (provider == null || !status().online()) {
            return new ToolCallback[0];
        }
        try {
            return provider.getToolCallbacks();
        }
        catch (RuntimeException ex) {
            log.warn("Instrument library unreachable, planning without preset names: {}", rootMessage(ex));
            return new ToolCallback[0];
        }
    }

    /**
     * Everything the library knows about a preset, by the id its search returned. Empty when the library is down or has
     * no such preset, for instance when the model made the id up.
     */
    public Optional<LibraryPreset> findById(long id) {
        if (clients.isEmpty()) {
            return Optional.empty();
        }
        try {
            JsonNode details = call(clients.getFirst(), "get_instrument", Map.of("id", id));
            JsonNode instrument = details.path("instrument");
            JsonNode preview = details.path("preview").path("url");
            return Optional.of(new LibraryPreset(id, instrument.path("name").asString(), instrument.path("product").asString(),
                    instrument.path("available").asBoolean(), preview.isMissingNode() || preview.isNull() ? null : URI.create(preview.asString())));
        }
        catch (RuntimeException ex) {
            log.debug("Could not find preset {} in the library: {}", id, rootMessage(ex));
            return Optional.empty();
        }
    }

    private JsonNode call(McpSyncClient client, String tool, Map<String, Object> arguments) {
        McpSchema.CallToolResult result = client.callTool(McpSchema.CallToolRequest.builder(tool).arguments(arguments).build());
        String text = result.content().stream()
                .filter(McpSchema.TextContent.class::isInstance)
                .map(content -> ((McpSchema.TextContent) content).text())
                .findFirst()
                .orElse("{}");
        if (Boolean.TRUE.equals(result.isError())) {
            throw new IllegalStateException(text);
        }
        return json.readTree(text);
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    void logStatusAtStartup() {
        LibraryStatus status = status();
        if (status.online()) {
            log.info("{}: {}", status.detail(), status.tools());
        }
        else {
            log.warn("{} (start it with ./gradlew bootRun in kontakt-mcp-server/; the app works without it)", status.detail());
        }
    }

    private static String rootMessage(Throwable failure) {
        Throwable root = failure;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
    }

}
