# FinancesK - Deploy Automático 🚀

## Sistema Implementado

### 📁 Arquivos Criados

1. **`.github/workflows/deploy.yml`** - Workflow principal do GitHub Actions
2. **`.github/DEPLOY_SETUP.md`** - Documentação completa de configuração
3. **`.dockerignore`** - Otimização do build Docker
4. **`deploy-local.sh`** - Script para deploy manual local

### 🔄 Fluxo Automático

**Triggers:**
- ✅ Push na branch `main`
- ✅ Pull Request mergeado na `main`

**Pipeline:**
1. **Testes** → Executa todos os testes
2. **Build** → Constrói imagem Docker multi-arquitetura
3. **Versionamento** → Gera tags automaticamente
4. **Deploy** → Publica no Docker Hub
5. **Release** → Cria release no GitHub

### 🏷️ Tags Automáticas

Cada deploy gera 3 versões:

```bash
# Sempre a mais recente (produção)
redmater/financesk:latest

# Versão única com timestamp
redmater/financesk:0.0.1.20241009143052

# Versão base do projeto
redmater/financesk:0.0.1
```

### ⚙️ Configuração Necessária

**Secrets do GitHub** (obrigatório):
```
DOCKER_USERNAME: redmater
DOCKER_PASSWORD: seu_token_dockerhub
```

**Como configurar:**
1. Vá em: Repository → Settings → Secrets and variables → Actions
2. Adicione os secrets acima
3. Para o token: Docker Hub → Account Settings → Security → Access Tokens

### 🛠️ Deploy Local

**Script criado:** `./deploy-local.sh`

```bash
# Deploy completo (build + push)
./deploy-local.sh --deploy

# Apenas build local
./deploy-local.sh --build

# Apenas push (se imagem já existe)
./deploy-local.sh --push

# Limpeza de imagens antigas
./deploy-local.sh --cleanup
```

### 🎯 Como Usar

**Automático:**
1. Configure os secrets no GitHub
2. Faça commit/merge na `main`
3. GitHub Actions faz tudo automaticamente
4. Atualize produção: `docker-compose pull && docker-compose up -d`

**Manual:**
```bash
# Deploy local de teste
./deploy-local.sh --build

# Deploy completo para produção
./deploy-local.sh --deploy
```

### 📊 Monitoramento

- **GitHub Actions:** Tab "Actions" do repositório
- **Docker Hub:** https://hub.docker.com/r/redmater/financesk
- **Releases:** Criados automaticamente a cada deploy

### 🔧 Otimizações Implementadas

- ✅ Build multi-arquitetura (AMD64 + ARM64)
- ✅ Cache de dependências Gradle
- ✅ Cache de layers Docker
- ✅ Build em duas etapas (builder + runtime)
- ✅ Usuário não-root para segurança
- ✅ .dockerignore otimizado

### 🚨 Próximos Passos

1. **Configure os secrets no GitHub**
2. **Teste o primeiro deploy**
3. **Verifique as imagens no Docker Hub**
4. **Atualize docker-compose-prod.yml se necessário**

O sistema está pronto para uso! Qualquer push ou merge na `main` irá automaticamente:
- Executar testes
- Construir nova imagem
- Versionar adequadamente
- Publicar no seu Docker Hub
- Manter histórico de versões

Tudo configurado conforme solicitado! 🎉
