package com.chatbot.chatbot.dto.Response;

import com.chatbot.chatbot.model.enumList.AccountStatus;
import com.chatbot.chatbot.model.enumList.AccountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private AccountStatus status;
    private BigDecimal balance;
    private String customerName;
    private LocalDateTime createdAt;
}