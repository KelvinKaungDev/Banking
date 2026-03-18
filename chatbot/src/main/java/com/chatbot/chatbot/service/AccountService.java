package com.chatbot.chatbot.service;

import com.chatbot.chatbot.dto.Request.AccountRequest;
import com.chatbot.chatbot.dto.Response.AccountResponse;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    AccountResponse createAccount(AccountRequest accountRequest);

    AccountResponse getAccountByNumber(String accountNumber);

    List<AccountResponse> getAccountByCustomer(Long CustomerId);

    BigDecimal getBalance(String accountNumber);

    AccountResponse freezeAccount(String accountNumber);

    AccountResponse closeAccount(String accountNumber);
}
