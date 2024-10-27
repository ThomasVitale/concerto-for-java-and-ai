package com.thomasvitale.mousike;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Theme(value = "mousike")
public class MousikeApplication implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(MousikeApplication.class, args);
	}

    @Bean
    ApplicationListener<ApplicationReadyEvent> logbackOtelAppenderInitializer(OpenTelemetry openTelemetry) {
        return _ -> OpenTelemetryAppender.install(openTelemetry);
    }

}
