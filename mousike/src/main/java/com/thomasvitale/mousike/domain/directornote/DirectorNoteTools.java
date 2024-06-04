package com.thomasvitale.mousike.domain.directornote;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration(proxyBeanMethods = false)
class DirectorNoteTools {

    @Bean
    @Description("Save DirectorNote structured data objects in the database")
    Function<DirectorNote, DirectorNote> saveDirectorNote(DirectorNoteRepository directorNoteRepository) {
        return directorNoteRepository::save;
    }

}
