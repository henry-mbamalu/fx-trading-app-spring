package com.app.fxtradingapp.service;

import com.app.fxtradingapp.dto.transaction.TransactionFilterDto;
import com.app.fxtradingapp.entity.Transaction;
import com.app.fxtradingapp.repository.TransactionRepository;
import com.app.fxtradingapp.specification.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public Page<Transaction> getTransactions(
           TransactionFilterDto transactionFilterDto,
           UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        Sort sort = transactionFilterDto.getDirection().equalsIgnoreCase("asc") ? Sort.by(transactionFilterDto.getSortBy()).ascending() : Sort.by(transactionFilterDto.getSortBy()).descending();
        int normalizedPage = transactionFilterDto.getPage() > 0 ? transactionFilterDto.getPage() - 1 : 0;
        Pageable pageable = PageRequest.of(normalizedPage, transactionFilterDto.getSize(), sort);
        Specification<Transaction> spec = TransactionSpecification.filterByCriteria(
                transactionFilterDto.getStatus(), transactionFilterDto.getType(), transactionFilterDto.getFromCurrency(), transactionFilterDto.getToCurrency(), userId, isAdmin
        );
        return transactionRepository.findAll(spec, pageable);
    }
}
