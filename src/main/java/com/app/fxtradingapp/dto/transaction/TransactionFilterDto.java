package com.app.fxtradingapp.dto.transaction;

import lombok.Data;

@Data
public class TransactionFilterDto {
    private String status;
    private String type;
    private String fromCurrency;
    private String toCurrency;

    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String direction = "desc";
}
