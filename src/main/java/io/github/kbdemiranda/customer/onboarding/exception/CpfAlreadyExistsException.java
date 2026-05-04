package io.github.kbdemiranda.customer.onboarding.exception;

public class CpfAlreadyExistsException extends RuntimeException {

    public CpfAlreadyExistsException(String cpf) {
        super("CPF already exists: " + cpf);
    }
}
