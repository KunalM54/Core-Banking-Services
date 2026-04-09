package com.example.BankSystem.model;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class StatementDto {
    private String type;
    private BigDecimal amount;
    private String date;

    // Static formatter to handle the "01/01/2025" format
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Constructor that takes the raw Database Entity and converts it
    public StatementDto(Transaction transaction) {
        this.type = transaction.getTransactionType().toString().toLowerCase(); // "DEPOSIT" -> "deposit"
        this.amount = transaction.getAmount();
        this.date = transaction.getDate().format(FORMATTER); // LocalDateTime -> "01/01/2025"
    }

    // Getters are required for JSON to work
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getDate() { return date; }
}
