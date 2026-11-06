package com.thomasvitale.mousike.kontakt;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import com.thomasvitale.mousike.kontakt.catalog.InstrumentDetails;
import com.thomasvitale.mousike.kontakt.catalog.InstrumentMatch;
import com.thomasvitale.mousike.kontakt.catalog.LibraryInfo;
import com.thomasvitale.mousike.kontakt.catalog.SearchResult;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Starts the server on the fixture database and talks to it as an MCP client and as a REST client would.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KontaktMcpServerApplicationTests {

    @TempDir
    static Path directory;

    @LocalServerPort
    int port;

    @BeforeAll
    static void createFixture() throws Exception {
        KontaktFixture.create(directory);
    }

    @DynamicPropertySource
    static void kontaktDatabase(DynamicPropertyRegistry registry) {
        registry.add("kontakt.database", () -> directory.resolve("komplete.db3").toString());
    }

    @Test
    void exposesKontaktOverMcp() {
        try (McpSyncClient client = McpClient.sync(HttpClientStreamableHttpTransport.builder("http://localhost:" + port).build()).build()) {
            McpSchema.InitializeResult initialized = client.initialize();
            assertThat(initialized.serverInfo().name()).isEqualTo("kontakt-mcp-server");
            assertThat(initialized.instructions()).contains("Kontakt");

            assertThat(client.listTools().tools()).extracting(McpSchema.Tool::name)
                    .containsExactlyInAnyOrder("list_libraries", "search_instruments", "get_instrument", "list_sound_types", "list_characters");

            McpSchema.CallToolResult result = client.callTool(McpSchema.CallToolRequest.builder("search_instruments").arguments(Map.of("query", "cello", "availableOnly", false)).build());
            assertThat(result.isError()).isNotEqualTo(Boolean.TRUE);
            assertThat(result.content()).first().asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(McpSchema.TextContent.class))
                    .extracting(McpSchema.TextContent::text).asString().contains("AS Tina Guo Legato").contains("1 matching preset.");

            McpSchema.CallToolResult nothing = client.callTool(McpSchema.CallToolRequest.builder("search_instruments").arguments(Map.of("query", "bagpipes")).build());
            assertThat(nothing.content()).first().asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(McpSchema.TextContent.class))
                    .extracting(McpSchema.TextContent::text).asString().contains("No available Kontakt preset matches 'bagpipes'");

            assertThat(client.listResources().resources()).extracting(McpSchema.Resource::uri).contains("kontakt://summary");
            McpSchema.ReadResourceResult summary = client.readResource(McpSchema.ReadResourceRequest.builder("kontakt://summary").build());
            assertThat(summary.contents()).first().asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(McpSchema.TextResourceContents.class))
                    .extracting(McpSchema.TextResourceContents::text).asString().startsWith("6 presets from 4 libraries (1 available now)");

            McpSchema.CallToolResult details = client.callTool(McpSchema.CallToolRequest.builder("get_instrument").arguments(Map.of("id", 1)).build());
            assertThat(details.content()).first().asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(McpSchema.TextContent.class))
                    .extracting(McpSchema.TextContent::text).asString().contains("Low strings, long notes").contains("Albion ONE Manual.pdf");

            McpSchema.CallToolResult unknown = client.callTool(McpSchema.CallToolRequest.builder("get_instrument").arguments(Map.of("id", 99)).build());
            assertThat(unknown.isError()).isTrue();
        }
    }

    @Test
    void exposesKontaktOverRest() {
        RestClient rest = RestClient.create("http://localhost:" + port + "/api");

        SearchResult strings = rest.get().uri("/instruments?q={q}&availableOnly=false", "strings").retrieve().body(SearchResult.class);
        assertThat(strings.matches()).extracting(InstrumentMatch::name, InstrumentMatch::available).containsExactly(
                org.assertj.core.groups.Tuple.tuple("Strings Low Long", true), org.assertj.core.groups.Tuple.tuple("Strings High Long", true),
                org.assertj.core.groups.Tuple.tuple("AS Tina Guo Legato", false));
        assertThat(strings.note()).contains("1 of them sit on a drive that is not mounted");

        SearchResult loadable = rest.get().uri("/instruments?q={q}", "strings").retrieve().body(SearchResult.class);
        assertThat(loadable.matches()).extracting(InstrumentMatch::name).containsExactly("Strings Low Long", "Strings High Long");
        assertThat(loadable.note()).isEqualTo("2 matching presets, best first.");
        SearchResult cello = rest.get().uri("/instruments?q={q}", "cello").retrieve().body(SearchResult.class);
        assertThat(cello.note()).startsWith("No available Kontakt preset matches 'cello'");

        SearchResult tools = rest.get().uri("/instruments?q={q}&category=tools&availableOnly=false", "brass").retrieve().body(SearchResult.class);
        assertThat(tools.matches()).extracting(InstrumentMatch::name, InstrumentMatch::format)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("Brass Swell Phrase", "Tool preset"));
        assertThat(rest.get().uri("/instruments?q={q}&category=drums", "brass").<HttpStatusCode>exchange((request, response) -> response.getStatusCode()))
                .isEqualTo(HttpStatusCode.valueOf(400));
        SearchResult cellos = rest.get().uri("/instruments?q={q}&availableOnly=false", "cello").retrieve().body(SearchResult.class);
        assertThat(cellos.matches()).singleElement().extracting(InstrumentMatch::soundTypes)
                .isEqualTo(List.of("Bowed Strings, Cello", "Bowed Strings, Ensemble"));

        List<LibraryInfo> libraries = rest.get().uri("/libraries").retrieve().body(new ParameterizedTypeReference<>() {});
        assertThat(libraries).filteredOn(LibraryInfo::available).extracting(LibraryInfo::product).containsExactly("Albion ONE");

        InstrumentDetails details = rest.get().uri("/instruments/1").retrieve().body(InstrumentDetails.class);
        assertThat(details.preview()).isNotNull();
        assertThat(details.preview().url().getPath()).isEqualTo("/api/instruments/1/preview");
        assertThat(rest.get().uri("/instruments/2").retrieve().body(InstrumentDetails.class).preview()).isNull();
        var preview = rest.get().uri("/instruments/1/preview").retrieve().toEntity(String.class);
        assertThat(preview.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("audio/ogg"));
        assertThat(preview.getBody()).startsWith("OggS");
        assertThat(rest.get().uri("/instruments/2/preview").<HttpStatusCode>exchange((request, response) -> response.getStatusCode()))
                .isEqualTo(HttpStatusCode.valueOf(404));

        assertThat(details.library().manuals()).singleElement().satisfies(manual -> {
            assertThat(manual.name()).isEqualTo("Albion ONE Manual.pdf");
            assertThat(manual.url().getPath()).isEqualTo("/api/libraries/Albion ONE/manuals/Albion ONE Manual.pdf");
        });

        assertThat(rest.get().uri("/libraries/{product}/instruments", "Albion ONE").retrieve().body(new ParameterizedTypeReference<List<InstrumentMatch>>() {}))
                .extracting(InstrumentMatch::name).containsExactly("Strings High Long", "Strings Low Long");

        var manual = rest.get().uri("/libraries/{product}/manuals/{name}", "Albion ONE", "Albion ONE Manual.pdf").retrieve().toEntity(String.class);
        assertThat(manual.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(manual.getBody()).startsWith("%PDF");

        // only the library's listed manuals are served
        assertThat(rest.get().uri("/libraries/{product}/manuals/{name}", "Albion ONE", "komplete.db3").<HttpStatusCode>exchange((request, response) -> response.getStatusCode()))
                .isEqualTo(HttpStatusCode.valueOf(404));
        assertThat(rest.get().uri("/libraries/{product}/manuals/{name}", "Albion ONE", "../../komplete.db3").<HttpStatusCode>exchange((request, response) -> response.getStatusCode()).is4xxClientError())
                .isTrue();
        assertThat(rest.get().uri("/instruments/99").<HttpStatusCode>exchange((request, response) -> response.getStatusCode()))
                .isEqualTo(HttpStatusCode.valueOf(404));
    }

}
