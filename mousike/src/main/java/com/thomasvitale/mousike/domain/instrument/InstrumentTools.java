package com.thomasvitale.mousike.domain.instrument;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

@Configuration(proxyBeanMethods = false)
public class InstrumentTools {

    @Bean
    @Description("Verify if the given musical instrument is available")
    Function<InstrumentQuery, InstrumentResult> isInstrumentAvailable(InstrumentService instrumentService) {
        return instrument -> new InstrumentResult(instrumentService.getInstruments().stream().anyMatch(
                i -> i.equals(instrument.name)
        ));
    }

    public record InstrumentQuery(String name){}
    public record InstrumentResult(boolean available){}

}
