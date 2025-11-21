import uvicorn
import sys
import os

ROOT_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC_DIR = os.path.join(ROOT_DIR, 'src')
if SRC_DIR not in sys.path:
    sys.path.insert(0, SRC_DIR)

if __name__ == "__main__":
    print("="*80)
    print("🚀 INICIANDO API DE PREVISÕES")
    print("="*80)
    print()
    print("📍 Servidor rodando em: http://localhost:8000")
    print("📚 Documentação Swagger: http://localhost:8000/docs")
    print("📚 Documentação ReDoc: http://localhost:8000/redoc")
    print()
    print("Pressione CTRL+C para parar o servidor")
    print("="*80)
    print()
    
    uvicorn.run(
        "api:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info",
        app_dir=SRC_DIR
    )
