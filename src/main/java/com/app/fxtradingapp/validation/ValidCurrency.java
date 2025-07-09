package com.app.fxtradingapp.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CurrencyValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrency {
    String message() default "Supported currency code; USD, EUR, GBP, NGN, KES, ZAR, JPY, CAD, AUD, CHF";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
