package com.thomasvitale.mousike.ai.observations.chat;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.docs.ObservationDocumentation;

/**
 * Observation created around a chat model execution.
 */
public enum ChatObservation implements ObservationDocumentation {

    CHAT_OBSERVATION {
        @Override
        public String getName() {
            return "springai.chat";
        }

        @Override
        public String getContextualName() {
            return "springai chat";
        }

        @Override
        public KeyName[] getLowCardinalityKeyNames() {
            return ChatLowCardinalityTags.values();
        }

        @Override
        public KeyName[] getHighCardinalityKeyNames() {
            return ChatHighCardinalityTags.values();
        }

        @Override
        public String getPrefix() {
            return "springai";
        }
    };

    enum ChatLowCardinalityTags implements KeyName {

        /**
         * Name of the chat model.
         */
        MODEL_NAME {
            @Override
            public String asString() {
                return "springai.chat.model.name";
            }
        },

        /**
         * Name of the model provider.
         */
        MODEL_PROVIDER {
            @Override
            public String asString() {
                return "springai.chat.model.provider";
            }
        },

    }

    enum ChatHighCardinalityTags implements KeyName {

        /**
         * Temperature of the model.
         */
        MODEL_TEMPERATURE {
            @Override
            public String asString() {
                return "springai.chat.model.temperature";
            }
        },

        /**
         * Reason why the model finished execution.
         */
        FINISH_REASON {
            @Override
            public String asString() {
                return "springai.chat.finishReason";
            }
        },

        /**
         * Full prompt from the execution context window.
         */
        PROMPT {
            @Override
            public String asString() {
                return "springai.chat.prompt";
            }
        },

        /**
         * Tokens used for the input prompt.
         */
        USAGE_TOKENS_INPUT {
            @Override
            public String asString() {
                return "springai.chat.usage.tokens.input";
            }
        },

        /**
         * Tokens used for the model output.
         */
        USAGE_TOKENS_OUTPUT {
            @Override
            public String asString() {
                return "springai.chat.usage.tokens.output";
            }
        },

        /**
         * Total usage of tokens.
         */
        USAGE_TOKENS_TOTAL {
            @Override
            public String asString() {
                return "springai.chat.usage.tokens.total";
            }
        }

    }

}
