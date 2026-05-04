package io.github.kbdemiranda.customer.onboarding.exception;

public class ZipCodeNotFoundException extends RuntimeException {

    public ZipCodeNotFoundException() {
        super("Zip code not found");
    }
}
