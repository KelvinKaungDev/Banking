package com.chatbot.chatbot.repository;

import com.chatbot.chatbot.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByToAccountAccountNumber(String accountNumber);

    List<Transaction> findByFromAccountAccountNumber(String accountNumber);
}
