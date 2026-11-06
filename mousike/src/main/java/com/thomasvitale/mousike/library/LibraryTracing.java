package com.thomasvitale.mousike.library;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import io.modelcontextprotocol.client.transport.customizer.McpSyncHttpClientRequestCustomizer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Carries the current trace into every MCP request, so the library server's spans nest under the app's tool call in
 * one trace. The MCP transport uses its own {@code java.net.http.HttpClient}, which nothing instruments by default.
 */
@Configuration(proxyBeanMethods = false)
class LibraryTracing {

    @Bean
    McpSyncHttpClientRequestCustomizer mcpTracePropagation(Tracer tracer, Propagator propagator) {
        return (request, method, uri, body, context) -> {
            Span current = tracer.currentSpan();
            if (current != null) {
                propagator.inject(current.context(), request, (builder, key, value) -> builder.header(key, value));
            }
        };
    }

}
