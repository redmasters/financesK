#!/bin/bash

# FinancesK - Deploy Local Script
# Este script facilita o deploy manual local para testes

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configurações
DOCKER_IMAGE="redmater/financesk"
DEFAULT_TAG="latest"

echo -e "${BLUE}🚀 FinancesK - Deploy Local${NC}"
echo "=================================="

# Função para mostrar ajuda
show_help() {
    echo "Uso: $0 [opções]"
    echo ""
    echo "Opções:"
    echo "  -t, --tag TAG        Tag da imagem (padrão: latest)"
    echo "  -b, --build          Fazer build local da imagem"
    echo "  -p, --push           Fazer push para Docker Hub"
    echo "  -d, --deploy         Deploy completo (build + push)"
    echo "  -c, --cleanup        Limpar imagens antigas locais"
    echo "  -h, --help           Mostrar esta ajuda"
    echo ""
    echo "Exemplos:"
    echo "  $0 --build           # Apenas build local"
    echo "  $0 --deploy          # Build + push para Docker Hub"
    echo "  $0 -t v1.0.1 --push  # Push com tag específica"
}

# Função para verificar dependências
check_dependencies() {
    echo -e "${YELLOW}📋 Verificando dependências...${NC}"

    if ! command -v docker &> /dev/null; then
        echo -e "${RED}❌ Docker não encontrado!${NC}"
        exit 1
    fi

    if ! command -v ./gradlew &> /dev/null; then
        echo -e "${RED}❌ Gradle wrapper não encontrado!${NC}"
        exit 1
    fi

    echo -e "${GREEN}✅ Dependências OK${NC}"
}

# Função para executar testes
run_tests() {
    echo -e "${YELLOW}🧪 Executando testes...${NC}"

    if ./gradlew test --no-daemon; then
        echo -e "${GREEN}✅ Testes passaram${NC}"
    else
        echo -e "${RED}❌ Testes falharam!${NC}"
        exit 1
    fi
}

# Função para fazer build da imagem
build_image() {
    local tag=${1:-$DEFAULT_TAG}

    echo -e "${YELLOW}🔨 Construindo imagem Docker...${NC}"
    echo "Tag: ${DOCKER_IMAGE}:${tag}"

    # Obter versão do build.gradle.kts
    VERSION=$(grep "version = " build.gradle.kts | head -1 | sed 's/.*version = "\(.*\)".*/\1/' | sed 's/-SNAPSHOT//')
    TIMESTAMP=$(date +%Y%m%d%H%M%S)
    VERSIONED_TAG="${VERSION}.${TIMESTAMP}"

    echo "Versões que serão criadas:"
    echo "  - ${DOCKER_IMAGE}:latest"
    echo "  - ${DOCKER_IMAGE}:${VERSION}"
    echo "  - ${DOCKER_IMAGE}:${VERSIONED_TAG}"

    if docker build \
        --platform linux/amd64,linux/arm64 \
        -t "${DOCKER_IMAGE}:latest" \
        -t "${DOCKER_IMAGE}:${VERSION}" \
        -t "${DOCKER_IMAGE}:${VERSIONED_TAG}" \
        .; then
        echo -e "${GREEN}✅ Build concluído com sucesso${NC}"
        echo "Imagem criada: ${DOCKER_IMAGE}:${tag}"
    else
        echo -e "${RED}❌ Erro no build da imagem!${NC}"
        exit 1
    fi
}

# Função para fazer push da imagem
push_image() {
    local tag=${1:-$DEFAULT_TAG}

    echo -e "${YELLOW}📤 Enviando para Docker Hub...${NC}"

    # Verificar se está logado no Docker Hub
    if ! docker info | grep -q "Username:"; then
        echo -e "${YELLOW}⚠️  Fazendo login no Docker Hub...${NC}"
        docker login
    fi

    # Obter versão para push de todas as tags
    VERSION=$(grep "version = " build.gradle.kts | head -1 | sed 's/.*version = "\(.*\)".*/\1/' | sed 's/-SNAPSHOT//')
    TIMESTAMP=$(date +%Y%m%d%H%M%S)
    VERSIONED_TAG="${VERSION}.${TIMESTAMP}"

    echo "Enviando todas as versões:"

    # Push de todas as tags
    for image_tag in "latest" "$VERSION" "$VERSIONED_TAG"; do
        echo -e "${BLUE}Enviando: ${DOCKER_IMAGE}:${image_tag}${NC}"
        if docker push "${DOCKER_IMAGE}:${image_tag}"; then
            echo -e "${GREEN}✅ Push ${image_tag} concluído${NC}"
        else
            echo -e "${RED}❌ Erro no push ${image_tag}!${NC}"
            exit 1
        fi
    done

    echo -e "${GREEN}🎉 Todas as imagens foram enviadas com sucesso!${NC}"
    echo ""
    echo "Imagens disponíveis:"
    echo "  - ${DOCKER_IMAGE}:latest"
    echo "  - ${DOCKER_IMAGE}:${VERSION}"
    echo "  - ${DOCKER_IMAGE}:${VERSIONED_TAG}"
    echo ""
    echo "Para usar na produção:"
    echo "  docker-compose pull && docker-compose up -d"
}

# Função para limpeza
cleanup_images() {
    echo -e "${YELLOW}🧹 Limpando imagens antigas...${NC}"

    # Remover imagens não utilizadas
    docker image prune -f

    # Remover imagens antigas do projeto (mantém apenas as 3 mais recentes)
    docker images "${DOCKER_IMAGE}" --format "table {{.Repository}}\t{{.Tag}}\t{{.CreatedAt}}" | tail -n +2 | head -n -3 | awk '{print $1":"$2}' | xargs -r docker rmi

    echo -e "${GREEN}✅ Limpeza concluída${NC}"
}

# Função de deploy completo
full_deploy() {
    local tag=${1:-$DEFAULT_TAG}

    echo -e "${BLUE}🚀 Iniciando deploy completo...${NC}"

    check_dependencies
    run_tests
    build_image "$tag"
    push_image "$tag"

    echo -e "${GREEN}🎉 Deploy completo realizado com sucesso!${NC}"
}

# Parse de argumentos
TAG="$DEFAULT_TAG"
BUILD=false
PUSH=false
DEPLOY=false
CLEANUP=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--tag)
            TAG="$2"
            shift 2
            ;;
        -b|--build)
            BUILD=true
            shift
            ;;
        -p|--push)
            PUSH=true
            shift
            ;;
        -d|--deploy)
            DEPLOY=true
            shift
            ;;
        -c|--cleanup)
            CLEANUP=true
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            echo -e "${RED}❌ Opção desconhecida: $1${NC}"
            show_help
            exit 1
            ;;
    esac
done

# Executar ações baseadas nos parâmetros
if [ "$DEPLOY" = true ]; then
    full_deploy "$TAG"
elif [ "$BUILD" = true ] && [ "$PUSH" = true ]; then
    check_dependencies
    run_tests
    build_image "$TAG"
    push_image "$TAG"
elif [ "$BUILD" = true ]; then
    check_dependencies
    run_tests
    build_image "$TAG"
elif [ "$PUSH" = true ]; then
    check_dependencies
    push_image "$TAG"
elif [ "$CLEANUP" = true ]; then
    cleanup_images
else
    echo -e "${YELLOW}⚠️  Nenhuma ação especificada!${NC}"
    show_help
    exit 1
fi
