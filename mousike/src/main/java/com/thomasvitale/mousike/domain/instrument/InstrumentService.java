package com.thomasvitale.mousike.domain.instrument;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class InstrumentService {

    private static final List<String> instruments = List.of(
            "piano",
            "strings",
            "brass",
            "cello",
            "percussions",
            "drones",
            "harp"
    );

    public List<String> getInstruments() {
        return instruments;
    }

}
