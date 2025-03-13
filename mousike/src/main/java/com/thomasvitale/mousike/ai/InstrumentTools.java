package com.thomasvitale.mousike.ai;

import java.util.List;

import com.thomasvitale.mousike.domain.instrument.InstrumentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class InstrumentTools {

    private static final Logger logger = LoggerFactory.getLogger(InstrumentTools.class);

    private final InstrumentService instrumentService;

    public InstrumentTools(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @Tool(description = "Retrieve the list of instruments available to use in the composition")
    List<String> getAvailableInstruments() {
        logger.info("Retrieve the list of instruments available");
        return instrumentService.getInstruments();
    }

}
