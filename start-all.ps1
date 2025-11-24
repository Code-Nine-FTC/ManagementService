# Script para iniciar todos os serviços - Versão PowerShell
# Uso: .\start-all.ps1 [profile]
# Exemplo: .\start-all.ps1 dev

param(
    [string]$Profile = "dev"
)

# Configurações
$ErrorActionPreference = "Stop"

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "INICIANDO TODOS OS SERVIÇOS - PROFILE: $Profile" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Serviços que serão iniciados:"
Write-Host "  🐘 PostgreSQL      -> localhost:5433" -ForegroundColor White
Write-Host "  🐍 Python API      -> localhost:8000" -ForegroundColor White
Write-Host "  ☕ Java Backend    -> localhost:8080" -ForegroundColor White
Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

# Verificar se Docker está rodando
Write-Host "🔍 Verificando Docker..." -ForegroundColor Yellow
try {
    docker info | Out-Null
    Write-Host "  ✅ Docker está rodando!" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker não está rodando. Inicie o Docker Desktop e tente novamente." -ForegroundColor Red
    exit 1
}

# Definir variável de ambiente
$env:SPRING_PROFILES_ACTIVE = $Profile

# Parar containers existentes
Write-Host ""
Write-Host "🛑 Parando containers existentes..." -ForegroundColor Yellow
docker-compose down

# Compilar projeto Java
Write-Host ""
Write-Host "🔨 Compilando projeto Java..." -ForegroundColor Yellow
if ($IsWindows -or $env:OS -match "Windows") {
    .\mvnw.cmd clean package -DskipTests
} else {
    ./mvnw clean package -DskipTests
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Erro ao compilar projeto Java" -ForegroundColor Red
    exit 1
}

# Buildar imagens Docker
Write-Host ""
Write-Host "🐳 Buildando imagens Docker..." -ForegroundColor Yellow
docker-compose build

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Erro ao buildar imagens Docker" -ForegroundColor Red
    exit 1
}

# Iniciar serviços
Write-Host ""
Write-Host "🚀 Iniciando serviços..." -ForegroundColor Green
docker-compose up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Erro ao iniciar serviços" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "⏳ Aguardando serviços ficarem prontos..." -ForegroundColor Yellow
Write-Host ""

# Função para aguardar serviço com timeout
function Wait-Service {
    param(
        [string]$Name,
        [scriptblock]$CheckCommand,
        [int]$TimeoutSeconds,
        [string]$LogService
    )
    
    Write-Host "  🔄 Aguardando $Name..." -ForegroundColor Cyan
    $elapsed = 0
    $interval = 2
    
    while ($elapsed -lt $TimeoutSeconds) {
        try {
            $result = & $CheckCommand 2>$null
            if ($result) {
                Write-Host "  ✅ $Name pronto!" -ForegroundColor Green
                return $true
            }
        } catch {
            # Continuar tentando
        }
        
        Start-Sleep -Seconds $interval
        $elapsed += $interval
    }
    
    Write-Host "  ❌ $Name não iniciou no tempo esperado" -ForegroundColor Red
    if ($LogService) {
        Write-Host "  📋 Logs de $LogService`:" -ForegroundColor Yellow
        docker-compose logs $LogService
    }
    return $false
}

# Aguardar PostgreSQL
$pgReady = Wait-Service -Name "PostgreSQL" -TimeoutSeconds 60 -LogService "db" -CheckCommand {
    docker-compose exec -T db pg_isready -U postgres 2>$null
    return $LASTEXITCODE -eq 0
}

if (-not $pgReady) {
    exit 1
}

# Aguardar Python API
$pythonReady = Wait-Service -Name "Python API" -TimeoutSeconds 90 -LogService "python-api" -CheckCommand {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8000/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        return $response.StatusCode -eq 200
    } catch {
        return $false
    }
}

if (-not $pythonReady) {
    exit 1
}

# Aguardar Java Backend
$javaReady = Wait-Service -Name "Java Backend" -TimeoutSeconds 120 -LogService "java-backend" -CheckCommand {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/swagger-ui/index.html" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        return $response.StatusCode -eq 200
    } catch {
        return $false
    }
}

if (-not $javaReady) {
    exit 1
}

# Verificar integridade dos dados
Write-Host ""
Write-Host "🔧 Verificando integridade dos dados..." -ForegroundColor Yellow

try {
    $nullCount = docker-compose exec -T db psql -U postgres -d teste -t -c "SELECT COUNT(*) FROM items WHERE maximum_stock IS NULL;" 2>$null
    $nullCount = [int]($nullCount -replace '\s+','')
    
    if ($nullCount -gt 0) {
        Write-Host "  ⚠️  Corrigindo $nullCount itens com maximum_stock NULL..." -ForegroundColor Yellow
        docker-compose exec -T db psql -U postgres -d teste -c "UPDATE items SET maximum_stock = COALESCE(minimum_stock * 3, 100) WHERE maximum_stock IS NULL;" | Out-Null
        Write-Host "  ✅ Dados corrigidos!" -ForegroundColor Green
    } else {
        Write-Host "  ✅ Dados íntegros!" -ForegroundColor Green
    }
} catch {
    Write-Host "  ⚠️  Não foi possível verificar integridade dos dados" -ForegroundColor Yellow
}

# Mensagem final
Write-Host ""
Write-Host "======================================================================" -ForegroundColor Green
Write-Host "✅ TODOS OS SERVIÇOS ESTÃO RODANDO!" -ForegroundColor Green
Write-Host "======================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "🌐 URLs disponíveis:" -ForegroundColor Cyan
Write-Host "  📊 PostgreSQL:           localhost:5433" -ForegroundColor White
Write-Host "  🤖 Python API:           http://localhost:8000" -ForegroundColor White
Write-Host "  📚 Python API Docs:      http://localhost:8000/docs" -ForegroundColor White
Write-Host "  ☕ Java Backend:         http://localhost:8080" -ForegroundColor White
Write-Host "  🏥 Java Health:          http://localhost:8080/actuator/health" -ForegroundColor White
Write-Host ""
Write-Host "📋 Comandos úteis:" -ForegroundColor Cyan
Write-Host "  Ver logs:                docker-compose logs -f" -ForegroundColor White
Write-Host "  Ver logs Python:         docker-compose logs -f python-api" -ForegroundColor White
Write-Host "  Ver logs Java:           docker-compose logs -f java-backend" -ForegroundColor White
Write-Host "  Parar tudo:              docker-compose down" -ForegroundColor White
Write-Host "  Reiniciar:               docker-compose restart" -ForegroundColor White
Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 Acompanhe os logs com: docker-compose logs -f" -ForegroundColor Yellow
Write-Host ""
Write-Host "Pressione Ctrl+C para parar de seguir os logs (serviços continuarão rodando)" -ForegroundColor Yellow
Write-Host ""

# Seguir logs (opcional)
try {
    docker-compose logs -f
} catch {
    # Usuário pressionou Ctrl+C
    Write-Host ""
    Write-Host "Logs interrompidos. Serviços continuam rodando." -ForegroundColor Yellow
}
