package com.thomasvitale.mousike.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class AiConfig {

    @Bean
    ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

}
