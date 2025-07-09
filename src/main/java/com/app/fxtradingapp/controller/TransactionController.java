package com.app.fxtradingapp.controller;

import com.app.fxtradingapp.dto.transaction.TransactionFilterDto;
import com.app.fxtradingapp.entity.Transaction;
import com.app.fxtradingapp.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Fetch paginated transactions", description = "Returns paginated and filtered transactions")
    public ResponseEntity<Page<Transaction>> getTransactions(
            @ModelAttribute TransactionFilterDto transactionFilterDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Page<Transaction> transactions = transactionService.getTransactions(
                transactionFilterDto,
                userDetails
        );
        return ResponseEntity.ok(transactions);
    }
}
