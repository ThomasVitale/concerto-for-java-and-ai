package com.thomasvitale.mousike;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Theme(value = "mousike")
@PWA(name = "Mousike", shortName = "Mousike", iconPath = "icons/icon.png")
public class MousikeApplication implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(MousikeApplication.class, args);
	}

}
