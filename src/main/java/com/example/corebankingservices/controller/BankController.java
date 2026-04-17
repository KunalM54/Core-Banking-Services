package com.example.corebankingservices.controller;

import com.example.corebankingservices.model.Account;
import com.example.corebankingservices.model.StatementDto;
import com.example.corebankingservices.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/account")
public class BankController {
    private final AccountService accountService;

    public BankController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/open")
    public Account openAccount(@RequestParam String name,
                               @RequestParam BigDecimal balance) {

        return accountService.openAccount(name, balance);
    }

    @PostMapping("/deposit")
    public Account deposit(@RequestParam String accountNumber,
                           @RequestParam BigDecimal amount) {
        return accountService.deposit(accountNumber, amount);
    }

    @PostMapping("/withdraw")
    public Account withdraw(@RequestParam String accountNumber,
                            @RequestParam BigDecimal amount) {
        return accountService.withdraw(accountNumber, amount);
    }

    @GetMapping("/balance")
    public Account getAccountBalance(@RequestParam String accountNumber) {
        return accountService.getAccountDetails(accountNumber);
    }

    @PostMapping("/transfer")
    public String transfer(@RequestParam String fromAccount,
                           @RequestParam String toAccount,
                           @RequestParam BigDecimal amount) {

        accountService.transferAmount(fromAccount, toAccount, amount);

        return "Transfer Successful! Sent " + amount + " to account " + toAccount;
    }

    @GetMapping("/statement")
    public List<StatementDto> getAccountStatement(@RequestParam String accountNumber) {
        return accountService.getStatement(accountNumber);
    }
}
