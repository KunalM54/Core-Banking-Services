# Core Banking Services

A Spring Boot REST API providing core banking operations including account management, deposits, withdrawals, transfers, and transaction history.

## Overview

This application implements fundamental banking operations using Spring Data JPA with MySQL. It provides account creation with auto-generated 10-digit account numbers, fund management through deposits and withdrawals, inter-account transfers with pessimistic locking, and complete transaction history tracking.

## Features

* Account Management
* Fund Deposits
* Fund Withdrawals
* Account Balance Inquiry
* Inter-Account Fund Transfers
* Transaction History (Account Statement)

## Tech Stack

* Java 17
* Spring Boot 4.0.3
* Spring Data JPA
* Spring Web MVC
* MySQL

## System Design / How It Works

1. **Open Account** - Generates unique 10-digit account number, validates minimum balance of 10, saves account, creates DEPOSIT transaction if initial balance > 0
2. **Deposit** - Finds account by account number, validates amount > 0, updates balance, records DEPOSIT transaction with current timestamp
3. **Withdraw** - Acquires pessimistic write lock on account row, validates sufficient balance, updates balance, records WITHDRAW transaction
4. **Transfer** - Acquires pessimistic write locks on both accounts (source then target), validates amount and balance, updates both balances, records TRANSFER transactions for both accounts
5. **Statement** - Fetches all transactions for account, maps to StatementDto with lowercase type and dd/MM/yyyy formatted date

## Project Structure

```text
com.example.corebankingservices
├── BankSystemApplication.java
├── model
│   ├── Account.java
│   ├── Transaction.java
│   └── StatementDto.java
├── transection
│   └── TransactionType.java
├── controller
│   └── BankController.java
├── service
│   └── AccountService.java
└── repository
    ├── AccountRepository.java
    └── TransactionRepository.java
```

## Setup & Installation

1. Ensure Java 17 and Maven are installed
2. Create MySQL database
3. Configure `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/your_database
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```
4. Run `mvn spring-boot:run`

## API Endpoints

Base path: `http://localhost:8080/account`

### 1) Open Account

* **POST** `/account/open`
* **Query Params**: name, balance

```http
POST /account/open?name=John&balance=500
```

### 2) Deposit

* **POST** `/account/deposit`
* **Query Params**: accountNumber, amount

```http
POST /account/deposit?accountNumber=1234567890&amount=100
```

### 3) Withdraw

* **POST** `/account/withdraw`
* **Query Params**: accountNumber, amount

```http
POST /account/withdraw?accountNumber=1234567890&amount=50
```

### 4) Get Balance

* **GET** `/account/balance`
* **Query Params**: accountNumber

```http
GET /account/balance?accountNumber=1234567890
```

### 5) Transfer

* **POST** `/account/transfer`
* **Query Params**: fromAccount, toAccount, amount

```http
POST /account/transfer?fromAccount=1234567890&toAccount=0987654321&amount=100
```

### 6) Get Statement

* **GET** `/account/statement`
* **Query Params**: accountNumber

```http
GET /account/statement?accountNumber=1234567890
```

## Database Schema

### `account`

* `id` (PK)
* `accountNumber`
* `name`
* `balance`

### `transaction`

* `id` (PK)
* `transactionType`
* `amount`
* `date`
* `account_id` (FK -> account)

## Configuration Notes

* `spring.application.name=corebankingservices`
* `spring.datasource.url=jdbc:mysql://localhost:3306/your_database`
* `spring.datasource.username=your_username`
* `spring.datasource.password=your_password`
* `spring.jpa.hibernate.ddl-auto=update`

## Future Improvements

* Add authentication and authorization
* Implement overdraft protection
* Add pagination to statement endpoint
* Support date range filtering for statements

