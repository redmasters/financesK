#!/bin/bash

# Script simples de backup PostgreSQL Docker
CONTAINER_NAME="financesK-postgres"
DB_NAME="financesk_db"
DB_USER="financesk_admin"
BACKUP_DIR="$HOME/docker/postgres"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Criar diretório se não existir
mkdir -p "$BACKUP_DIR"

echo "🗃️ Fazendo backup do banco $DB_NAME..."

# Executar backup
docker exec "$CONTAINER_NAME" pg_dump \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    --clean --no-owner --no-privileges \
    > "$BACKUP_DIR/backup_financesk_${TIMESTAMP}.sql"

if [ $? -eq 0 ]; then
    echo "✅ Backup salvo em: $BACKUP_DIR/backup_financesk_${TIMESTAMP}.sql"

    # Comprimir o backup
    gzip "$BACKUP_DIR/backup_financesk_${TIMESTAMP}.sql"
    echo "🗜️ Backup comprimido: backup_financesk_${TIMESTAMP}.sql.gz"
else
    echo "❌ Erro ao fazer backup!"
    exit 1
fi
