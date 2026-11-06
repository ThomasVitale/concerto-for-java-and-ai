package com.thomasvitale.mousike.kontakt;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.sqlite.SQLiteDataSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;

/**
 * A tiny Kontakt database with the real schema subset the server reads: the view, the FTS index, and six entries.
 * Albion ONE is on disk, in {@code directory}, with its patch files, one preview and a manual. The other libraries point at a
 * drive that is not mounted.
 */
final class KontaktFixture {

    private KontaktFixture() {}

    static void create(Path directory) throws Exception {
        Path albion = directory.resolve("Albion ONE");
        Files.createDirectories(albion.resolve("Instruments"));
        Files.createDirectories(albion.resolve("Documentation"));
        Files.writeString(albion.resolve("Instruments/Strings Low Long.nki"), "nki");
        Files.writeString(albion.resolve("Instruments/Strings High Long.nki"), "nki");
        Files.createDirectories(albion.resolve("Instruments/.previews"));
        Files.writeString(albion.resolve("Instruments/.previews/Strings Low Long.nki.ogg"), "OggS fixture");
        Files.writeString(albion.resolve("Documentation/Albion ONE Manual.pdf"), "%PDF-1.4 fixture");

        String script = new ClassPathResource("kontakt-fixture.sql").getContentAsString(StandardCharsets.UTF_8)
                .replace("{albion}", albion.toString());
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + directory.resolve("komplete.db3"));
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new ByteArrayResource(script.getBytes(StandardCharsets.UTF_8)));
        }
    }

}
