package com.thomasvitale.mousike.ai.clients;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Common configuration properties for the OpenAI clients.
 */
@ConfigurationProperties(HttpClientProperties.CONFIG_PREFIX)
public class HttpClientProperties {

    public static final String CONFIG_PREFIX = "mousike.spring.ai.client";

    /**
     * Maximum time to wait for a connection.
     */
    private Duration connectTimeout = Duration.ofSeconds(10);

    /**
     * Maximum time to wait for a response.
     */
    private Duration readTimeout = Duration.ofSeconds(60);

    /**
     * SSL certificate bundle to use to establish a secure connection.
     */
    private String sslBundle;

    /**
     * Whether to log requests.
     */
    private boolean logRequests = false;

    /**
     * Whether to log responses.
     */
    private boolean logResponses = false;

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getSslBundle() {
        return sslBundle;
    }

    public void setSslBundle(String sslBundle) {
        this.sslBundle = sslBundle;
    }

    public boolean isLogRequests() {
        return logRequests;
    }

    public void setLogRequests(boolean logRequests) {
        this.logRequests = logRequests;
    }

    public boolean isLogResponses() {
        return logResponses;
    }

    public void setLogResponses(boolean logResponses) {
        this.logResponses = logResponses;
    }

}
