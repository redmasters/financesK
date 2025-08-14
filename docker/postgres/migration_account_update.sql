-- Script de migração para atualizar a tabela tbl_account
-- Adiciona as colunas que estão faltando na entidade Account

-- Criar tabela tbl_bank_institution se não existir
CREATE TABLE IF NOT EXISTS tbl_bank_institution
(
    bank_institution_id   SERIAL PRIMARY KEY,
    bank_institution_name VARCHAR(100) NOT NULL,
    bank_institution_logo VARCHAR(255)
);

-- Adicionar coluna account_description se não existir
ALTER TABLE tbl_account
ADD COLUMN IF NOT EXISTS account_description VARCHAR(255);

-- Remover a coluna account_institution antiga se existir (migração de dados)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name='tbl_account' AND column_name='account_institution') THEN
        -- Primeiro, migrar dados existentes se necessário
        -- (este passo pode ser personalizado conforme a necessidade)
        ALTER TABLE tbl_account DROP COLUMN account_institution;
    END IF;
END $$;

-- Adicionar coluna bank_institution_id como foreign key
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='tbl_account' AND column_name='bank_institution_id') THEN
        ALTER TABLE tbl_account ADD COLUMN bank_institution_id INTEGER;
        ALTER TABLE tbl_account ADD CONSTRAINT fk_account_bank_institution
            FOREIGN KEY (bank_institution_id) REFERENCES tbl_bank_institution (bank_institution_id);
    END IF;
END $$;

-- Adicionar coluna account_type
ALTER TABLE tbl_account
ADD COLUMN IF NOT EXISTS account_type VARCHAR(50);

-- Adicionar coluna account_credit_limit
ALTER TABLE tbl_account
ADD COLUMN IF NOT EXISTS account_credit_limit INTEGER;

-- Adicionar coluna account_statement_closing_date
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='tbl_account' AND column_name='account_statement_closing_date') THEN
        ALTER TABLE tbl_account ADD COLUMN account_statement_closing_date INTEGER;
    ELSE
        -- Se já existe, alterar o tipo de TIMESTAMP para INTEGER
        ALTER TABLE tbl_account ALTER COLUMN account_statement_closing_date TYPE INTEGER;
    END IF;
END $$;

-- Adicionar coluna account_payment_due_date
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='tbl_account' AND column_name='account_payment_due_date') THEN
        ALTER TABLE tbl_account ADD COLUMN account_payment_due_date INTEGER;
    ELSE
        -- Se já existe, alterar o tipo de TIMESTAMP para INTEGER
        ALTER TABLE tbl_account ALTER COLUMN account_payment_due_date TYPE INTEGER;
    END IF;
END $$;

-- Verificar se a coluna updated_at já existe antes de adicionar
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='tbl_account' AND column_name='updated_at') THEN
        ALTER TABLE tbl_account ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE;
    END IF;
END $$;

-- Criar índices adicionais para as novas colunas (se necessário)
CREATE INDEX IF NOT EXISTS idx_bank_institution_name ON tbl_bank_institution (bank_institution_name);
CREATE INDEX IF NOT EXISTS idx_account_bank_institution ON tbl_account (bank_institution_id);
CREATE INDEX IF NOT EXISTS idx_account_type ON tbl_account (account_type);
CREATE INDEX IF NOT EXISTS idx_account_statement_closing ON tbl_account (account_statement_closing_date);
CREATE INDEX IF NOT EXISTS idx_account_payment_due ON tbl_account (account_payment_due_date);

-- Comentários para documentação das tabelas e colunas

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
COMMENT ON COLUMN tbl_account.bank_institution_id IS 'ID da instituição financeira (banco, fintech, etc.) associada à conta';
COMMENT ON COLUMN tbl_account.account_type IS 'Tipo da conta: CHECKING (corrente), SAVINGS (poupança), CREDIT_CARD, etc.';
COMMENT ON COLUMN tbl_account.account_credit_limit IS 'Limite de crédito da conta em centavos (aplicável a cartões de crédito)';
COMMENT ON COLUMN tbl_account.account_statement_closing_date IS 'Dia do mês de fechamento da fatura (1-31, para cartões de crédito)';
COMMENT ON COLUMN tbl_account.account_payment_due_date IS 'Dia do mês de vencimento do pagamento (1-31, para cartões de crédito)';
COMMENT ON COLUMN tbl_account.account_description IS 'Descrição adicional ou observações sobre a conta';
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
