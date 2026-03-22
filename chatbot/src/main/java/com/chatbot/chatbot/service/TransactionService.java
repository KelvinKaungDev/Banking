package com.chatbot.chatbot.service;

import com.chatbot.chatbot.dto.Request.TransactionRequest;
import com.chatbot.chatbot.dto.Request.TransferRequest;
import com.chatbot.chatbot.dto.Response.TransactionResponse;

public interface TransactionService {
    TransactionResponse deposit(TransactionRequest request);
    TransactionResponse withdraw(TransactionRequest request);
    TransactionResponse transfer(TransferRequest request);
}