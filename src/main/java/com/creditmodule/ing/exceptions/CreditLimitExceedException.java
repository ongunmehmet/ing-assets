package com.creditmodule.ing.exceptions;

public class CreditLimitExceedException extends RuntimeException {
    public CreditLimitExceedException(String message) {
        super(message);
    }
}
