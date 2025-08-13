-- Tabela de usuários
CREATE TABLE tbl_app_user
(
    id            SERIAL PRIMARY KEY,
    username      VARCHAR(50) UNIQUE  NOT NULL,
    email         VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de categorias (deve vir antes de transações por causa da FK)
CREATE TABLE tbl_category
(
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(50) NOT NULL UNIQUE,
    icon      VARCHAR(50),
    color     VARCHAR(7),
    parent_id INTEGER REFERENCES tbl_category (id) -- Auto-relacionamento para hierarquia
);

-- Tabela de contas bancárias
CREATE TABLE tbl_account
(
    account_id              SERIAL PRIMARY KEY,
    account_name            VARCHAR(100) NOT NULL,
    account_description     VARCHAR(255),
    account_current_balance INTEGER,                    -- Removido NOT NULL para alinhar com a entidade
    account_currency        VARCHAR(3)   NOT NULL    DEFAULT 'BRL',
    user_id                 INTEGER      NOT NULL REFERENCES tbl_app_user (id),
    created_at              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE
);

-- Tabela principal de transações
CREATE TABLE tbl_transaction
(
    id                         SERIAL PRIMARY KEY,
    description                VARCHAR(255)   NOT NULL,
    amount                     INTEGER NOT NULL CHECK (amount > 0),
    down_payment               INTEGER,
    transaction_type           VARCHAR(20)    NOT NULL CHECK (transaction_type IN ('EXPENSE', 'INCOME')),
    transaction_operation_type VARCHAR(20)    NOT NULL CHECK (transaction_operation_type IN
                                                              ('INITIAL_BALANCE', 'SALARY', 'DEPOSIT', 'WITHDRAWAL',
                                                               'TRANSFER_IN', 'TRANSFER_OUT', 'INTEREST', 'FEE',
                                                               'ADJUSTMENT', 'REFUND', 'PAYMENT', 'REWARD',
                                                               'LOAN_PAYMENT', 'LOAN_DISBURSEMENT', 'DIVIDEND', 'TAX',
                                                               'OTHER')),
    payment_status             VARCHAR(10)    NOT NULL  DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PAID', 'FAILED')),
    category_id                INTEGER        NOT NULL REFERENCES tbl_category (id),
    due_date                   DATE           NOT NULL,
    created_at                 TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMP WITH TIME ZONE,
    notes                      TEXT,
    recurrence_pattern         VARCHAR(20) CHECK (recurrence_pattern IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')),
    installment_info           JSONB,
    user_id                    INTEGER        NOT NULL REFERENCES tbl_app_user (id),
    account_id                 INTEGER        NOT NULL REFERENCES tbl_account (account_id)
);

-- Tabela de orçamentos
CREATE TABLE tbl_budget
(
    id           SERIAL PRIMARY KEY,
    user_id      INTEGER        NOT NULL REFERENCES tbl_app_user (id),
    category_id  INTEGER REFERENCES tbl_category (id),
    amount       INTEGER NOT NULL,
    budget_month DATE           NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de histórico de saldo das contas
CREATE TABLE tbl_account_balance_history
(
    balance_history_id     SERIAL PRIMARY KEY,
    account_id             INTEGER        NOT NULL REFERENCES tbl_account (account_id),
    transaction_id         INTEGER REFERENCES tbl_transaction (id),
    history_amount         INTEGER NOT NULL,
    history_operation_type VARCHAR(20)    NOT NULL CHECK (history_operation_type IN
                                                          ('INITIAL_BALANCE', 'SALARY', 'DEPOSIT', 'WITHDRAWAL',
                                                           'TRANSFER_IN', 'TRANSFER_OUT', 'INTEREST', 'FEE',
                                                           'ADJUSTMENT', 'REFUND', 'PAYMENT', 'REWARD', 'LOAN_PAYMENT',
                                                           'LOAN_DISBURSEMENT', 'DIVIDEND', 'TAX', 'OTHER')),
    balance_timestamp      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Índices para otimização de consultas

-- Índices para tbl_app_user
CREATE INDEX idx_app_user_username ON tbl_app_user (username);
CREATE INDEX idx_app_user_email ON tbl_app_user (email);

-- Índices para tbl_category
CREATE INDEX idx_category_parent ON tbl_category (parent_id);

-- Índices para tbl_account
CREATE INDEX idx_account_user ON tbl_account (user_id);
CREATE INDEX idx_account_currency ON tbl_account (account_currency);

-- Índices para tbl_transaction
CREATE INDEX idx_transaction_user_date ON tbl_transaction (user_id, due_date);
CREATE INDEX idx_transaction_category ON tbl_transaction (category_id);
CREATE INDEX idx_transaction_account ON tbl_transaction (account_id);
CREATE INDEX idx_transaction_status ON tbl_transaction (payment_status);
CREATE INDEX idx_transaction_type ON tbl_transaction (transaction_type);
CREATE INDEX idx_transaction_operation_type ON tbl_transaction (transaction_operation_type);
CREATE INDEX idx_transaction_recurrence ON tbl_transaction (recurrence_pattern);
CREATE INDEX idx_transaction_installment ON tbl_transaction USING GIN (installment_info);
CREATE INDEX idx_transaction_created_at ON tbl_transaction (created_at);
CREATE INDEX idx_transaction_due_date ON tbl_transaction (due_date);

-- Índices para tbl_budget
CREATE INDEX idx_budget_user_month ON tbl_budget (user_id, budget_month);
CREATE INDEX idx_budget_category ON tbl_budget (category_id);

-- Índices para tbl_account_balance_history
CREATE INDEX idx_balance_history_account ON tbl_account_balance_history (account_id);
CREATE INDEX idx_balance_history_transaction ON tbl_account_balance_history (transaction_id);
CREATE INDEX idx_balance_history_timestamp ON tbl_account_balance_history (balance_timestamp);
CREATE INDEX idx_balance_history_operation_type ON tbl_account_balance_history (history_operation_type);
CREATE INDEX idx_balance_history_account_timestamp ON tbl_account_balance_history (account_id, balance_timestamp);

-- Índices compostos para consultas frequentes
CREATE INDEX idx_transaction_user_status_date ON tbl_transaction (user_id, payment_status, due_date);
CREATE INDEX idx_transaction_account_type_date ON tbl_transaction (account_id, transaction_type, due_date);
CREATE INDEX idx_balance_history_account_operation_timestamp ON tbl_account_balance_history (account_id, history_operation_type, balance_timestamp);
