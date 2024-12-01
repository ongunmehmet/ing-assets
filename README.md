# API Documentation

This document provides details about the API endpoints available in the application.

---
## Getting Started with Docker

### Prerequisites
Before getting started, ensure you have the following installed:

1-Docker
2-Docker Compose

### Step 1: Build the Application

Build the project using Maven to generate the JAR file:


```bash
mvn clean package
```

### Step 2: Build and Start Containers Using Docker Compose
```bash
docker-compose up -d
```
### Step 3: Stop the Services
```bash
docker-compose down
```
## Authentication Endpoints

### Login
- **URL:** `/api/auth/login`
- **Method:** `POST`
- **Description:** Authenticate a user and return a JWT token.
- **Request Body:**
    ```json
    {
        "accountNumber": "string",
        "password": "string"
    }
    ```
- **Response:**
    - `200 OK`: Token generated successfully.
    - `401 Unauthorized`: Invalid credentials.

### Register User and Customer
- **URL:** `/api/auth/register`
- **Method:** `POST`
- **Description:** Create a new user and associated customer.
- **Request Body:**
    ```json
    {
  "password": "159753",
  "name":"mehmet",
  "surname": "surname",
  "creditLimit": 10000
    }
    ```
- **Response:**
    - `200 OK`: User and customer created successfully with account number.
    - `400 Bad Request`: Validation errors.

---

## Customer Endpoints

### Get Customer by ID
- **URL:** `/v1/customers/{id}`
- **Method:** `GET`
- **Description:** Retrieve a customer's details by their ID.
- **Response:**
    - `200 OK`: Customer details.
    - `404 Not Found`: Customer not found.

### Get Customer ID by Account Number
- **URL:** `/v1/customers/getid/{accountNumber}`
- **Method:** `GET`
- **Description:** Retrieve a customer's ID using their account number.
- **Response:**
    - `200 OK`: Customer ID.
    - `404 Not Found`: Customer not found.

---

## Loan Endpoints

### Create Loan
- **URL:** `/v1/loan/createLoan`
- **Method:** `POST`
- **Description:** Create a new loan for a customer.
- **Request Body:**
    ```json
    {
  "accountNumber": "ee6434e1-05da-462e-baff-95968ba7dafd",
  "loanAmount": "2000",
  "numberOfInstallment": "6",
  "createDate": "30-11-2024"
    }
    ```
- **Response:**
    - `200 OK`: Loan created successfully.
    - `400 Bad Request`: Invalid loan request.

### Show Loan by ID
- **URL:** `/v1/loan/showLoan/{customerId}/{id}`
- **Method:** `GET`
- **Description:** Retrieve a specific loan by ID.
- **Response:**
    - `200 OK`: Loan details.
    - `404 Not Found`: Loan not found.

---

## Loan Installment Endpoints

### Get Loan Installment by ID
- **URL:** `/v1/loanInstallments/{id}`
- **Method:** `GET`
- **Description:** Retrieve loan installment details by ID.
- **Response:**
    - `200 OK`: Loan installment details.
    - `404 Not Found`: Loan installment not found.

### Pay Loan Installment
- **URL:** `/v1/loanInstallments/payloaninstallment`
- **Method:** `POST`
- **Description:** Make a payment for a loan installment.
- **Request Body:**
    ```json
    {
  "amount": 400,
  "paymentDate": "03-12-2024",
  "accountNumber": "ee6434e1-05da-462e-baff-95968ba7dafd",
  "loanId": 2
    }
    ```
- **Response:**
    - `200 OK`: Payment successful.
    - `400 Bad Request`: Invalid payment request.

---

## Notes

- All endpoints (except authentication) require a valid JWT token in the `Authorization` header.
- Example header:
    ```
    Authorization: Bearer <your-jwt-token>
    ```
- Errors follow a consistent format with a `message` field describing the error.