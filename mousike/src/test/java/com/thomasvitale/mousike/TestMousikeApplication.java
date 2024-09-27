package com.thomasvitale.mousike;

import org.springframework.boot.SpringApplication;

public class TestMousikeApplication {

	public static void main(String[] args) {
		SpringApplication.from(MousikeApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
