package com.creditmodule.ing.annotation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class InstallmentValidator implements ConstraintValidator<ValidateInstallment, Integer> {
    @Override
    public void initialize(ValidateInstallment constraintAnnotation) {
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return value == 6 || value == 9 || value == 12 || value == 24;
    }
}
