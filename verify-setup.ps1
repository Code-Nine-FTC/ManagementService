# Script para verificar setup completo do sistema - Versão PowerShell
# Uso: .\verify-setup.ps1

$ErrorActionPreference = "Continue"

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "🔍 VERIFICAÇÃO COMPLETA DO SISTEMA" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

# Função para verificar serviço
function Test-Service {
    param(
        [string]$Name,
        [string]$Url
    )
    
    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        Write-Host "  ✅ $Name está respondendo" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "  ❌ $Name não está respondendo" -ForegroundColor Red
        return $false
    }
}

# Verificar containers
Write-Host "📦 Verificando containers Docker..." -ForegroundColor Yellow
try {
    $runningContainers = (docker-compose ps --services --filter "status=running" | Measure-Object -Line).Lines
    $expected = 3
    
    if ($runningContainers -eq $expected) {
        Write-Host "  ✅ Todos os $expected containers estão rodando" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  Apenas $runningContainers de $expected containers estão rodando" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ❌ Erro ao verificar containers" -ForegroundColor Red
}

Write-Host ""
Write-Host "🌐 Verificando serviços..." -ForegroundColor Yellow
Test-Service -Name "Python API" -Url "http://localhost:8000/health"
Test-Service -Name "Java Backend" -Url "http://localhost:8080/actuator/health"

Write-Host ""
Write-Host "🗄️  Verificando banco de dados..." -ForegroundColor Yellow

# Verificar schema ml
try {
    $schemaExists = docker exec management_db psql -U postgres -d teste -t -c "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = 'ml';" 2>$null
    $schemaExists = [int]($schemaExists -replace '\s+','')
    
    if ($schemaExists -eq 1) {
        Write-Host "  ✅ Schema 'ml' existe" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Schema 'ml' não encontrado" -ForegroundColor Red
    }
} catch {
    Write-Host "  ❌ Erro ao verificar schema 'ml'" -ForegroundColor Red
}

# Verificar tabela predictions
try {
    $tableExists = docker exec management_db psql -U postgres -d teste -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'ml' AND table_name = 'predictions';" 2>$null
    $tableExists = [int]($tableExists -replace '\s+','')
    
    if ($tableExists -eq 1) {
        Write-Host "  ✅ Tabela 'ml.predictions' existe" -ForegroundColor Green
        
        # Contar previsões
        $predCount = docker exec management_db psql -U postgres -d teste -t -c "SELECT COUNT(*) FROM ml.predictions;" 2>$null
        $predCount = [int]($predCount -replace '\s+','')
        Write-Host "  ℹ️  Total de previsões salvas: $predCount" -ForegroundColor Cyan
    } else {
        Write-Host "  ⚠️  Tabela 'ml.predictions' não encontrada (será criada na primeira execução)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ⚠️  Não foi possível verificar tabela 'ml.predictions'" -ForegroundColor Yellow
}

# Verificar items com maximum_stock NULL
try {
    $nullCount = docker exec management_db psql -U postgres -d teste -t -c "SELECT COUNT(*) FROM items WHERE maximum_stock IS NULL;" 2>$null
    $nullCount = [int]($nullCount -replace '\s+','')
    
    if ($nullCount -eq 0) {
        Write-Host "  ✅ Todos os itens têm maximum_stock definido" -ForegroundColor Green
    } else {
        Write-Host "  ❌ $nullCount itens com maximum_stock NULL (necessário corrigir!)" -ForegroundColor Red
    }
} catch {
    Write-Host "  ⚠️  Não foi possível verificar maximum_stock" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🤖 Testando Python API de predições..." -ForegroundColor Yellow

# Testar predição de um item
try {
    $prediction = Invoke-RestMethod -Uri "http://localhost:8000/predictions/item/223" -Method Get -ErrorAction SilentlyContinue
    
    if ($prediction.predicted_quantity) {
        $predValue = $prediction.predicted_quantity
        Write-Host "  ✅ API de predições funcionando (Item 223: $predValue)" -ForegroundColor Green
    } else {
        Write-Host "  ❌ API de predições não está respondendo corretamente" -ForegroundColor Red
    }
} catch {
    Write-Host "  ❌ API de predições não está respondendo corretamente" -ForegroundColor Red
}

Write-Host ""
Write-Host "☕ Testando autenticação Java Backend..." -ForegroundColor Yellow

# Testar login
try {
    $loginBody = @{
        email = "codenine@email.com"
        password = "codenine123"
    } | ConvertTo-Json
    
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/login" `
        -Method Post `
        -Body $loginBody `
        -ContentType "application/json" `
        -ErrorAction SilentlyContinue
    
    if ($loginResponse.token) {
        Write-Host "  ✅ Autenticação JWT funcionando" -ForegroundColor Green
        
        # Testar endpoint de previsões
        $token = $loginResponse.token
        $headers = @{
            Authorization = "Bearer $token"
        }
        
        try {
            $predResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/predictions/item/223" `
                -Method Get `
                -Headers $headers `
                -ErrorAction SilentlyContinue
            
            if ($predResponse.itemId) {
                Write-Host "  ✅ Endpoints de previsão acessíveis" -ForegroundColor Green
            } else {
                Write-Host "  ⚠️  Endpoints de previsão podem não estar acessíveis" -ForegroundColor Yellow
            }
        } catch {
            Write-Host "  ⚠️  Endpoints de previsão podem não estar acessíveis" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  ❌ Autenticação não está funcionando" -ForegroundColor Red
    }
} catch {
    Write-Host "  ❌ Autenticação não está funcionando" -ForegroundColor Red
}

Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "📊 RESUMO DA VERIFICAÇÃO" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Próximos passos sugeridos:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1️⃣  Gerar previsões iniciais:" -ForegroundColor White
Write-Host "   `$token = (Invoke-RestMethod -Uri 'http://localhost:8080/login' ``" -ForegroundColor Gray
Write-Host "     -Method Post -Body (@{email='codenine@email.com';password='codenine123'} | ConvertTo-Json) ``" -ForegroundColor Gray
Write-Host "     -ContentType 'application/json').token" -ForegroundColor Gray
Write-Host "   Invoke-RestMethod -Uri 'http://localhost:8080/api/predictions/generate-all' ``" -ForegroundColor Gray
Write-Host "     -Method Post -Headers @{Authorization='Bearer ' + `$token}" -ForegroundColor Gray
Write-Host ""
Write-Host "2️⃣  Consultar previsão de um item:" -ForegroundColor White
Write-Host "   Invoke-RestMethod -Uri 'http://localhost:8080/api/predictions/item/223' ``" -ForegroundColor Gray
Write-Host "     -Headers @{Authorization='Bearer ' + `$token}" -ForegroundColor Gray
Write-Host ""
Write-Host "3️⃣  Ver documentação da API:" -ForegroundColor White
Write-Host "   http://localhost:8080/swagger-ui/index.html" -ForegroundColor Cyan
Write-Host "   http://localhost:8000/docs" -ForegroundColor Cyan
Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
