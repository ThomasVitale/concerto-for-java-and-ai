package com.thomasvitale.mousike.ai.clients;

import java.time.Duration;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Options for configuring the HTTP clients used by the models.
 */
public record HttpClientConfig(
        Duration connectTimeout,
        Duration readTimeout,
        @Nullable
        String sslBundle,
        boolean logRequests,
        boolean logResponses
) {

    public HttpClientConfig {
        Assert.notNull(connectTimeout, "connectTimeout must not be null");
        Assert.notNull(readTimeout, "readTimeout must not be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration readTimeout = Duration.ofSeconds(60);
        @Nullable
        private String sslBundle;
        private boolean logRequests = false;
        private boolean logResponses = false;

        private Builder() {}

        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder sslBundle(String sslBundle) {
            this.sslBundle = sslBundle;
            return this;
        }

        public Builder logRequests(boolean logRequests) {
            this.logRequests = logRequests;
            return this;
        }

        public Builder logResponses(boolean logResponses) {
            this.logResponses = logResponses;
            return this;
        }

        public HttpClientConfig build() {
            return new HttpClientConfig(connectTimeout, readTimeout, sslBundle, logRequests, logResponses);
        }
    }

}
