echo "======================================================================"
echo "PARANDO TODOS OS SERVIÇOS"
echo "======================================================================"
echo ""

docker-compose down

echo ""
echo "✅ Todos os serviços foram parados!"
echo ""
echo "Para remover volumes (CUIDADO: apaga dados do banco):"
echo "  docker-compose down -v"
echo ""
