package com.thomasvitale.mousike.kontakt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.nio.file.Path;

@ConfigurationProperties(prefix = "kontakt")
public record KontaktProperties(

        /*
         * The Kontakt 8 {@code komplete.db3} SQLite file (opened read-only).
         */
        Path database,

        /*
         * Where clients reach this server, used to build the links it hands out.
         */
        URI url

) {}
