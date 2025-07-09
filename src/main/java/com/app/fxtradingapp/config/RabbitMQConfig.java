package com.app.fxtradingapp.config;

import org.springframework.amqp.core.*;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public DirectExchange walletExchange() {
        return new DirectExchange("wallet.exchange");
    }

    @Bean
    public Queue fundWalletQueue() {
        return QueueBuilder.durable("wallet.fund.queue")
                .withArgument("x-dead-letter-exchange", "wallet.deadletter.exchange")
                .withArgument("x-dead-letter-routing-key", "fund.wallet.dead")
                .build();
    }

    @Bean
    public Queue convertCurrencyQueue() {
        return QueueBuilder.durable("wallet.convert.queue")
                .withArgument("x-dead-letter-exchange", "wallet.deadletter.exchange")
                .withArgument("x-dead-letter-routing-key", "convert.currency.dead")
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("wallet.deadletter.exchange");
    }

    @Bean
    public Binding fundWalletBinding() {
        return BindingBuilder.bind(fundWalletQueue())
                .to(walletExchange())
                .with("fund.wallet");
    }

    @Bean
    public Binding convertCurrencyBinding() {
        return BindingBuilder.bind(convertCurrencyQueue())
                .to(walletExchange())
                .with("convert.currency");
    }
}