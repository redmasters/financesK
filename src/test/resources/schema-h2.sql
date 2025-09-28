-- Simple H2 schema for tests - only essential tables
DROP TABLE IF EXISTS tbl_account_balance_history;
DROP TABLE IF EXISTS tbl_transaction;
DROP TABLE IF EXISTS tbl_account;
DROP TABLE IF EXISTS tbl_app_user;
DROP TABLE IF EXISTS tbl_category;
DROP TABLE IF EXISTS tbl_bank_institution;

-- Tabela de usuários
CREATE TABLE tbl_app_user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255),
    path_avatar VARCHAR(500) DEFAULT '/default/avatar.png',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de instituições bancárias
CREATE TABLE tbl_bank_institution (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de contas
CREATE TABLE tbl_account (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    account_name VARCHAR(100) NOT NULL,
    account_description TEXT,
    account_current_balance INT NOT NULL DEFAULT 0,
    account_currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    user_id INT NOT NULL,
    bank_institution_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign keys after table creation
ALTER TABLE tbl_account ADD CONSTRAINT FK_account_user FOREIGN KEY (user_id) REFERENCES tbl_app_user(id);
ALTER TABLE tbl_account ADD CONSTRAINT FK_account_bank FOREIGN KEY (bank_institution_id) REFERENCES tbl_bank_institution(id);
