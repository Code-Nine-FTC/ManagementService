from fastapi import APIRouter, HTTPException
from datetime import datetime
import logging

from src.controllers.schemas import HealthResponse, PredictionResponse, ModelInfo

prediction_service = None

logger = logging.getLogger(__name__)

router = APIRouter()


@router.get("/health", response_model=HealthResponse, tags=["Health"])
async def health_check():
    return HealthResponse(
        status="healthy" if prediction_service else "unhealthy",
        timestamp=datetime.now().isoformat(),
        model_loaded=prediction_service is not None and getattr(prediction_service, 'model', None) is not None,
        database_connected=prediction_service is not None and getattr(prediction_service, 'db', None) is not None
    )

@router.get("/test/demo", tags=["Test"])
async def demo_prediction():
    return PredictionResponse(
        item_id=999,
        predicted_quantity=123.45,
        current_stock=80.0,
        minimum_stock=50.0,
        maximum_stock=200.0,
        prediction_month=12,
        prediction_year=2024,
        needs_restock=True,
        restock_quantity=43.45,
        confidence_score=0.94,
        model_used="gradient_boosting",
        timestamp=datetime.now().isoformat()
    )

@router.get("/model/info", response_model=ModelInfo, tags=["Model"])
async def get_model_info():
    if prediction_service is None:
        raise HTTPException(status_code=503, detail="Serviço de previsão não inicializado")
    return prediction_service.get_model_info()