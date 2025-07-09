package com.app.fxtradingapp.messaging.dtos;

import com.app.fxtradingapp.dto.wallet.ConvertCurrencyDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class ConvertCurrencyMessage {
    private UUID userId;
    private ConvertCurrencyDto convertCurrencyDto;
    private String idempotencyKey;
}