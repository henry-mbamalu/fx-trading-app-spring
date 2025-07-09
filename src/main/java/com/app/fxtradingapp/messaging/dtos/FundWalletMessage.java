package com.app.fxtradingapp.messaging.dtos;

import com.app.fxtradingapp.dto.wallet.FundWalletDto;
import lombok.AllArgsConstructor;
import lombok.Data;


import java.util.UUID;

@AllArgsConstructor
@Data
public class FundWalletMessage {
    private UUID userId;
    private FundWalletDto fundWalletDto;
    private String idempotencyKey;
}