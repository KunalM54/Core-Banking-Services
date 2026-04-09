package com.example.BankSystem.service;

import com.example.BankSystem.model.Account;
import com.example.BankSystem.model.StatementDto;
import com.example.BankSystem.model.Transaction;
import com.example.BankSystem.repository.AccountRepository;
import com.example.BankSystem.repository.TransactionRepository;
import com.example.BankSystem.transection.TransactionType;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private static final BigDecimal MIN_BALANCE = new BigDecimal("10");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            long number = 1000000000L + (long)(Math.random() * 9000000000L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    @Transactional
    public Account openAccount(String name, BigDecimal balance) {

        if (balance.compareTo(MIN_BALANCE) < 0) {
            throw new RuntimeException("Minimum balance must be " + MIN_BALANCE);
        }


        Account account = new Account();
        account.setName(name);
        account.setBalance(balance);

        String accountNumber = generateAccountNumber();
        account.setAccountNumber(accountNumber);

        Account savedAccount = accountRepository.save(account);

        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            Transaction transaction = new Transaction();
            transaction.setAmount(balance);
            transaction.setTransactionType(TransactionType.DEPOSIT);
            transaction.setDate(LocalDateTime.now());
            transaction.setAccount(savedAccount);
            transactionRepository.save(transaction);
        }
        return savedAccount;
    }

    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit amount must be greater than 0");
        }

        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);

        Account updatedAccount = accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setDate(LocalDateTime.now());
        transaction.setAccount(updatedAccount);
        transactionRepository.save(transaction);
        return updatedAccount;
    }

    @Transactional
    public Account withdraw(String accountNumber, BigDecimal amount) {
//        Account account = accountRepository.findByAccountNumber(accountNumber)
//                .orElseThrow(() -> new RuntimeException("Account not found"));


        // Finds account by account number and locks the row in DB so no other thread can touch it until current transaction finishes
        Account account = accountRepository.findByAccountNumberWithLock(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Thread is only used to check, how lock work
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdrawal amount must be greater than 0");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds. Current balance: " + account.getBalance());
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        Account updatedAccount = accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.WITHDRAW);
        transaction.setAmount(amount);
        transaction.setDate(LocalDateTime.now());
        transaction.setAccount(updatedAccount);
        transactionRepository.save(transaction);

        return updatedAccount;
    }

    public Account getAccountDetails(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    @Transactional
    public void transferAmount(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {

//        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
//                .orElseThrow(() -> new RuntimeException("Sender account not found"));
//
//        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
//                .orElseThrow(() -> new RuntimeException("Receiver account not found"));

        // Finds account by account number and locks the row in DB so no other thread can touch it until current transaction finishes
        Account fromAccount = accountRepository.findByAccountNumberWithLock(fromAccountNumber)
                .orElseThrow(() -> new RuntimeException("Sender account not found"));

        // Finds account by account number and locks the row in DB so no other thread can touch it until current transaction finishes
        Account toAccount = accountRepository.findByAccountNumberWithLock(toAccountNumber)
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));


        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transfer amount must be greater than 0");
        }
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        BigDecimal sourceBalance = fromAccount.getBalance().subtract(amount);
        fromAccount.setBalance(sourceBalance);

        BigDecimal targetBalance = toAccount.getBalance().add(amount);
        toAccount.setBalance(targetBalance);

        // Thread is only used to check, how lock work
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 6. Save Both Accounts
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // 7. Create Transaction Record for SENDER (Debit)
        Transaction debitTx = new Transaction();
        debitTx.setTransactionType(TransactionType.TRANSFER); // Or WITHDRAWAL
        debitTx.setAmount(amount.negate()); // Optional: Store as negative to indicate money leaving
        debitTx.setDate(LocalDateTime.now());
        debitTx.setAccount(fromAccount);
        transactionRepository.save(debitTx);

        // 8. Create Transaction Record for RECEIVER (Credit)
        Transaction creditTx = new Transaction();
        creditTx.setTransactionType(TransactionType.TRANSFER); // Or DEPOSIT
        creditTx.setAmount(amount);
        creditTx.setDate(LocalDateTime.now());
        creditTx.setAccount(toAccount);
        transactionRepository.save(creditTx);
    }

    public List<StatementDto> getStatement(String accountNumber) {
        // 1. Find the Account
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // 2. Fetch raw transactions from DB
        List<Transaction> transactions = transactionRepository.findByAccount(account);

        // 3. Convert to DTO list using Stream (Simpler version)
        return transactions.stream()
                .map(StatementDto::new) // Calls the DTO constructor we made in Step 1
                .collect(Collectors.toList());
    }
}