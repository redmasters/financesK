#!/bin/bash

# =====================================================
# SCRIPT DE BACKUP POSTGRESQL DOCKER - VERSÃO COMPLETA
# =====================================================

# Configurações do banco (baseadas no docker-compose.yml)
CONTAINER_NAME="financesK-postgres"
DB_NAME="financesk_db"
DB_USER="financesk_admin"
BACKUP_DIR="$HOME/docker/postgres"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="backup_financesk_${TIMESTAMP}.sql"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🗃️  Iniciando backup do PostgreSQL...${NC}"

# Criar diretório de backup se não existir
mkdir -p "$BACKUP_DIR"

# Verificar se o container está rodando
if ! docker ps | grep -q "$CONTAINER_NAME"; then
    echo -e "${RED}❌ Erro: Container $CONTAINER_NAME não está rodando!${NC}"
    exit 1
fi

echo -e "${YELLOW}📦 Container encontrado: $CONTAINER_NAME${NC}"
echo -e "${YELLOW}🎯 Banco de dados: $DB_NAME${NC}"
echo -e "${YELLOW}📁 Diretório de backup: $BACKUP_DIR${NC}"

# Executar o backup
echo -e "${YELLOW}⏳ Executando pg_dump...${NC}"

docker exec "$CONTAINER_NAME" pg_dump \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    --verbose \
    --clean \
    --no-owner \
    --no-privileges \
    --format=plain > "$BACKUP_DIR/$BACKUP_FILE"

# Verificar se o backup foi bem-sucedido
if [ $? -eq 0 ]; then
    # Calcular tamanho do arquivo
    BACKUP_SIZE=$(du -h "$BACKUP_DIR/$BACKUP_FILE" | cut -f1)

    echo -e "${GREEN}✅ Backup concluído com sucesso!${NC}"
    echo -e "${GREEN}📄 Arquivo: $BACKUP_DIR/$BACKUP_FILE${NC}"
    echo -e "${GREEN}📏 Tamanho: $BACKUP_SIZE${NC}"

    # Comprimir o backup (opcional)
    echo -e "${YELLOW}🗜️  Comprimindo backup...${NC}"
    gzip "$BACKUP_DIR/$BACKUP_FILE"

    if [ $? -eq 0 ]; then
        COMPRESSED_SIZE=$(du -h "$BACKUP_DIR/$BACKUP_FILE.gz" | cut -f1)
        echo -e "${GREEN}✅ Backup comprimido: $BACKUP_FILE.gz${NC}"
        echo -e "${GREEN}📏 Tamanho comprimido: $COMPRESSED_SIZE${NC}"
    fi

    # Listar backups existentes
    echo -e "\n${YELLOW}📋 Backups existentes em $BACKUP_DIR:${NC}"
    ls -lh "$BACKUP_DIR"/backup_financesk_*.sql.gz 2>/dev/null || echo "Nenhum backup anterior encontrado"

else
    echo -e "${RED}❌ Erro ao criar backup!${NC}"
    exit 1
fi

echo -e "\n${GREEN}🎉 Processo de backup finalizado!${NC}"
