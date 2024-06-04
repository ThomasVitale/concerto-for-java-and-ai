package com.thomasvitale.mousike.ai.clients;

import java.net.http.HttpClient;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;

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
            HttpClient httpClient = HttpClient.newBuilder().connectTimeout(clientConfig.connectTimeout()).build();

            var jdkClientHttpRequestFactory = new JdkClientHttpRequestFactory(httpClient);
            jdkClientHttpRequestFactory.setReadTimeout(clientConfig.readTimeout());

            ClientHttpRequestFactory requestFactory;
            if (clientConfig.logRequests()) {
                requestFactory = new BufferingClientHttpRequestFactory(jdkClientHttpRequestFactory);
            } else {
                requestFactory = jdkClientHttpRequestFactory;
            }

            restClientBuilder
                    .requestFactory(requestFactory)
                    .requestInterceptors(interceptors -> {
                        if (clientConfig.logRequests() || clientConfig.logResponses()) {
                            interceptors.add(new HttpLoggingInterceptor(clientConfig.logRequests(), clientConfig.logResponses()));
                        }
                    });
        };
    }

}
