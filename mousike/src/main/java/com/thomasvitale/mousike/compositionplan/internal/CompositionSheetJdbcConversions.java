package com.thomasvitale.mousike.compositionplan.internal;

import java.sql.SQLException;

import com.thomasvitale.mousike.compositionplan.CompositionSheet;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Stores a {@link CompositionSheet} as one {@code jsonb} value. {@code DataConfig} hands these to Spring Data JDBC.
 */
final class CompositionSheetJdbcConversions {

    private CompositionSheetJdbcConversions() {
    }

    @Component
    @WritingConverter
    record Writer(JsonMapper json) implements Converter<CompositionSheet, PGobject> {

        @Override
        public PGobject convert(CompositionSheet sheet) {
            try {
                PGobject value = new PGobject();
                value.setType("jsonb");
                value.setValue(json.writeValueAsString(sheet));
                return value;
            }
            catch (SQLException ex) {
                throw new IllegalStateException("Could not store the composition plan", ex);
            }
        }

    }

    @Component
    @ReadingConverter
    record Reader(JsonMapper json) implements Converter<PGobject, CompositionSheet> {

        @Override
        public CompositionSheet convert(PGobject value) {
            return json.readValue(String.valueOf(value.getValue()), CompositionSheet.class);
        }

    }

}
