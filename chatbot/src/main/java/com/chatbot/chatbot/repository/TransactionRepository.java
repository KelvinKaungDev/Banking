package com.chatbot.chatbot.repository;

import com.chatbot.chatbot.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByToAccountAccountNumber(String accountNumber);

    List<Transaction> findByFromAccountAccountNumber(String accountNumber);

    @Query("""
        SELECT t FROM Transaction t
        LEFT JOIN t.fromAccount fa
        LEFT JOIN t.toAccount ta
        WHERE (fa.customer.id = :customerId OR ta.customer.id = :customerId)
          AND (:startDate IS NULL OR t.createdAt >= :startDate)
          AND (:endDate IS NULL OR t.createdAt <= :endDate)
        ORDER BY t.createdAt DESC
        """)
    Page<Transaction> findByCustomerId(
            @Param("customerId") Long customerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
