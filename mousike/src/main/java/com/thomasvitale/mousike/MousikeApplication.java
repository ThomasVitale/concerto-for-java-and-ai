package com.thomasvitale.mousike;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.aura.Aura;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@Modulithic(sharedModules = {"config", "ui"})
@Push
@StyleSheet(Aura.STYLESHEET)
@StyleSheet("styles.css")
public class MousikeApplication implements AppShellConfigurator {

	static void main(String[] args) {
		SpringApplication.run(MousikeApplication.class, args);
	}

	@Override
	public void configurePage(AppShellSettings settings) {
		settings.addFavIcon("icon", "icons/icon.png", "192x192");
	}

}
