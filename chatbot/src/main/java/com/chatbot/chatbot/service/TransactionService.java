package com.chatbot.chatbot.service;

import com.chatbot.chatbot.dto.Request.TransactionHistoryRequest;
import com.chatbot.chatbot.dto.Request.TransactionRequest;
import com.chatbot.chatbot.dto.Request.TransferRequest;
import com.chatbot.chatbot.dto.Response.TransactionHistoryResponse;
import com.chatbot.chatbot.dto.Response.TransactionResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
    TransactionResponse deposit(TransactionRequest request);
    TransactionResponse withdraw(TransactionRequest request);
    TransactionResponse transfer(TransferRequest request);
    List<TransactionResponse> getTransactionsByAccount(String accountNumber);
    TransactionHistoryResponse getTransactionHistory(
            Long customerId,
            TransactionHistoryRequest request
    );
    byte[] exportTransactionsToPdf(Long customerId,
                                   LocalDateTime startDate,
                                   LocalDateTime endDate) throws Exception;
}