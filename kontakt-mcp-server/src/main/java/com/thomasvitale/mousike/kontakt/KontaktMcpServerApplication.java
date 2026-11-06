package com.thomasvitale.mousike.kontakt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KontaktMcpServerApplication {

    static void main(String[] args) {
        SpringApplication.run(KontaktMcpServerApplication.class, args);
    }

}
