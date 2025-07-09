package com.app.fxtradingapp.service;

import com.app.fxtradingapp.dto.ResponseDto;
import com.app.fxtradingapp.dto.wallet.ConvertCurrencyDto;
import com.app.fxtradingapp.dto.wallet.FundWalletDto;
import com.app.fxtradingapp.entity.Transaction;
import com.app.fxtradingapp.entity.Wallet;
import com.app.fxtradingapp.exception.CurrencyConversionException;
import com.app.fxtradingapp.exception.InsufficientFundsException;
import com.app.fxtradingapp.exception.OptimisticLockingFailureException;
import com.app.fxtradingapp.exception.WalletException;
import com.app.fxtradingapp.messaging.dtos.ConvertCurrencyMessage;
import com.app.fxtradingapp.messaging.dtos.FundWalletMessage;
import com.app.fxtradingapp.repository.TransactionRepository;
import com.app.fxtradingapp.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class WalletService {

//    @Autowired
//    private final FxService fxService;
//    private final WalletRepository walletRepository;
//    private final TransactionRepository transactionRepository;

//    public ResponseEntity<?> fundWallet(UUID userId, FundWalletDto fundWalletDto) {
//        ResponseDto response = new ResponseDto();
//        try{
//            BigDecimal amount = fundWalletDto.getAmount();
//
//            Wallet wallet = walletRepository.findByUserIdAndCurrencyCode(userId, "NGN");
//            if(wallet == null){
//                wallet = Wallet.builder()
//                        .userId(userId)
//                        .currencyCode("NGN")
//                        .balance(BigDecimal.ZERO)
//                        .createdAt(LocalDateTime.now())
//                        .build();
//            }
//
//            wallet.setBalance(wallet.getBalance().add(amount));
//            wallet.setUpdatedAt(LocalDateTime.now());
//            Wallet updatedWallet = walletRepository.save(wallet);
//
//            transactionRepository.save(Transaction.builder()
//                    .userId(userId)
//                    .type("fund")
//                    .fromCurrency("NGN")
//                    .toCurrency("NGN")
//                    .amount(amount)
//                    .status("success")
//                    .createdAt(LocalDateTime.now())
//                    .build());
//
//            response.setData(updatedWallet);
//            return ResponseEntity.status(HttpStatus.OK).body(response);
//        } catch (Exception e) {
//            System.out.println("failed");
//            System.out.println(e);
//            response.setMessage("An error occurred during wallet funding");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }
//
//    public ResponseEntity<?> convertCurrency(UUID userId, ConvertCurrencyDto convertCurrencyDto) {
//        ResponseDto response = new ResponseDto();
//      try{
//
//          Wallet fromWallet = walletRepository.findByUserIdAndCurrencyCode(userId, convertCurrencyDto.getFromCurrency());
//
//          if (fromWallet == null) {
//              response.setMessage(convertCurrencyDto.getFromCurrency()+" wallet not found");
//              return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//          }
//
//          Wallet toWallet = walletRepository.findByUserIdAndCurrencyCode(userId, convertCurrencyDto.getToCurrency());
//          if(toWallet == null){
//              toWallet = Wallet.builder()
//                      .userId(userId)
//                      .currencyCode(convertCurrencyDto.getToCurrency())
//                      .balance(BigDecimal.ZERO)
//                      .createdAt(LocalDateTime.now())
//                      .build();
//          }
//
//          if (fromWallet.getBalance().compareTo(convertCurrencyDto.getAmount()) < 0) {
//              response.setMessage("Insufficient funds");
//              return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//          }
//
//
//          Double rate = fxService.getRate(convertCurrencyDto.getFromCurrency(), convertCurrencyDto.getToCurrency());
//
//          if (rate == null) {
//              response.setMessage("Currency not supported");
//              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//          }
//
//          BigDecimal convertedAmount = convertCurrencyDto.getAmount().multiply(BigDecimal.valueOf(rate));
//
//          // Update wallets
//          fromWallet.setBalance(fromWallet.getBalance().subtract(convertCurrencyDto.getAmount()));
//          toWallet.setBalance(toWallet.getBalance().add(convertedAmount));
//
//          walletRepository.save(fromWallet);
//          walletRepository.save(toWallet);
//
//          // Log transaction
//          Transaction transaction = Transaction.builder()
//                  .userId(userId)
//                  .type("convert")
//                  .fromCurrency(convertCurrencyDto.getFromCurrency())
//                  .toCurrency(convertCurrencyDto.getToCurrency())
//                  .amount(convertCurrencyDto.getAmount())
//                  .rateUsed(BigDecimal.valueOf(rate))
//                  .status("success")
//                  .build();
//
//          transactionRepository.save(transaction);
//          response.setMessage("Conversion successful");
//          response.setData(transaction);
//          return ResponseEntity.status(HttpStatus.OK).body(response);
//
//      } catch (Exception e) {
//          System.out.println(e);
//          response.setMessage("An error occurred during currency conversion");
//          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//      }
//    }

    @Autowired
    private final FxService fxService;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;


    // Exchange and queue names
    private static final String FUND_WALLET_EXCHANGE = "wallet.exchange";
    private static final String CONVERT_CURRENCY_EXCHANGE = "wallet.exchange";

    // Redis keys
    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";

    @Transactional
    public ResponseEntity<?> fundWallet(UserDetails userDetails, FundWalletDto fundWalletDto) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        String idempotencyKey = fundWalletDto.getIdempotencyKey();

        if (idempotencyKey != null) {
            String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(redisKey, "processing", 24, TimeUnit.HOURS);
            if (Boolean.FALSE.equals(isNew)) {
                ResponseDto response = new ResponseDto();
                response.setMessage("This operation was already processed or is in progress");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        }


        // Create and send message to RabbitMQ
        FundWalletMessage message = new FundWalletMessage(userId, fundWalletDto, idempotencyKey);
        rabbitTemplate.convertAndSend(FUND_WALLET_EXCHANGE, "fund.wallet", message);

        ResponseDto response = new ResponseDto();
        response.setMessage("Wallet funding request accepted for processing");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }


    @Transactional
    public void processWalletFunding(FundWalletMessage message) {
        Objects.requireNonNull(message, "FundWalletMessage cannot be null");
        BigDecimal amount = message.getFundWalletDto().getAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        UUID userId = message.getUserId();

        Wallet wallet = walletRepository.findByUserIdAndCurrencyCodeWithLock(userId, "NGN")
                .orElseGet(() -> {
                    Wallet newWallet = Wallet.builder()
                            .userId(userId)
                            .currencyCode("NGN")
                            .balance(BigDecimal.ZERO)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return walletRepository.save(newWallet);
                });

        BigDecimal newBalance = wallet.getBalance().add(amount);
        int updatedRows = walletRepository.updateBalanceWithVersionCheck(
                wallet.getId(),
                newBalance,
                wallet.getVersion()
        );

        if (updatedRows == 0) {
            throw new OptimisticLockingFailureException("Wallet was modified concurrently");
        }

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .type("fund")
                .fromCurrency("NGN")
                .toCurrency("NGN")
                .amount(amount)
                .status("success")
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
    }


    @Transactional
    public ResponseEntity<?> convertCurrency(UserDetails userDetails, ConvertCurrencyDto convertCurrencyDto) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        // Idempotency check
        String idempotencyKey = convertCurrencyDto.getIdempotencyKey();

        if (idempotencyKey != null) {
            String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(redisKey, "processing", 24, TimeUnit.HOURS);
            if (Boolean.FALSE.equals(isNew)) {
                ResponseDto response = new ResponseDto();
                response.setMessage("This operation was already processed or is in progress");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        }

        // Send to RabbitMQ
        ConvertCurrencyMessage message = new ConvertCurrencyMessage(userId, convertCurrencyDto, idempotencyKey);
        rabbitTemplate.convertAndSend(CONVERT_CURRENCY_EXCHANGE, "convert.currency", message);

        ResponseDto response = new ResponseDto();
        response.setMessage("Currency conversion request accepted for processing");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @Transactional
    public void processCurrencyConversion(ConvertCurrencyMessage message) {
        Objects.requireNonNull(message, "ConvertCurrencyMessage cannot be null");
        Objects.requireNonNull(message.getConvertCurrencyDto(), "ConvertCurrencyDto cannot be null");

        Wallet fromWallet = walletRepository.findByUserIdAndCurrencyCodeWithLock(
                        message.getUserId(),
                        message.getConvertCurrencyDto().getFromCurrency())
                .orElseThrow(() -> new WalletException(
                        message.getConvertCurrencyDto().getFromCurrency() + " wallet not found"));

        Wallet toWallet = walletRepository.findByUserIdAndCurrencyCodeWithLock(
                        message.getUserId(),
                        message.getConvertCurrencyDto().getToCurrency())
                .orElseGet(() -> {
                    Wallet newWallet = Wallet.builder()
                            .userId(message.getUserId())
                            .currencyCode(message.getConvertCurrencyDto().getToCurrency())
                            .balance(BigDecimal.ZERO)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return walletRepository.save(newWallet);
                });

        if (fromWallet.getBalance().compareTo(message.getConvertCurrencyDto().getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds in " +
                    message.getConvertCurrencyDto().getFromCurrency() + " wallet");
        }

        Double rate = fxService.getRate(
                message.getConvertCurrencyDto().getFromCurrency(),
                message.getConvertCurrencyDto().getToCurrency());
        if (rate == null) {
            throw new CurrencyConversionException("Currency not supported");
        }

        BigDecimal amountToDeduct = message.getConvertCurrencyDto().getAmount();
        BigDecimal convertedAmount = amountToDeduct.multiply(BigDecimal.valueOf(rate));

        int fromUpdated = walletRepository.updateBalanceWithVersionCheck(
                fromWallet.getId(),
                fromWallet.getBalance().subtract(amountToDeduct),
                fromWallet.getVersion()
        );

        int toUpdated = walletRepository.updateBalanceWithVersionCheck(
                toWallet.getId(),
                toWallet.getBalance().add(convertedAmount),
                toWallet.getVersion()
        );

        if (fromUpdated == 0 || toUpdated == 0) {
            throw new OptimisticLockingFailureException("Wallet was modified concurrently during conversion");
        }

        Transaction transaction = Transaction.builder()
                .userId(message.getUserId())
                .type("convert")
                .fromCurrency(message.getConvertCurrencyDto().getFromCurrency())
                .toCurrency(message.getConvertCurrencyDto().getToCurrency())
                .amount(amountToDeduct)
                .rateUsed(BigDecimal.valueOf(rate))
                .status("success")
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
    }


}