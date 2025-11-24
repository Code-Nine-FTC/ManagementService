from fastapi import APIRouter, HTTPException
from datetime import datetime
import logging

prediction_service = None

logger = logging.getLogger(__name__)

router = APIRouter()


@router.get("/health", tags=["Health"])
async def health_check():
    return {
        "status": "healthy" if prediction_service else "unhealthy",
        "timestamp": datetime.now().isoformat(),
        "model_loaded": prediction_service is not None and getattr(prediction_service, 'model', None) is not None,
        "database_connected": prediction_service is not None and getattr(prediction_service, 'db', None) is not None
    }

@router.get("/model/info", tags=["Model"])
async def get_model_info():
    if prediction_service is None:
        raise HTTPException(status_code=503, detail="Serviço de previsão não inicializado")
    return prediction_service.get_model_info()