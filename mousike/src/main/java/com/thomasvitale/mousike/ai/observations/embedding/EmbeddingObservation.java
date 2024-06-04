package com.thomasvitale.mousike.ai.observations.embedding;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.docs.ObservationDocumentation;

/**
 * Observation created around an embedding model execution.
 */
public enum EmbeddingObservation implements ObservationDocumentation {

    EMBEDDING_OBSERVATION {
        @Override
        public String getName() {
            return "springai.embedding";
        }

        @Override
        public String getContextualName() {
            return "springai embedding";
        }

        @Override
        public KeyName[] getLowCardinalityKeyNames() {
            return EmbeddingLowCardinalityTags.values();
        }

        @Override
        public KeyName[] getHighCardinalityKeyNames() {
            return EmbeddingHighCardinalityTags.values();
        }

        @Override
        public String getPrefix() {
            return "springai";
        }
    };

    enum EmbeddingLowCardinalityTags implements KeyName {

        /**
         * Name of the chat model.
         */
        MODEL_NAME {
            @Override
            public String asString() {
                return "springai.embedding.model.name";
            }
        },

        /**
         * Name of the model provider.
         */
        MODEL_PROVIDER {
            @Override
            public String asString() {
                return "springai.embedding.model.provider";
            }
        },

    }

    enum EmbeddingHighCardinalityTags implements KeyName {

        /**
         * Tokens used for the input prompt.
         */
        USAGE_TOKENS_INPUT {
            @Override
            public String asString() {
                return "springai.embedding.usage.tokens.input";
            }
        },

        /**
         * Tokens used for the model output.
         */
        USAGE_TOKENS_OUTPUT {
            @Override
            public String asString() {
                return "springai.embedding.usage.tokens.output";
            }
        },

        /**
         * Total usage of tokens.
         */
        USAGE_TOKENS_TOTAL {
            @Override
            public String asString() {
                return "springai.embedding.usage.tokens.total";
            }
        }

    }

}
