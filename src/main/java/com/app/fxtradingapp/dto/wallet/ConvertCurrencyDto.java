package com.app.fxtradingapp.dto.wallet;

import com.app.fxtradingapp.validation.ValidCurrency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ConvertCurrencyDto {
    @NotNull(message = "IdempotencyKey is required")
    @Size(min = 8, message = "idempotencyKey must be at least 8 characters")
    private String idempotencyKey;

    @NotNull(message = "From currency is required")
    @ValidCurrency
    private String fromCurrency;

    @NotNull(message = "To currency is required")
    @ValidCurrency
    private String toCurrency;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;
}