-- Tabela de usuários
CREATE TABLE tbl_app_user
(
    id            SERIAL PRIMARY KEY,
    username      VARCHAR(50) UNIQUE  NOT NULL,
    email         VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    password_salt VARCHAR(255)        NOT NULL,
    path_avatar   VARCHAR(255),
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE
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

-- Tabela de instituições bancárias
CREATE TABLE tbl_bank_institution
(
    bank_institution_id   SERIAL PRIMARY KEY,
    bank_institution_name VARCHAR(100) NOT NULL,
    bank_institution_logo VARCHAR(255)
);

-- Tabela de contas bancárias
CREATE TABLE tbl_account
(
    account_id                     SERIAL PRIMARY KEY,
    account_name                   VARCHAR(100) NOT NULL,
    account_description            VARCHAR(255),
    bank_institution_id            INTEGER REFERENCES tbl_bank_institution (bank_institution_id),
    account_type                   VARCHAR(50),
    account_credit_limit           INTEGER,
    account_statement_closing_date INTEGER,
    account_payment_due_date       INTEGER,
    account_current_balance        INTEGER,
    account_currency               VARCHAR(3)   NOT NULL DEFAULT 'BRL',
    user_id                        INTEGER      NOT NULL REFERENCES tbl_app_user (id),
    created_at                     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at                     TIMESTAMP WITH TIME ZONE
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
    paid_at                    DATE,
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

-- Tabela para armazenar tokens de redefinição de senha
CREATE TABLE tbl_password_reset_token
(
    reset_token_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    reset_token    VARCHAR(255),
    user_id        INTEGER                                 NOT NULL,
    expiry_date    TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_tbl_password_reset_token PRIMARY KEY (reset_token_id)
);

ALTER TABLE tbl_password_reset_token
    ADD CONSTRAINT FK_TBL_PASSWORD_RESET_TOKEN_ON_USER FOREIGN KEY (user_id) REFERENCES tbl_app_user (id);

-- Índices para otimização de consultas

-- Índices para tbl_app_user
CREATE INDEX idx_app_user_username ON tbl_app_user (username);
CREATE INDEX idx_app_user_email ON tbl_app_user (email);

-- Índices para tbl_category
CREATE INDEX idx_category_parent ON tbl_category (parent_id);

-- Índices para tbl_bank_institution
CREATE INDEX idx_bank_institution_name ON tbl_bank_institution (bank_institution_name);

-- Índices para tbl_account
CREATE INDEX idx_account_user ON tbl_account (user_id);
CREATE INDEX idx_account_bank_institution ON tbl_account (bank_institution_id);
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

-- Índices para tbl_password_reset_token
CREATE INDEX idx_password_reset_token_user ON tbl_password_reset_token (user_id);

-- Comentários para documentação das tabelas e colunas

-- Comentarios para tbl_password_reset_token
COMMENT ON TABLE tbl_password_reset_token IS 'Tabela para armazenar tokens de redefinição de senha dos usuários';
COMMENT ON COLUMN tbl_password_reset_token.reset_token_id IS 'Identificador único do token de redefinição';
COMMENT ON COLUMN tbl_password_reset_token.reset_token IS 'Token de redefinição de senha (string única e segura)';
COMMENT ON COLUMN tbl_password_reset_token.user_id IS 'ID do usuário associado ao token de redefinição';
COMMENT ON COLUMN tbl_password_reset_token.expiry_date IS 'Data e hora de expiração do token';

-- Comentários para tbl_bank_institution
COMMENT ON TABLE tbl_bank_institution IS 'Tabela de instituições bancárias e fintechs para normalização dos dados das contas';
COMMENT ON COLUMN tbl_bank_institution.bank_institution_id IS 'Identificador único da instituição bancária';
COMMENT ON COLUMN tbl_bank_institution.bank_institution_name IS 'Nome da instituição bancária (ex: Banco do Brasil, Nubank, Inter)';
COMMENT ON COLUMN tbl_bank_institution.bank_institution_logo IS 'URL ou caminho para o logotipo da instituição para exibição na interface';

-- Comentários para tbl_app_user
COMMENT ON TABLE tbl_app_user IS 'Tabela de usuários do sistema';
COMMENT ON COLUMN tbl_app_user.id IS 'Identificador único do usuário';
COMMENT ON COLUMN tbl_app_user.username IS 'Nome de usuário único para login (mínimo 6 caracteres)';
COMMENT ON COLUMN tbl_app_user.email IS 'Email único do usuário para comunicação';
COMMENT ON COLUMN tbl_app_user.password_hash IS 'Hash da senha do usuário (mínimo 6 caracteres)';
COMMENT ON COLUMN tbl_app_user.path_avatar IS 'Caminho ou URL da imagem de avatar do usuário';
COMMENT ON COLUMN tbl_app_user.created_at IS 'Data e hora de criação do usuário no sistema';

-- Comentários para tbl_category
COMMENT ON TABLE tbl_category IS 'Tabela de categorias de transações com suporte hierárquico (categoria pai/filha)';
COMMENT ON COLUMN tbl_category.id IS 'Identificador único da categoria';
COMMENT ON COLUMN tbl_category.name IS 'Nome único da categoria (ex: Alimentação, Transporte)';
COMMENT ON COLUMN tbl_category.icon IS 'Nome do ícone para exibição na interface do usuário';
COMMENT ON COLUMN tbl_category.color IS 'Código de cor hexadecimal (#RRGGBB) para identificação visual da categoria';
COMMENT ON COLUMN tbl_category.parent_id IS 'ID da categoria pai para hierarquia (subcategorias como Delivery sob Alimentação)';

-- Comentários para tbl_account
COMMENT ON TABLE tbl_account IS 'Tabela de contas bancárias, cartões de crédito e outras contas financeiras do usuário';
COMMENT ON COLUMN tbl_account.account_id IS 'Identificador único da conta financeira';
COMMENT ON COLUMN tbl_account.account_name IS 'Nome identificador da conta (ex: Conta Corrente Banco X, Cartão Y)';
COMMENT ON COLUMN tbl_account.account_description IS 'Descrição adicional ou observações sobre a conta';
COMMENT ON COLUMN tbl_account.bank_institution_id IS 'ID da instituição financeira (banco, fintech, etc.) associada à conta';
COMMENT ON COLUMN tbl_account.account_type IS 'Tipo da conta: CHECKING (corrente), SAVINGS (poupança), CREDIT_CARD, etc.';
COMMENT ON COLUMN tbl_account.account_credit_limit IS 'Limite de crédito da conta em centavos (aplicável a cartões de crédito)';
COMMENT ON COLUMN tbl_account.account_statement_closing_date IS 'Dia do mês de fechamento da fatura (1-31, para cartões de crédito)';
COMMENT ON COLUMN tbl_account.account_payment_due_date IS 'Dia do mês de vencimento do pagamento (1-31, para cartões de crédito)';
COMMENT ON COLUMN tbl_account.account_current_balance IS 'Saldo atual da conta em centavos (valores negativos para dívidas)';
COMMENT ON COLUMN tbl_account.account_currency IS 'Código ISO 4217 da moeda da conta (BRL, USD, EUR, etc.)';
COMMENT ON COLUMN tbl_account.user_id IS 'ID do usuário proprietário da conta financeira';
COMMENT ON COLUMN tbl_account.created_at IS 'Data e hora de criação do registro da conta no sistema';
COMMENT ON COLUMN tbl_account.updated_at IS 'Data e hora da última atualização dos dados da conta';

-- Comentários para tbl_transaction
COMMENT ON TABLE tbl_transaction IS 'Tabela principal de transações financeiras (receitas, despesas, transferências)';
COMMENT ON COLUMN tbl_transaction.id IS 'Identificador único da transação financeira';
COMMENT ON COLUMN tbl_transaction.description IS 'Descrição da transação (ex: Compra no supermercado, Salário)';
COMMENT ON COLUMN tbl_transaction.amount IS 'Valor principal da transação em centavos (sempre positivo)';
COMMENT ON COLUMN tbl_transaction.down_payment IS 'Valor de entrada para compras parceladas em centavos (opcional)';
COMMENT ON COLUMN tbl_transaction.transaction_type IS 'Tipo básico da transação: EXPENSE (despesa) ou INCOME (receita)';
COMMENT ON COLUMN tbl_transaction.transaction_operation_type IS 'Tipo específico da operação: SALARY, DEPOSIT, WITHDRAWAL, PAYMENT, etc.';
COMMENT ON COLUMN tbl_transaction.payment_status IS 'Status do pagamento: PENDING (pendente), PAID (pago), FAILED (falhado)';
COMMENT ON COLUMN tbl_transaction.category_id IS 'ID da categoria da transação para classificação e relatórios';
COMMENT ON COLUMN tbl_transaction.due_date IS 'Data de vencimento ou execução planejada da transação';
COMMENT ON COLUMN tbl_transaction.paid_at IS 'Data efetiva do pagamento da transação (preenchida quando status = PAID)';
COMMENT ON COLUMN tbl_transaction.created_at IS 'Data e hora de criação do registro da transação';
COMMENT ON COLUMN tbl_transaction.updated_at IS 'Data e hora da última modificação da transação';
COMMENT ON COLUMN tbl_transaction.notes IS 'Observações adicionais, detalhes ou comentários sobre a transação';
COMMENT ON COLUMN tbl_transaction.recurrence_pattern IS 'Padrão de recorrência: DAILY (diária), WEEKLY (semanal), MONTHLY (mensal), YEARLY (anual)';
COMMENT ON COLUMN tbl_transaction.installment_info IS 'Informações de parcelamento em formato JSON: número de parcelas, valor, parcela atual';
COMMENT ON COLUMN tbl_transaction.user_id IS 'ID do usuário proprietário da transação';
COMMENT ON COLUMN tbl_transaction.account_id IS 'ID da conta financeira associada à transação';

-- Comentários para tbl_budget
COMMENT ON TABLE tbl_budget IS 'Tabela de orçamentos planejados por categoria e período mensal';
COMMENT ON COLUMN tbl_budget.id IS 'Identificador único do registro de orçamento';
COMMENT ON COLUMN tbl_budget.user_id IS 'ID do usuário proprietário do orçamento';
COMMENT ON COLUMN tbl_budget.category_id IS 'ID da categoria do orçamento (null para orçamento geral/total)';
COMMENT ON COLUMN tbl_budget.amount IS 'Valor planejado do orçamento em centavos para o período';
COMMENT ON COLUMN tbl_budget.budget_month IS 'Mês de referência do orçamento (formato YYYY-MM-01)';
COMMENT ON COLUMN tbl_budget.created_at IS 'Data e hora de criação do planejamento orçamentário';

-- Comentários para tbl_account_balance_history
COMMENT ON TABLE tbl_account_balance_history IS 'Histórico detalhado de todas as movimentações e alterações de saldo das contas';
COMMENT ON COLUMN tbl_account_balance_history.balance_history_id IS 'Identificador único do registro de histórico de saldo';
COMMENT ON COLUMN tbl_account_balance_history.account_id IS 'ID da conta financeira associada ao histórico';
COMMENT ON COLUMN tbl_account_balance_history.transaction_id IS 'ID da transação que gerou a movimentação (null para ajustes manuais)';
COMMENT ON COLUMN tbl_account_balance_history.history_amount IS 'Valor da movimentação em centavos (positivo para crédito, negativo para débito)';
COMMENT ON COLUMN tbl_account_balance_history.history_operation_type IS 'Tipo da operação que gerou a movimentação: DEPOSIT, WITHDRAWAL, TRANSFER, etc.';
COMMENT ON COLUMN tbl_account_balance_history.balance_timestamp IS 'Data e hora exata da movimentação financeira';



--- Insere categorias padrão
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (1, 'Alimentacao', 'fas fa-utensils', '#F59E0B', null);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (2, 'Restaurante', 'fas fa-utensils', '#D97706', 1);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (3, 'Delivery', 'fas fa-utensils', '#D97706', 1);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (4, 'Cafeteria', 'fas fa-utensils', '#D97706', 1);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (5, 'Padaria', 'fas fa-utensils', '#D97706', 1);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (6, 'Assinaturas/Servicos', 'fas fa-mobile-alt', '#3B82F6', null);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (7, 'Televisao', 'fas fa-bolt', '#3B82F6', 6);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (8, 'Telefone/Celular', 'fas fa-mobile-alt', '#3B82F6', 6);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (9, 'Streamings', 'fas fa-mobile-alt', '#3B82F6', 6);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (10, 'Aplicativos', 'fas fa-mobile-alt', '#3B82F6', 6);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (11, 'Jogos', 'fas fa-gamepad', '#3B82F6', 6);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (12, 'Cuidado Pessoal', 'fas fa-heart', '#DC2626', null);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (13, 'Cabeleireiro/Barbeiro', 'fas fa-heart', '#DC2626', 12);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (14, 'Higiene Pessoal', 'fas fa-heart', '#DC2626', 12);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (15, 'Dividas/Emprestimos', 'fas fa-money-bill', '#EF4444', null);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (16, 'Cartao de Credito', 'fas fa-money-bill', '#EF4444', 15);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (17, 'Renegociacao', 'fas fa-money-bill', '#EF4444', 15);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (18, 'Financiamento', 'fas fa-money-bill', '#EF4444', 15);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (19, 'Emprestimo Consignado', 'fas fa-money-bill', '#EF4444', 15);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (20, 'Emprestimo', 'fas fa-money-bill', '#EF4444', 15);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (21, 'Supermercado', 'fas fa-shopping-cart', '#F59E0B', null);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (22, 'Moradia', 'fas fa-home', '#10B981', null);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (23, 'Agua', 'fas fa-home', '#3B82F6', 22);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (24, 'Internet', 'fas fa-bolt', '#3B82F6', 22);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (25, 'Aluguel/Prestacao', 'fas fa-home', '#3B82F6', 22);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (26, 'Luz', 'fas fa-bolt', '#F59E0B', 22);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (27, 'Gas', 'fas fa-bolt', '#DC2626', 22);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (28, 'Salario', 'fas fa-money-bill', '#10B981', null);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (29, 'Saude', 'fas fa-heart', '#EC4899', null);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (30, 'Academia', 'fas fa-heart', '#64748B', 29);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (31, 'Farmacia', 'fas fa-heart', '#EF4444', 29);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (32, 'Exames', 'fas fa-heart', '#6366F1', 29);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (33, 'Transporte', 'fas fa-car', '#10B981', null);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (34, 'Combustivel', 'fas fa-gas-pump', '#DC2626', 33);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (35, 'Estacionamento', 'fas fa-car', '#3B82F6', 33);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (36, 'Manutencao', 'fas fa-car', '#64748B', 33);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (37, 'Lavacao', 'fas fa-car', '#06B6D4', 33);
INSERT INTO tbl_category (id, name, icon, color, parent_id) VALUES (38, 'Viagens', 'fas fa-plane', '#3B82F6', null);
