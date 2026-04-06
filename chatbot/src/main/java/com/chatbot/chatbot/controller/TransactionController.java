package com.chatbot.chatbot.controller;

import com.chatbot.chatbot.dto.Request.TransactionHistoryRequest;
import com.chatbot.chatbot.dto.Request.TransactionRequest;
import com.chatbot.chatbot.dto.Request.TransferRequest;
import com.chatbot.chatbot.dto.Response.TransactionHistoryResponse;
import com.chatbot.chatbot.dto.Response.TransactionResponse;
import com.chatbot.chatbot.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.deposit(request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.withdraw(request));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.transfer(request));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAccount(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccount(accountNumber));
    }

    @GetMapping("/history/{customerId}")
    public ResponseEntity<TransactionHistoryResponse> getHistory(
            @PathVariable Long customerId,
            @ModelAttribute TransactionHistoryRequest request) {
        return ResponseEntity.ok(transactionService.getTransactionHistory(customerId, request));
    }

    @GetMapping("/export/pdf/{customerId}")
    public ResponseEntity<byte[]> exportPdf(
            @PathVariable Long customerId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) throws Exception {

        byte[] pdf = transactionService.exportTransactionsToPdf(customerId, startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "transactions_" + customerId + "_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
