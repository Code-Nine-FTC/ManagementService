#!/bin/bash

set -e

echo "======================================================================"
echo "🔍 VERIFICAÇÃO COMPLETA DO SISTEMA"
echo "======================================================================"
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Função para verificar serviço
check_service() {
    local name=$1
    local url=$2
    
    if curl -s "$url" > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅${NC} $name está respondendo"
        return 0
    else
        echo -e "  ${RED}❌${NC} $name não está respondendo"
        return 1
    fi
}

# Verificar containers
echo "📦 Verificando containers Docker..."
CONTAINERS=$(docker-compose ps --services --filter "status=running" | wc -l | tr -d ' ')
EXPECTED=3

if [ "$CONTAINERS" -eq "$EXPECTED" ]; then
    echo -e "  ${GREEN}✅${NC} Todos os $EXPECTED containers estão rodando"
else
    echo -e "  ${YELLOW}⚠️${NC}  Apenas $CONTAINERS de $EXPECTED containers estão rodando"
fi

echo ""
echo "🌐 Verificando serviços..."
check_service "PostgreSQL" "localhost:5433" || true
check_service "Python API" "http://localhost:8000/health"
check_service "Java Backend" "http://localhost:8080/actuator/health"

echo ""
echo "🗄️  Verificando banco de dados..."

# Verificar schema ml
SCHEMA_EXISTS=$(docker exec management_db psql -U postgres -d teste -t -c "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = 'ml';" 2>/dev/null | tr -d ' ')

if [ "$SCHEMA_EXISTS" -eq 1 ]; then
    echo -e "  ${GREEN}✅${NC} Schema 'ml' existe"
else
    echo -e "  ${RED}❌${NC} Schema 'ml' não encontrado"
fi

# Verificar tabela predictions
TABLE_EXISTS=$(docker exec management_db psql -U postgres -d teste -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'ml' AND table_name = 'predictions';" 2>/dev/null | tr -d ' ')

if [ "$TABLE_EXISTS" -eq 1 ]; then
    echo -e "  ${GREEN}✅${NC} Tabela 'ml.predictions' existe"
    
    # Contar previsões
    PRED_COUNT=$(docker exec management_db psql -U postgres -d teste -t -c "SELECT COUNT(*) FROM ml.predictions;" 2>/dev/null | tr -d ' ')
    echo -e "  ${GREEN}ℹ️${NC}  Total de previsões salvas: $PRED_COUNT"
else
    echo -e "  ${YELLOW}⚠️${NC}  Tabela 'ml.predictions' não encontrada (será criada na primeira execução)"
fi

# Verificar items com maximum_stock NULL
NULL_COUNT=$(docker exec management_db psql -U postgres -d teste -t -c "SELECT COUNT(*) FROM items WHERE maximum_stock IS NULL;" 2>/dev/null | tr -d ' ')

if [ "$NULL_COUNT" -eq 0 ]; then
    echo -e "  ${GREEN}✅${NC} Todos os itens têm maximum_stock definido"
else
    echo -e "  ${RED}❌${NC} $NULL_COUNT itens com maximum_stock NULL (necessário corrigir!)"
fi

echo ""
echo "🤖 Testando Python API de predições..."

# Testar predição de um item
PREDICTION=$(curl -s "http://localhost:8000/predictions/item/223" 2>/dev/null)

if echo "$PREDICTION" | grep -q "predicted_quantity"; then
    PRED_VALUE=$(echo "$PREDICTION" | python3 -c "import sys, json; print(json.load(sys.stdin)['predicted_quantity'])" 2>/dev/null)
    echo -e "  ${GREEN}✅${NC} API de predições funcionando (Item 223: $PRED_VALUE)"
else
    echo -e "  ${RED}❌${NC} API de predições não está respondendo corretamente"
fi

echo ""
echo "☕ Testando autenticação Java Backend..."

# Testar login
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/login \
    -H "Content-Type: application/json" \
    -d '{"email":"codenine@email.com","password":"codenine123"}' 2>/dev/null)

if echo "$LOGIN_RESPONSE" | grep -q "token"; then
    echo -e "  ${GREEN}✅${NC} Autenticação JWT funcionando"
    
    # Extrair token e testar endpoint protegido
    TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['token'])" 2>/dev/null)
    
    if [ -n "$TOKEN" ]; then
        # Testar endpoint de previsões
        PRED_RESPONSE=$(curl -s "http://localhost:8080/api/predictions/item/223" \
            -H "Authorization: Bearer $TOKEN" 2>/dev/null)
        
        if echo "$PRED_RESPONSE" | grep -q "itemId"; then
            echo -e "  ${GREEN}✅${NC} Endpoints de previsão acessíveis"
        else
            echo -e "  ${YELLOW}⚠️${NC}  Endpoints de previsão podem não estar acessíveis"
        fi
    fi
else
    echo -e "  ${RED}❌${NC} Autenticação não está funcionando"
fi

echo ""
echo "======================================================================"
echo "📊 RESUMO DA VERIFICAÇÃO"
echo "======================================================================"
echo ""
echo "Próximos passos sugeridos:"
echo ""
echo "1️⃣  Gerar previsões iniciais:"
echo "   curl -X POST http://localhost:8080/api/predictions/generate-all \\"
echo "     -H \"Authorization: Bearer {TOKEN}\""
echo ""
echo "2️⃣  Consultar previsão de um item:"
echo "   curl http://localhost:8080/api/predictions/item/223 \\"
echo "     -H \"Authorization: Bearer {TOKEN}\""
echo ""
echo "3️⃣  Ver documentação da API:"
echo "   http://localhost:8080/swagger-ui/index.html"
echo "   http://localhost:8000/docs"
echo ""
echo "======================================================================"
