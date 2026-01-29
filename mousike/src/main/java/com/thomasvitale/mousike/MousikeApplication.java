package com.thomasvitale.mousike;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.lumo.Lumo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet(Lumo.UTILITY_STYLESHEET)
@StyleSheet("styles.css")
@PWA(name = "Mousike", shortName = "Mousike")
public class MousikeApplication implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(MousikeApplication.class, args);
	}

}
