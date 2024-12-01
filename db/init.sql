CREATE DATABASE testing;

CREATE USER customer WITH PASSWORD 'password';

GRANT ALL PRIVILEGES ON DATABASE testing TO customer;

CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     account_number VARCHAR(36) NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    roles VARCHAR(255) NOT NULL -- Storing the roles as a comma-separated string
    );

-- Create the 'customers' table if it doesn't exist
CREATE TABLE IF NOT EXISTS customers (
                                         id BIGINT PRIMARY KEY,
                                         name VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    credit_limit BIGINT DEFAULT 10000,
    used_credit_limit BIGINT DEFAULT 0,
    user_id BIGINT UNIQUE,
    CONSTRAINT fk_customer_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- Insert roles directly into 'users' table (the roles column will hold a string value)
-- Insert a user with the ADMIN role
INSERT INTO users (account_number, username, password, enabled, roles)
VALUES
    ('ccba07f2-07be-4205-bc5b-476cebf6799b', 'admin_user', '$2a$10$BuTVMD3noJbsBHRYMChtQe.H6LwpiQUGbiP4fmMdbgkKvwGydC2Tu', true, 'ADMIN,CUSTOMER');

-- Insert a corresponding customer for the admin user
INSERT INTO customers (id, name, surname, credit_limit, used_credit_limit, user_id)
VALUES
    (1, 'Admin', 'User', 10000, 0, 1);