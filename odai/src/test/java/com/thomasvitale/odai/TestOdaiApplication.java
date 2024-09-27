package com.thomasvitale.odai;

import org.springframework.boot.SpringApplication;

public class TestOdaiApplication {

    public static void main(String[] args) {
        SpringApplication.from(OdaiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
