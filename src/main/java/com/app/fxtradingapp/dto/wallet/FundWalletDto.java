package com.app.fxtradingapp.dto.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundWalletDto {
    @NotNull(message = "IdempotencyKey is required")
    @Size(min = 8, message = "idempotencyKey must be at least 8 characters")
    private String idempotencyKey;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1", inclusive = true, message = "Amount must be at least 1")
    private BigDecimal amount;
}