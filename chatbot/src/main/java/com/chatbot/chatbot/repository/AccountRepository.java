package com.chatbot.chatbot.repository;

import com.chatbot.chatbot.model.Account;
import com.chatbot.chatbot.model.enumList.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(Long customerId);

    List<Account> findByCustomerIdAndStatus(Long customerId, AccountStatus status);
}
