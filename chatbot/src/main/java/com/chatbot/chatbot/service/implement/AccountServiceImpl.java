package com.chatbot.chatbot.service.implement;

import com.chatbot.chatbot.dto.Request.AccountRequest;
import com.chatbot.chatbot.dto.Response.AccountResponse;
import com.chatbot.chatbot.model.Account;
import com.chatbot.chatbot.model.Customer;
import com.chatbot.chatbot.model.enumList.AccountStatus;
import com.chatbot.chatbot.repository.AccountRepository;
import com.chatbot.chatbot.repository.CustomerRepository;
import com.chatbot.chatbot.service.AccountService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .accountType(request.getAccountType())
                .status(AccountStatus.ACTIVE)
                .balance(request.getInitialDeposit())
                .customer(customer)
                .build();

        return mapToResponse(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
        return mapToResponse(account);
    }

    @Override
    @Transactional
    public List<AccountResponse> getAccountByCustomer(Long customerId) {
        return accountRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BigDecimal getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
        return account.getBalance();
    }

    @Override
    public AccountResponse freezeAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new RuntimeException("Cannot freeze a closed account");
        }

        account.setStatus(AccountStatus.FROZEN);
        return mapToResponse(accountRepository.save(account));
    }

    @Override
    public AccountResponse closeAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new RuntimeException("Cannot closed a freeze account");
        }

        account.setStatus(AccountStatus.CLOSED);
        return mapToResponse(accountRepository.save(account));
    }

    private String generateAccountNumber() {
        long count = accountRepository.count() + 1;
        return String.format("ACC-%06d", count);
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .status(account.getStatus())
                .balance(account.getBalance())
                .customerName(account.getCustomer().getFirstName()
                        + " " + account.getCustomer().getLastName())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
