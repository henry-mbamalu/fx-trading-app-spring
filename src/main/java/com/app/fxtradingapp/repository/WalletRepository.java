package com.app.fxtradingapp.repository;


import com.app.fxtradingapp.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
//    Wallet findByUserIdAndCurrencyCode(UUID userId, String currencyCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.currencyCode = :currencyCode")
    Optional<Wallet> findByUserIdAndCurrencyCodeWithLock(
            @Param("userId") UUID userId,
            @Param("currencyCode") String currencyCode
    );

    // Optimistic lock (version check on save)
    @Modifying
    @Query("UPDATE Wallet w SET w.balance = :balance WHERE w.id = :id AND w.version = :version")
    int updateBalanceWithVersionCheck(
            @Param("id") UUID id,
            @Param("balance") BigDecimal balance,
            @Param("version") Long version
    );
}