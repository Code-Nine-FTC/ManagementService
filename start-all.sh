set -e

PROFILE=${1:-dev}

echo "======================================================================"
echo "INICIANDO TODOS OS SERVIÇOS - PROFILE: $PROFILE"
echo "======================================================================"
echo ""
echo "Serviços que serão iniciados:"
echo "  🐘 PostgreSQL      -> localhost:5433"
echo "  🐍 Python API      -> localhost:8000"
echo "  ☕ Java Backend    -> localhost:8080"
echo ""
echo "======================================================================"
echo ""

if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Inicie o Docker e tente novamente."
    exit 1
fi

export SPRING_PROFILES_ACTIVE=$PROFILE

echo "🛑 Parando containers existentes..."
docker-compose down

echo ""
echo "🔨 Compilando projeto Java..."
./mvnw clean package -DskipTests

echo ""
echo "🐳 Buildando imagens Docker..."
docker-compose build

echo ""
echo "🚀 Iniciando serviços..."
docker-compose up -d

echo ""
echo "⏳ Aguardando serviços ficarem prontos..."
echo ""

echo "  🐘 Aguardando PostgreSQL..."
# Loop com timeout manual (compatível com macOS)
SECONDS=0
until docker-compose exec -T db pg_isready -U postgres > /dev/null 2>&1; do 
    if [ $SECONDS -gt 60 ]; then
        echo "❌ PostgreSQL não iniciou no tempo esperado"
        docker-compose logs db
        exit 1
    fi
    sleep 2
done
echo "  ✅ PostgreSQL pronto!"

echo "  🐍 Aguardando Python API..."
SECONDS=0
until curl -s http://localhost:8000/health > /dev/null 2>&1; do 
    if [ $SECONDS -gt 90 ]; then
        echo "❌ Python API não iniciou no tempo esperado"
        docker-compose logs python-api
        exit 1
    fi
    sleep 3
done
echo "  ✅ Python API pronta!"

echo "  ☕ Aguardando Java Backend..."
SECONDS=0
until curl -s http://localhost:8080/swagger-ui/index.html > /dev/null 2>&1; do 
    if [ $SECONDS -gt 120 ]; then
        echo "❌ Java Backend não iniciou no tempo esperado"
        docker-compose logs java-backend
        exit 1
    fi
    sleep 5
done
echo "  ✅ Java Backend pronto!"

echo ""
echo "🔧 Verificando integridade dos dados..."

# Verificar se maximum_stock precisa ser corrigido
NULL_COUNT=$(docker-compose exec -T db psql -U postgres -d teste -t -c "SELECT COUNT(*) FROM items WHERE maximum_stock IS NULL;" 2>/dev/null | tr -d ' ')

if [ "$NULL_COUNT" -gt 0 ]; then
    echo "  ⚠️  Corrigindo $NULL_COUNT itens com maximum_stock NULL..."
    docker-compose exec -T db psql -U postgres -d teste -c "UPDATE items SET maximum_stock = COALESCE(minimum_stock * 3, 100) WHERE maximum_stock IS NULL;" > /dev/null 2>&1
    echo "  ✅ Dados corrigidos!"
else
    echo "  ✅ Dados íntegros!"
fi

echo ""
echo "======================================================================"
echo "✅ TODOS OS SERVIÇOS ESTÃO RODANDO!"
echo "======================================================================"
echo ""
echo "🌐 URLs disponíveis:"
echo "  📊 PostgreSQL:           localhost:5433"
echo "  🤖 Python API:           http://localhost:8000"
echo "  📚 Python API Docs:      http://localhost:8000/docs"
echo "  ☕ Java Backend:         http://localhost:8080"
echo "  🏥 Java Health:          http://localhost:8080/actuator/health"
echo ""
echo "📋 Comandos úteis:"
echo "  Ver logs:                docker-compose logs -f"
echo "  Ver logs Python:         docker-compose logs -f python-api"
echo "  Ver logs Java:           docker-compose logs -f java-backend"
echo "  Parar tudo:              docker-compose down"
echo "  Reiniciar:               docker-compose restart"
echo ""
echo "======================================================================"
echo ""

# Mostrar logs
echo "📋 Acompanhe os logs com: docker-compose logs -f"
echo ""
echo "Pressione Ctrl+C para parar de seguir os logs (serviços continuarão rodando)"
echo ""

# Seguir logs (opcional)
docker-compose logs -f
