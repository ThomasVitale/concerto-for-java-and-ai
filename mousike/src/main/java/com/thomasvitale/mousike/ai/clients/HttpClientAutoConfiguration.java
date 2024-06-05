package com.thomasvitale.mousike.ai.clients;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(HttpClientProperties.class)
public class HttpClientAutoConfiguration {

    @Bean
    RestClientCustomizer restClientCustomizer(HttpClientProperties httpClientProperties) {
        HttpClientConfig clientConfig = HttpClientConfig.builder()
                .connectTimeout(httpClientProperties.getConnectTimeout())
                .readTimeout(httpClientProperties.getReadTimeout())
                .sslBundle(httpClientProperties.getSslBundle())
                .logRequests(httpClientProperties.isLogRequests())
                .logResponses(httpClientProperties.isLogResponses())
                .build();

        return restClientBuilder -> {
            restClientBuilder
                    .requestFactory(new BufferingClientHttpRequestFactory(
                            ClientHttpRequestFactories.get(ClientHttpRequestFactorySettings.DEFAULTS
                                            .withConnectTimeout(clientConfig.connectTimeout())
                                            .withReadTimeout(clientConfig.readTimeout()))))
                    .requestInterceptors(interceptors -> {
                        if (clientConfig.logRequests() || clientConfig.logResponses()) {
                            interceptors.add(new HttpLoggingInterceptor(clientConfig.logRequests(), clientConfig.logResponses()));
                        }
                    });
        };
    }

}
