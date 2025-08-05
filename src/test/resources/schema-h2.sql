-- Tabela de usuários
CREATE TABLE tbl_app_user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de categorias
CREATE TABLE tbl_category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(10) NOT NULL,
    icon VARCHAR(50),
    color VARCHAR(7),
    parent_id INT,
    FOREIGN KEY (parent_id) REFERENCES tbl_category(id)
);

-- Tabela principal de transações
CREATE TABLE tbl_transaction (
    id INT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    type VARCHAR(10) NOT NULL,
    category_id INT NOT NULL,
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    recurrence_pattern VARCHAR(20),
    installment_info VARCHAR(2000), -- H2 não suporta JSONB, use VARCHAR
    user_id INT NOT NULL,
    FOREIGN KEY (category_id) REFERENCES tbl_category(id),
    FOREIGN KEY (user_id) REFERENCES tbl_app_user(id)
);

-- Tabela de orçamentos
CREATE TABLE tbl_budget (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category_id INT,
    amount DECIMAL(10, 2) NOT NULL,
    "month" DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES tbl_app_user(id),
    FOREIGN KEY (category_id) REFERENCES tbl_category(id)
);

-- Índices para otimização
CREATE INDEX idx_transaction_user_date ON tbl_transaction(user_id, transaction_date);
CREATE INDEX idx_transaction_category ON tbl_transaction(category_id);
CREATE INDEX idx_category_type ON tbl_category(type);
CREATE INDEX idx_budget_user_month ON tbl_budget(user_id, "month");
