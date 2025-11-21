import logging

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from src.controllers.item_controller import router as item_router
from src.services.prediction_service import PredictionService

import src.controllers.item_controller as item_controller
import src.services.item_service as item_service


logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="API de Previsão de Demanda",
    description="API para prever demanda futura de itens usando Machine Learning",
    version="2.0.0"
)

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

prediction_service = None

@app.on_event("startup")
async def startup_event():
    global prediction_service
    try:
        logger.info("🚀 Inicializando serviço de previsão...")
        prediction_service = PredictionService()
        item_controller.prediction_service = prediction_service
        item_service.prediction_service = prediction_service
        logger.info("✅ Serviço de previsão inicializado com sucesso!")
    except Exception as e:
        logger.error(f"❌ Erro ao inicializar serviço: {e}")
        raise

@app.on_event("shutdown")
async def shutdown_event():
    logger.info("Desligando...")

@app.get("/", tags=["Root"])
async def root():
    return {
        "message": "API MachineLearning CodeNine",
        "version": "1.0.2",
        "docs": "/docs",
        "health": "/health"
    }

app.include_router(item_router)
