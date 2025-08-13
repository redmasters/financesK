#!/bin/bash

# Script para build e deploy da aplicação financesK

echo "🚀 Iniciando build e deploy da aplicação financesK..."

# Verificar se o Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Por favor, inicie o Docker primeiro."
    exit 1
fi

# Build da aplicação com Gradle
echo "📦 Fazendo build da aplicação..."
./gradlew clean build -x test

# Verificar se o build foi bem-sucedido
if [ $? -ne 0 ]; then
    echo "❌ Falha no build da aplicação."
    exit 1
fi

# Parar containers existentes (se houver)
echo "🛑 Parando containers existentes..."
docker compose down

# Build e start dos serviços
echo "🐳 Iniciando containers..."
docker compose up --build -d

# Aguardar os serviços ficarem prontos
echo "⏳ Aguardando serviços ficarem prontos..."
sleep 15

# Verificar se os serviços estão rodando
if docker compose ps | grep -q "Up"; then
    echo "✅ Deploy concluído com sucesso!"
    echo "📊 Aplicação disponível em: http://localhost:8080"
    echo "🗄️  Banco de dados disponível em: localhost:5432"
    echo "📋 Para visualizar logs: docker compose logs -f"
    echo "🛑 Para parar: docker compose down"
else
    echo "❌ Falha ao iniciar os serviços."
    docker compose logs
    exit 1
fi
