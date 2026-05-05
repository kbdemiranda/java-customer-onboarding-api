package io.github.kbdemiranda.customer.onboarding.exception;

public class ExternalProviderException extends RuntimeException {

    public ExternalProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExternalProviderException(String message) {
        super(message);
    }
}
