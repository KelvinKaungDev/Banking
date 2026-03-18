package com.chatbot.chatbot.controller;

import com.chatbot.chatbot.dto.Request.AccountRequest;
import com.chatbot.chatbot.dto.Response.AccountResponse;
import com.chatbot.chatbot.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("api/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountRequest accountRequest) {
        AccountResponse account = accountService.createAccount(accountRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        AccountResponse account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.status(HttpStatus.OK).body(account);
    }

    @GetMapping("customer/{customerId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByCustomer(@PathVariable Long customerId) {
        List<AccountResponse> accountByCustomer = accountService.getAccountByCustomer(customerId);
        return ResponseEntity.status(HttpStatus.OK).body(accountByCustomer);
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getBalance(accountNumber));
    }

    @PatchMapping("/{accountNumber}/freeze")
    public ResponseEntity<AccountResponse> freezeAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.freezeAccount(accountNumber));
    }

    @PatchMapping("/{accountNumber}/close")
    public ResponseEntity<AccountResponse> closeAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.closeAccount(accountNumber));
    }
}
