package com.chatbot.chatbot.service.implement;

import com.chatbot.chatbot.dto.Request.TransactionRequest;
import com.chatbot.chatbot.dto.Request.TransferRequest;
import com.chatbot.chatbot.dto.Response.TransactionResponse;
import com.chatbot.chatbot.model.Account;
import com.chatbot.chatbot.model.Transaction;
import com.chatbot.chatbot.model.enumList.AccountStatus;
import com.chatbot.chatbot.model.enumList.TransactionStatus;
import com.chatbot.chatbot.model.enumList.TransactionType;
import com.chatbot.chatbot.repository.AccountRepository;
import com.chatbot.chatbot.repository.TransactionRepository;
import com.chatbot.chatbot.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public TransactionResponse deposit(TransactionRequest request) {

        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found: "
                        + request.getAccountNumber()));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active: "
                    + request.getAccountNumber());
        }

        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .referenceNumber(generateReferenceNumber())
                .transactionType(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .amount(request.getAmount())
                .description(request.getDescription())
                .toAccount(account)    // ← deposit goes TO this account
                .fromAccount(null)     // ← no sender for deposit
                .build();

        return mapToResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public TransactionResponse transfer(TransferRequest request) {

        // Step 1 - Find both accounts
        Account fromAccount = accountRepository
                .findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() -> new RuntimeException("Sender account not found: "
                        + request.getFromAccountNumber()));

        Account toAccount = accountRepository
                .findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new RuntimeException("Receiver account not found: "
                        + request.getToAccountNumber()));

        // Step 2 - Check sender is ACTIVE
        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Sender account is not active");
        }

        // Step 3 - Check receiver is ACTIVE
        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Receiver account is not active");
        }

        // Step 4 - Cannot transfer to same account
        if (fromAccount.getAccountNumber().equals(toAccount.getAccountNumber())) {
            throw new RuntimeException("Cannot transfer to the same account");
        }

        // Step 5 - Check sufficient balance
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance! Current balance: "
                    + fromAccount.getBalance());
        }

        // Step 6 - Deduct from sender
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        accountRepository.save(fromAccount);

        // Step 7 - Add to receiver
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
        accountRepository.save(toAccount);

        // Step 8 - Record transaction
        Transaction transaction = Transaction.builder()
                .referenceNumber(generateReferenceNumber())
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .amount(request.getAmount())
                .description(request.getDescription())
                .fromAccount(fromAccount)   // ← sender
                .toAccount(toAccount)       // ← receiver
                .build();

        return mapToResponse(transactionRepository.save(transaction));
    }


//    @Override
//    public TransactionResponse transfer(TransferRequest request) {
//
//        // Step 1 - Find both accounts
//        Account fromAccount = accountRepository
//                .findByAccountNumber(request.getFromAccountNumber())
//                .orElseThrow(() -> new RuntimeException("Sender account not found: "
//                        + request.getFromAccountNumber()));
//
//        Account toAccount = accountRepository
//                .findByAccountNumber(request.getToAccountNumber())
//                .orElseThrow(() -> new RuntimeException("Receiver account not found: "
//                        + request.getToAccountNumber()));
//
//        // Step 2 - Check sender is ACTIVE
//        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
//            throw new RuntimeException("Sender account is not active");
//        }
//
//        // Step 3 - Check receiver is ACTIVE
//        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
//            throw new RuntimeException("Receiver account is not active");
//        }
//
//        // Step 4 - Cannot transfer to same account
//        if (fromAccount.getAccountNumber().equals(toAccount.getAccountNumber())) {
//            throw new RuntimeException("Cannot transfer to the same account");
//        }
//
//        // Step 5 - Check sufficient balance
//        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
//            throw new RuntimeException("Insufficient balance! Current balance: "
//                    + fromAccount.getBalance());
//        }
//
//        // Step 6 - Deduct from sender
//        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
//        accountRepository.save(fromAccount);
//
//        if (true) throw new RuntimeException("SIMULATED CRASH after deducting sender!");
//
//        // Step 7 - Add to receiver
//        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
//        accountRepository.save(toAccount);
//
//        // Step 8 - Record transaction
//        Transaction transaction = Transaction.builder()
//                .referenceNumber(generateReferenceNumber())
//                .transactionType(TransactionType.TRANSFER)
//                .status(TransactionStatus.SUCCESS)
//                .amount(request.getAmount())
//                .description(request.getDescription())
//                .fromAccount(fromAccount)   // ← sender
//                .toAccount(toAccount)       // ← receiver
//                .build();
//
//        return mapToResponse(transactionRepository.save(transaction));
//    }

    @Override
    @Transactional
    public TransactionResponse withdraw(TransactionRequest request) {
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found: "
                        + request.getAccountNumber()));

        // Step 2 - Check account is ACTIVE
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active: "
                    + request.getAccountNumber());
        }

        // Step 3 - Check sufficient balance
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance! Current balance: "
                    + account.getBalance());
        }

        // Step 4 - Deduct money
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        // Step 5 - Record transaction
        Transaction transaction = Transaction.builder()
                .referenceNumber(generateReferenceNumber())
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.SUCCESS)
                .amount(request.getAmount())
                .description(request.getDescription())
                .fromAccount(account)    // ← money comes FROM this account
                .toAccount(null)         // ← no receiver for withdrawal
                .build();

        return mapToResponse(transactionRepository.save(transaction));
    }

    private String generateReferenceNumber() {
        return "TXN-" + System.currentTimeMillis();
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .referenceNumber(transaction.getReferenceNumber())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .fromAccount(transaction.getFromAccount() != null
                        ? transaction.getFromAccount().getAccountNumber() : null)
                .toAccount(transaction.getToAccount() != null
                        ? transaction.getToAccount().getAccountNumber() : null)
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}