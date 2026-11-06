package com.thomasvitale.mousike.library;

import java.net.URI;
import java.util.List;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The preset lookup against answers shaped like the library's search_instruments and get_instrument results.
 */
class InstrumentLibraryTests {

    static final String DETAILS = """
            {"instrument":{"id":3039,"name":"Nucleus - 01 Solo Cello - Legato","product":"Nucleus","available":true},
             "preview":{"path":"/Users/Shared/Nucleus/.previews/x.nki.ogg","url":"http://localhost:4400/api/instruments/3039/preview"}}""";

    McpSyncClient client = mock(McpSyncClient.class);

    @SuppressWarnings("unchecked")
    InstrumentLibrary instrumentLibrary = new InstrumentLibrary(List.of(client), mock(ObjectProvider.class), JsonMapper.builder().build());

    @Test
    void findsThePresetAndItsPreviewById() {
        answer("get_instrument", DETAILS);

        assertThat(instrumentLibrary.findById(3039)).contains(new LibraryPreset(3039, "Nucleus - 01 Solo Cello - Legato", "Nucleus", true,
                URI.create("http://localhost:4400/api/instruments/3039/preview")));
    }

    @Test
    void aPresetWithoutPreviewHasNone() {
        answer("get_instrument", """
                {"instrument":{"id":3038,"name":"Nucleus - 02 Solo Cello - Sustained","product":"Nucleus","available":true},"preview":null}""");

        assertThat(instrumentLibrary.findById(3038)).get().extracting(LibraryPreset::preview).isNull();
    }

    @Test
    void anIdTheLibraryDoesNotHaveFindsNothing() {
        when(client.callTool(argThat(request -> request != null && request.name().equals("get_instrument"))))
                .thenReturn(McpSchema.CallToolResult.builder().addTextContent("No Kontakt preset has id 9.").isError(true).build());

        assertThat(instrumentLibrary.findById(9)).isEmpty();
    }

    @Test
    void aFailingLibraryFindsNothing() {
        when(client.callTool(argThat(request -> request != null && request.name().equals("get_instrument"))))
                .thenThrow(new IllegalStateException("Connection refused"));

        assertThat(instrumentLibrary.findById(3039)).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void aServerThatWentAwayHasNoTools() {
        ObjectProvider<SyncMcpToolCallbackProvider> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(mock(SyncMcpToolCallbackProvider.class));
        when(client.listTools()).thenThrow(new IllegalStateException("Connection refused"));

        assertThat(new InstrumentLibrary(List.of(client), provider, JsonMapper.builder().build()).toolCallbacks()).isEmpty();
    }

    private void answer(String tool, String json) {
        when(client.callTool(argThat(request -> request != null && request.name().equals(tool))))
                .thenReturn(McpSchema.CallToolResult.builder().addTextContent(json).isError(false).build());
    }

}
