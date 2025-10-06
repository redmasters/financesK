#!/bin/bash

# =====================================================
# SCRIPT DE RESTAURAÇÃO POSTGRESQL DOCKER
# =====================================================

CONTAINER_NAME="financesK-postgres"
DB_NAME="financesk_db"
DB_USER="financesk_admin"
BACKUP_DIR="$HOME/docker/postgres"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}🔄 Script de Restauração PostgreSQL Docker${NC}"

# Verificar se foi fornecido o arquivo de backup
if [ $# -eq 0 ]; then
    echo -e "${YELLOW}📋 Backups disponíveis:${NC}"
    ls -la "$BACKUP_DIR"/backup_financesk_*.sql* 2>/dev/null
    echo
    echo -e "${YELLOW}💡 Uso: $0 <arquivo_backup>${NC}"
    echo -e "${YELLOW}   Exemplo: $0 backup_financesk_20251004_143022.sql.gz${NC}"
    exit 1
fi

BACKUP_FILE="$1"
FULL_PATH="$BACKUP_DIR/$BACKUP_FILE"

# Verificar se o arquivo existe
if [ ! -f "$FULL_PATH" ]; then
    echo -e "${RED}❌ Arquivo não encontrado: $FULL_PATH${NC}"
    exit 1
fi

# Verificar se o container está rodando
if ! docker ps | grep -q "$CONTAINER_NAME"; then
    echo -e "${RED}❌ Container $CONTAINER_NAME não está rodando!${NC}"
    exit 1
fi

echo -e "${YELLOW}⚠️  ATENÇÃO: Esta operação irá SOBRESCREVER o banco atual!${NC}"
echo -e "${YELLOW}📄 Arquivo: $FULL_PATH${NC}"
read -p "Deseja continuar? (y/N): " -n 1 -r
echo

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}❌ Operação cancelada pelo usuário${NC}"
    exit 0
fi

echo -e "${YELLOW}🔄 Restaurando backup...${NC}"

# Se o arquivo estiver comprimido, descomprimir temporariamente
if [[ "$BACKUP_FILE" == *.gz ]]; then
    echo -e "${YELLOW}📦 Descomprimindo arquivo...${NC}"
    gunzip -c "$FULL_PATH" | docker exec -i "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME"
else
    # Arquivo não comprimido
    docker exec -i "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" < "$FULL_PATH"
fi

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Backup restaurado com sucesso!${NC}"
else
    echo -e "${RED}❌ Erro ao restaurar backup!${NC}"
    exit 1
fi
