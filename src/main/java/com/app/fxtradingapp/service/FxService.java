package com.app.fxtradingapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

@Service
public class FxService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String FX_CACHE_PREFIX = "fx_rate_";

    @Value("${FX_API_BASE_URL}")
    private String fxApiBaseUrl;

    public Double getRate(String fromCurrency, String toCurrency) {
        String key = FX_CACHE_PREFIX + fromCurrency + "_" + toCurrency;
        String backupKey = key + "_backup";


        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (Double) cached;
        }

        try {

            String url = fxApiBaseUrl + fromCurrency;
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> res = restTemplate.getForObject(url, Map.class);

            if (!"success".equals(res.get("result"))) {
                throw new RuntimeException("Failed to fetch rate from API");
            }

            Map<String, Double> rates = (Map<String, Double>) res.get("rates");
            Double rate = rates.get(toCurrency);

            if (rate == null) {
                throw new RuntimeException("Unsupported currency");
            }


            redisTemplate.opsForValue().set(key, rate, Duration.ofMinutes(10));
            redisTemplate.opsForValue().set(backupKey, rate);

            return rate;

        } catch (Exception e) {
            System.err.println("API failed: " + e.getMessage());


            Object backupRate = redisTemplate.opsForValue().get(backupKey);
            if (backupRate != null) {
                System.out.println("Using fallback backup rate: " + backupRate);
                return (Double) backupRate;
            }

            throw new RuntimeException("Rate unavailable and no fallback found");
        }
    }
}
