-- Create the database and user
CREATE DATABASE testing;

\c testing

-- -- Create 'users' table
-- CREATE TABLE IF NOT EXISTS users (
--                                      id BIGSERIAL PRIMARY KEY,
--                                      account_number VARCHAR(36) NOT NULL UNIQUE,
--     username VARCHAR(255) NOT NULL UNIQUE,
--     password VARCHAR(255) NOT NULL,
--     roles VARCHAR(255) NOT NULL -- Storing roles as a comma-separated string
--     );
--
-- -- Create 'customers' table
-- CREATE TABLE IF NOT EXISTS customers (
--                                          id BIGSERIAL PRIMARY KEY,
--                                          name VARCHAR(255) NOT NULL,
--     surname VARCHAR(255) NOT NULL,
--     credit_limit BIGINT DEFAULT 10000,
--     used_credit_limit BIGINT DEFAULT 0,
--     user_id BIGINT UNIQUE NOT NULL, -- Foreign key to users
--     CONSTRAINT fk_customer_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
--     );
-- -- Insert roles directly into the 'users' table (the roles column will hold a string value)
-- -- Insert a user with the ADMIN role
-- -- Insert a user into the 'users' table
-- INSERT INTO users (account_number, username, password, roles)
-- VALUES
--     ('1c43080b-c30c-4271-83f8-c9f372bdc17e', 'admin_user', '$2a$10$UyfbuHXk6QHZ./FkyE75v.dabF.4HdC5FrtCrzxPs.uE2UOJp9VTG', '{ADMIN,CUSTOMER}');
--
-- -- Insert a corresponding customer into the 'customers' table, using the user_id from the 'users' table
-- INSERT INTO customers (name, surname, credit_limit, used_credit_limit, user_id)
-- VALUES
--     ('Admin', 'User', 10000, 0, (SELECT id FROM users WHERE username = 'admin_user'));
