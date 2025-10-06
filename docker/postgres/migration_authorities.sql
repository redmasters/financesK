-- Migração para adicionar sistema de authorities aos usuários
-- Executar após criar a tabela tbl_app_user

-- Criar tabela para armazenar as authorities dos usuários
CREATE TABLE IF NOT EXISTS tbl_user_authorities (
    user_id INTEGER NOT NULL,
    authority VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, authority),
    FOREIGN KEY (user_id) REFERENCES tbl_app_user(id) ON DELETE CASCADE
);

-- Criar índice para melhorar performance das consultas
CREATE INDEX IF NOT EXISTS idx_user_authorities_user_id ON tbl_user_authorities(user_id);
CREATE INDEX IF NOT EXISTS idx_user_authorities_authority ON tbl_user_authorities(authority);

-- Inserir authority USER para todos os usuários existentes (se houver)
INSERT INTO tbl_user_authorities (user_id, authority)
SELECT id, 'USER'
FROM tbl_app_user
WHERE id NOT IN (SELECT DISTINCT user_id FROM tbl_user_authorities WHERE user_id IS NOT NULL);

-- Comentários sobre as authorities
-- USER: Usuário comum com acesso básico aos recursos
-- ADMIN: Administrador com acesso total ao sistema
-- CHANGE_PASSWORD_PRIVILEGE: Privilégio temporário concedido durante reset de senha
