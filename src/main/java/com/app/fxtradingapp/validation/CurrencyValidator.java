package com.app.fxtradingapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {
    @Override
    public boolean isValid(String currencyCode, ConstraintValidatorContext context) {
        if (currencyCode == null) return false;
        return SupportedCurrencies.CODES.contains(currencyCode);
    }
}
