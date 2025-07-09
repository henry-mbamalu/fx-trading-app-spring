package com.app.fxtradingapp.controller;

import com.app.fxtradingapp.dto.wallet.ConvertCurrencyDto;
import com.app.fxtradingapp.dto.wallet.FundWalletDto;
import com.app.fxtradingapp.entity.Wallet;
import com.app.fxtradingapp.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
@Validated
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/fund")
    public ResponseEntity<?> fundWallet(
            @Valid @RequestBody FundWalletDto fundWalletDto, @AuthenticationPrincipal UserDetails userDetails) {

        return walletService.fundWallet(userDetails, fundWalletDto);
    }

    @PostMapping("/convert")
    public ResponseEntity<?> convertCurrency(@Valid @RequestBody ConvertCurrencyDto convertCurrencyDto, @AuthenticationPrincipal UserDetails userDetails) {

        return walletService.convertCurrency(userDetails, convertCurrencyDto);
    }
}