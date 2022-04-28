package org.observertc.observer.configs;

import org.observertc.observer.common.JsonUtils;

public class InvalidConfigurationException extends RuntimeException {
    /**
     * Creates a InvalidConfigurationException with the given message.
     *
     * @param message the message for the exception
     */
    public InvalidConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a new runtime exception with the specified detail message and cause.
     *
     * @param message the message for the runtime exception
     * @param cause   the cause of the runtime exception
     */
    public InvalidConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public InvalidConfigurationException(String message, Object config) {
        super(message + JsonUtils.objectToString(config));
    }

}
