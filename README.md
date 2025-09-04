# Application Run and API Documentation

This document provides details about the API endpoints available in the application.

## Prerequisites
Before getting started, ensure you have the following installed:

1-Docker  
2-Docker Compose

---

## Getting Started Local
```bash
docker-compose -f docker-compose-localdb.yml up -d
```

Then start `IngApplication.java`

Then copy curls from `curlList.txt` and import into Postman:

1-register - first created user assigned as admin  
2-login - use received token as bearer token for all requests  
3-create an asset  
4-create an order  
5-admin matches orders

```json
{
  "accountNumber": "ccba07f2-07be-4205-bc5b-476cebf6799b",
  "password": "123456"
}
```

---

## For API Documentation
After running the application:
- [Swagger UI - API Documentation](http://localhost:8080/swagger-ui/index.html#/)

---

## Getting Started with Docker

### Step 1: Build the Application
Build the project using Maven to generate the JAR file:

```bash
mvn clean package
```

### Step 2: Build and Start Containers Using Docker Compose
```bash
docker-compose up -d
```

1-register (first user = admin)  
2-login (use JWT token)  
3-create an asset  
4-create orders  
5-match orders

### Step 3: Stop the Services
```bash
docker-compose down
```

---

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
    - `200 OK`: Token generated successfully
    - `401 Unauthorized`: Invalid credentials

### Register User and Customer
- **URL:** `/api/auth/register`
- **Method:** `POST`
- **Description:** Create a new user and associated customer.
- **Request Body:**
```json
{
  "password": "159753",
  "name": "mehmet",
  "surname": "surname",
  "creditLimit": 10000
}
```
- **Response:**
    - `200 OK`: User and customer created successfully with account number
    - `400 Bad Request`: Validation errors

---

## Customer Endpoints

### Get Customer by ID
- **URL:** `/v1/customers/{id}`
- **Method:** `GET`
- **Description:** Retrieve a customer's details by their ID.
- **Response:**
    - `200 OK`: Customer details
    - `404 Not Found`: Customer not found

### Get Customer ID by Account Number
- **URL:** `/v1/customers/getid/{accountNumber}`
- **Method:** `GET`
- **Description:** Retrieve a customer's ID using their account number.
- **Response:**
    - `200 OK`: Customer ID
    - `404 Not Found`: Customer not found

### Get Customer by Account Number
- **URL:** `/v1/customers/getaccount/{accountNumber}`
- **Method:** `GET`
- **Description:** Retrieve a customer's details using their account number.
- **Response:**
    - `200 OK`: Customer details
    - `404 Not Found`: Customer not found

---

## Asset Endpoints

### Create Asset
- **URL:** `/api/asset/createAsset`
- **Method:** `POST`
- **Description:** Create a new asset.
- **Request Body:**
```json
{
  "assetName": "AAPL",
  "initialSize": 1000,
  "initialPrice": 150.0
}
```
- **Response:**
    - `200 OK`: Asset created successfully
    - `400 Bad Request`: Duplicate asset name

### Delete Asset
- **URL:** `/api/asset/{id}`
- **Method:** `DELETE`
- **Description:** Delete asset by ID (only if not in use).
- **Response:**
    - `200 OK`: Asset deleted successfully
    - `404 Not Found`: Asset not found

### Show Asset by ID
- **URL:** `/api/asset/{id}`
- **Method:** `GET`
- **Description:** Retrieve asset by ID.
- **Response:**
    - `200 OK`: Asset details
    - `404 Not Found`: Asset not found

### List All Assets
- **URL:** `/api/asset/assets/all`
- **Method:** `GET`
- **Description:** List all assets.
- **Response:**
    - `200 OK`: List of assets

### List Customer Assets
- **URL:** `/api/asset/assets/{id}`
- **Method:** `GET`
- **Description:** List all assets owned by a customer.
- **Response:**
    - `200 OK`: List of customer assets

---

## Order Endpoints

### Create Order
- **URL:** `/api/orders/create`
- **Method:** `POST`
- **Description:** Create a new order (BUY or SELL).
- **Request Body:**
```json
{
  "customerId": 1,
  "assetName": "AAPL",
  "side": "BUY",
  "size": 10
}
```
- **Response:**
    - `200 OK`: Order created successfully with PENDING status
    - `400 Bad Request`: Insufficient credit or invalid request

### List Orders
- **URL:** `/api/orders/list/{id}?startDate=01-09-2025&endDate=04-09-2025`
- **Method:** `GET`
- **Description:** List all orders for a customer within a date range.
- **Response:**
    - `200 OK`: List of orders

### Show Order by ID
- **URL:** `/api/orders/show/{id}`
- **Method:** `GET`
- **Description:** Retrieve order details by ID.
- **Response:**
    - `200 OK`: Order details
    - `404 Not Found`: Order not found

### Delete Order
- **URL:** `/api/orders/delete/{orderId}/{id}`
- **Method:** `DELETE`
- **Description:** Cancel a pending order (refund credit if BUY).
- **Response:**
    - `200 OK`: Order cancelled successfully
    - `400 Bad Request`: Only pending orders can be deleted

---

## Admin Order Matching Endpoints

### Match Single Order
- **URL:** `/admin/orders/match/{id}`
- **Method:** `POST`
- **Description:** Match a single order manually.
- **Response:**
    - `200 OK`: Order matched successfully
    - `400 Bad Request`: Failed to match order

### Start Parallel Matching
- **URL:** `/admin/orders/start-matching`
- **Method:** `POST`
- **Description:** Start background worker for processing pending orders.
- **Response:**
    - `200 OK`: Parallel matching started

### Stop Parallel Matching
- **URL:** `/admin/orders/stop-matching`
- **Method:** `POST`
- **Description:** Stop background order matching worker.
- **Response:**
    - `200 OK`: Parallel matching stopped

---

## Notes

- All endpoints (except authentication) require a valid JWT token in the `Authorization` header.
- Example header:
```bash
Authorization: Bearer <your-jwt-token>
```
- Errors follow a consistent format with a `message` field describing the error.
