package com.chatbot.chatbot.repository;

import com.chatbot.chatbot.model.Customer;
import com.chatbot.chatbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUserId(Long userId);

    boolean existsByUserId(Long userId);   // ← check if customer profile already exists

    boolean existsByPhone(String phone);

    Optional<Customer> findByUser(User user);
}
