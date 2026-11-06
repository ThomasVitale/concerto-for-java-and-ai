package com.thomasvitale.mousike.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;
import org.springframework.data.relational.core.dialect.Dialect;

@Configuration(proxyBeanMethods = false)
@EnableJdbcAuditing
public class DataConfig {

    /** Every module's converters together, with the dialect's own kept in place. */
    @Bean
    JdbcCustomConversions jdbcCustomConversions(Dialect dialect, List<Converter<?, ?>> converters) {
        return JdbcCustomConversions.of(dialect, converters);
    }

}
