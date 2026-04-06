package com.chatbot.chatbot.dto.Response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TransactionHistoryResponse {
    private List<TransactionResponse> transactions;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int size;
}