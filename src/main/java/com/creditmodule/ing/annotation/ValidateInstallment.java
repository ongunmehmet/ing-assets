package com.creditmodule.ing.annotation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InstallmentValidator.class)
public @interface ValidateInstallment {

    String message() default "Invalid number of installments. Valid values are 6, 9, 12, 24.";


    Class<?>[] groups() default {};


    Class<? extends Payload>[] payload() default {};
}
