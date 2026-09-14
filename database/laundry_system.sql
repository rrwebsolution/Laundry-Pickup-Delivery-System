-- ============================================================
-- Laundry Pickup and Delivery Management System
-- Database Schema and Seed Data
-- ============================================================

DROP DATABASE IF EXISTS laundry_system;
CREATE DATABASE laundry_system;
USE laundry_system;

-- ------------------------------------------------------------
-- Table: users
-- Stores login accounts for Administrator, Staff, and Rider roles.
-- Passwords are stored as SHA-256 hashes (hex string), never plain text.
-- ------------------------------------------------------------
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('Administrator', 'Staff', 'Rider') NOT NULL,
    status ENUM('Active', 'Inactive') NOT NULL DEFAULT 'Active',
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: customers
-- ------------------------------------------------------------
CREATE TABLE customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address VARCHAR(255) NOT NULL,
    date_registered TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: laundry_services
-- ------------------------------------------------------------
CREATE TABLE laundry_services (
    service_id INT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    price DECIMAL(10,2) NOT NULL,
    pricing_type ENUM('Per Kilogram', 'Per Piece') NOT NULL
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: laundry_orders
-- ------------------------------------------------------------
CREATE TABLE laundry_orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    service_id INT NOT NULL,
    weight_quantity DECIMAL(10,2) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    pickup_fee DECIMAL(10,2) NOT NULL DEFAULT 0,
    delivery_fee DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL,
    order_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expected_completion_date DATE,
    status ENUM('Pending','Scheduled for Pickup','Picked Up','Washing','Drying',
                'Folding','Ready for Delivery','Out for Delivery','Delivered','Cancelled')
                NOT NULL DEFAULT 'Pending',
    notes VARCHAR(500),
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_order_service FOREIGN KEY (service_id) REFERENCES laundry_services(service_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: pickup_deliveries
-- ------------------------------------------------------------
CREATE TABLE pickup_deliveries (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    type ENUM('Pickup','Delivery') NOT NULL,
    address VARCHAR(255) NOT NULL,
    assigned_rider_id INT,
    scheduled_date DATE NOT NULL,
    scheduled_time TIME NOT NULL,
    status ENUM('Scheduled','In Progress','Completed','Cancelled') NOT NULL DEFAULT 'Scheduled',
    notes VARCHAR(500),
    CONSTRAINT fk_pd_order FOREIGN KEY (order_id) REFERENCES laundry_orders(order_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_pd_rider FOREIGN KEY (assigned_rider_id) REFERENCES users(user_id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: payments
-- ------------------------------------------------------------
CREATE TABLE payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    amount_paid DECIMAL(10,2) NOT NULL,
    change_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    payment_method ENUM('Cash','GCash','Bank Transfer') NOT NULL,
    payment_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_status ENUM('Unpaid','Partially Paid','Paid') NOT NULL DEFAULT 'Unpaid',
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES laundry_orders(order_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- Seed Data
-- ============================================================

-- Default accounts (password_hash = SHA-256 hex digest of the plain password)
-- admin   / admin123
-- staff1  / staff123
-- rider1  / rider123
INSERT INTO users (full_name, username, password_hash, role, status) VALUES
('System Administrator', 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrator', 'Active'),
('Staff One', 'staff1', '10176e7b7b24d317acfcf8d2064cfd2f24e154f7b5a96603077d5ef813d6a6b6', 'Staff', 'Active'),
('Rider One', 'rider1', 'e85978062768502c68bfd953d2d3793cf799b6abe697543512d8bc41bc60e210', 'Rider', 'Active');

-- Sample services
INSERT INTO laundry_services (service_name, description, price, pricing_type) VALUES
('Wash', 'Basic wash only', 60.00, 'Per Kilogram'),
('Wash + Dry', 'Wash and machine dry', 90.00, 'Per Kilogram'),
('Wash + Dry + Fold', 'Full service wash, dry, and fold', 120.00, 'Per Kilogram'),
('Dry Cleaning', 'Professional dry cleaning', 150.00, 'Per Piece'),
('Ironing', 'Pressing and ironing service', 20.00, 'Per Piece');

-- Sample customers
INSERT INTO customers (full_name, contact_number, email, address) VALUES
('Juan Dela Cruz', '09171234567', 'juan.delacruz@example.com', '123 Rizal St, Quezon City'),
('Maria Santos', '09281234567', 'maria.santos@example.com', '456 Bonifacio Ave, Makati City'),
('Pedro Reyes', '09391234567', 'pedro.reyes@example.com', '789 Mabini St, Pasig City');
