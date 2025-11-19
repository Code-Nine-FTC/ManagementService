# Script para parar todos os serviços - Versão PowerShell
# Uso: .\stop-all.ps1

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "PARANDO TODOS OS SERVIÇOS" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

docker-compose down

Write-Host ""
Write-Host "✅ Todos os serviços foram parados!" -ForegroundColor Green
Write-Host ""
Write-Host "Para remover volumes (CUIDADO: apaga dados do banco):" -ForegroundColor Yellow
Write-Host "  docker-compose down -v" -ForegroundColor White
Write-Host ""
