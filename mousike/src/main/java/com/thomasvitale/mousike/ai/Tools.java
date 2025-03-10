package com.thomasvitale.mousike.ai;

import java.util.List;
import java.util.function.Function;

import com.thomasvitale.mousike.domain.directornote.DirectorNote;
import com.thomasvitale.mousike.domain.directornote.DirectorNoteRepository;
import com.thomasvitale.mousike.domain.instrument.InstrumentService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

@Configuration(proxyBeanMethods = false)
public class Tools {

    @Bean
    @Description("Retrieve the list of instruments available to use in the composition")
    Function<InstrumentQuery, InstrumentResult> getAvailableInstruments(InstrumentService instrumentService) {
        return _ -> new InstrumentResult(instrumentService.getInstruments());
    }

    public record InstrumentQuery(String purpose){}
    public record InstrumentResult(List<String> instruments){}

    @Bean
    @Description("Save DirectorNote structured data objects in the database")
    Function<DirectorNote, DirectorNote> saveDirectorNote(DirectorNoteRepository directorNoteRepository) {
        return directorNoteRepository::save;
    }

}
