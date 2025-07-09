package com.app.fxtradingapp.messaging.listeners;

import com.app.fxtradingapp.messaging.dtos.ConvertCurrencyMessage;
import com.app.fxtradingapp.messaging.dtos.FundWalletMessage;
import com.app.fxtradingapp.service.WalletService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class WalletMessageListener {

    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;
    private final WalletService walletService;

    private static final String FUND_WALLET_QUEUE = "wallet.fund.queue";
    private static final String CONVERT_CURRENCY_QUEUE = "wallet.convert.queue";
    private static final String DEAD_LETTER_EXCHANGE = "wallet.deadletter.exchange";
    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";

    @RabbitListener(queues = FUND_WALLET_QUEUE)
    @Transactional
    public void processFundWallet(FundWalletMessage message) {
        String idempotencyKey = message.getIdempotencyKey();
        String redisKey = idempotencyKey != null ? IDEMPOTENCY_KEY_PREFIX + idempotencyKey : null;

        try {
            // Check if already processed (in case of consumer restart)
            if (redisKey != null && "processed".equals(redisTemplate.opsForValue().get(redisKey))) {
                return;
            }

            walletService.processWalletFunding(message);

            // Mark as completed in Redis
            if (redisKey != null) {
                redisTemplate.opsForValue().set(redisKey, "processed", 24, TimeUnit.HOURS);
            }

        } catch (Exception e) {
            // On failure, remove the idempotency key to allow retries
            if (redisKey != null) {
                redisTemplate.delete(redisKey);
            }
            // Send to dead letter queue
            rabbitTemplate.convertAndSend(DEAD_LETTER_EXCHANGE, "fund.wallet.dead", message);
            throw new RuntimeException("Failed to process fund wallet request", e);
        }
    }

    @RabbitListener(queues = CONVERT_CURRENCY_QUEUE)
    @Transactional
    public void processConvertCurrency(ConvertCurrencyMessage message) {
        String idempotencyKey = message.getIdempotencyKey();
        String redisKey = idempotencyKey != null ? IDEMPOTENCY_KEY_PREFIX + idempotencyKey : null;

        try {
            // Idempotency check
            if (redisKey != null && "processed".equals(redisTemplate.opsForValue().get(redisKey))) {
                return;
            }

            walletService.processCurrencyConversion(message);

            // Mark as completed in Redis
            if (redisKey != null) {
                redisTemplate.opsForValue().set(redisKey, "processed", 24, TimeUnit.HOURS);
            }

        } catch (Exception e) {
            // Clear idempotency key to allow retries
            if (redisKey != null) {
                redisTemplate.delete(redisKey);
            }

            // Send to dead letter queue with additional error context
            Map<String, Object> headers = new HashMap<>();
            headers.put("x-error-message", e.getMessage());
            headers.put("x-retry-count", 0);

            rabbitTemplate.convertAndSend(
                    DEAD_LETTER_EXCHANGE,
                    "convert.currency.dead",
                    message,
                    m -> {
                        m.getMessageProperties().setHeaders(headers);
                        return m;
                    }
            );

            throw e; // Re-throw to ensure transaction rollback
        }
    }

}
