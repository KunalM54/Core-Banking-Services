# Project Title
Core-Banking-Services

## Overview
Bank System is a Spring Boot backend project for core banking operations.  
It provides APIs to open an account, deposit money, withdraw money, transfer funds, check account balance, and view account statements.

## Features
- Open account with minimum opening balance validation.
- Auto-generate unique 10-digit account number.
- Deposit operation with validation.
- Withdraw operation with validation and insufficient balance check.
- Transfer money between two accounts.
- Transaction recording for all operations (`DEPOSIT`, `WITHDRAW`, `TRANSFER`).
- Statement API with simplified DTO response.
- Pessimistic locking in withdraw and transfer flows to avoid concurrent balance conflicts.
- Transactional service methods for data consistency.

## Tech Stack
- Java 17
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- MySQL
- Maven

## System Design / How It Works
1. Account is created using `/account/open` with name and opening balance.
2. Service validates minimum balance (`>= 10`) and generates a unique account number.
3. Deposit and withdraw update account balance and create transaction records.
4. Transfer loads sender and receiver accounts with DB lock, validates balance, updates both balances, and stores two transfer transaction entries (debit/credit style).
5. Statement API fetches all transactions for an account and maps them to `StatementDto` (`type`, `amount`, `date`).
6. Pessimistic locking (`PESSIMISTIC_WRITE`) is used in critical money operations to prevent race conditions during concurrent requests.

## Project Structure
```text
src/main/java/com/example/BankSystem
├── BankSystemApplication.java
├── controller
│   └── BankController.java
├── service
│   └── AccountService.java
├── repository
│   ├── AccountRepository.java
│   └── TransactionRepository.java
├── model
│   ├── Account.java
│   ├── Transaction.java
│   └── StatementDto.java
└── transection
    └── TransactionType.java
```

## Setup & Installation
1. Install Java 17, Maven, and MySQL.
2. Create database:
   - `bankdb`
3. Configure properties in `src/main/resources/application.properties`:
   - datasource URL
   - username
   - password
4. Build and run:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## API Endpoints
Base path: `/account`

### 1) Open Account
- **POST** `/account/open`
- **Query Params**: `name`, `balance`

Example:
```http
POST /account/open?name=Kunal&balance=5000
```

### 2) Deposit
- **POST** `/account/deposit`
- **Query Params**: `accountNumber`, `amount`

Example:
```http
POST /account/deposit?accountNumber=1234567890&amount=1000
```

### 3) Withdraw
- **POST** `/account/withdraw`
- **Query Params**: `accountNumber`, `amount`

Example:
```http
POST /account/withdraw?accountNumber=1234567890&amount=500
```

### 4) Balance
- **GET** `/account/balance`
- **Query Params**: `accountNumber`

Example:
```http
GET /account/balance?accountNumber=1234567890
```

### 5) Transfer
- **POST** `/account/transfer`
- **Query Params**: `fromAccount`, `toAccount`, `amount`

Example:
```http
POST /account/transfer?fromAccount=1234567890&toAccount=9876543210&amount=200
```

### 6) Statement
- **GET** `/account/statement`
- **Query Params**: `accountNumber`

Example:
```http
GET /account/statement?accountNumber=1234567890
```

Sample response:
```json
[
  {
    "type": "deposit",
    "amount": 5000,
    "date": "09/04/2026"
  }
]
```

## Database Schema

### `account`
- `id` (PK, auto-increment)
- `account_number` (unique, not null, length 10)
- `name` (not null)
- `balance` (not null)

### `transaction`
- `id` (PK, auto-increment)
- `transaction_type` (`DEPOSIT`, `WITHDRAW`, `TRANSFER`)
- `amount`
- `date`
- `account_id` (FK -> `account.id`)

## Configuration Notes
Current `application.properties` includes:
- `server.port=8081`
- `spring.datasource.url=jdbc:mysql://localhost:3306/bankdb`
- `spring.datasource.username=...`
- `spring.datasource.password=...`
- `spring.jpa.hibernate.ddl-auto=update`
- `spring.jpa.show-sql=true`

## Future Improvements
- Add global exception handling with consistent error response format.
- Add request DTOs and validation annotations for API inputs.
- Add authentication and authorization (JWT + role-based access).
- Add unit and integration test coverage.
- Add pagination and date filters in statement API.
- Add Docker support and environment-based configuration.
