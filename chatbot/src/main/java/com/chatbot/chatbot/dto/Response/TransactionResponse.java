package com.chatbot.chatbot.dto.Response;

import com.chatbot.chatbot.model.enumList.TransactionStatus;
import com.chatbot.chatbot.model.enumList.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {

    private Long id;
    private String referenceNumber;
    private TransactionType transactionType;
    private TransactionStatus status;
    private BigDecimal amount;
    private String description;
    private String fromAccount;
    private String toAccount;
    private LocalDateTime createdAt;
}