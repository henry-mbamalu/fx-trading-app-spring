package com.app.fxtradingapp.specification;

import com.app.fxtradingapp.entity.Transaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class TransactionSpecification {

    public static Specification<Transaction> filterByCriteria(
            String status, String type, String fromCurrency, String toCurrency, UUID userId, boolean isAdmin) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (fromCurrency != null) {
                predicates.add(cb.equal(root.get("fromCurrency"), fromCurrency));
            }
            if (toCurrency != null) {
                predicates.add(cb.equal(root.get("toCurrency"), toCurrency));
            }
            if (!isAdmin && userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
