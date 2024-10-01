package com.thomasvitale.mousike.domain.instrument;

import java.util.List;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

@Configuration(proxyBeanMethods = false)
public class InstrumentTools {

    @Bean
    @Description("Retrieve the list of instruments available to use in the composition")
    Function<InstrumentQuery, InstrumentResult> getAvailableInstruments(InstrumentService instrumentService) {
        return _ -> new InstrumentResult(instrumentService.getInstruments());
    }

    public record InstrumentQuery(String purpose){}
    public record InstrumentResult(List<String> instruments){}

}
