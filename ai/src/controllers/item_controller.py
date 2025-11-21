from fastapi import APIRouter, HTTPException, Query
from typing import List, Optional
from datetime import datetime
import logging

from src.services.item_service import (
    get_consumption_and_forecast, get_all_predictions, get_item_prediction, get_multiple_items_predictions, prediction_service
)
from src.controllers.schemas import PredictionResponse, ModelInfo, HealthResponse

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

@router.get("/model/info", response_model=ModelInfo, tags=["Model"])
async def get_model_info():
    if prediction_service is None:
        raise HTTPException(status_code=503, detail="Serviço de previsão não inicializado")
    return prediction_service.get_model_info()

@router.get("/items/{item_id}/consumption", tags=["Consumption"])
async def get_item_consumption(
    item_id: int,
    retrain: bool = Query(False, description="Treinar modelo se não existir"),
    months_forecast: int = Query(3, ge=1, le=12, description="Meses futuros a prever"),
    db_url: Optional[str] = Query(None, description="String de conexão do banco (opcional)")
):
    try:
        item_id_out, historical, forecast, metadata = get_consumption_and_forecast(
            item_id, retrain, months_forecast, db_url
        )
        if historical is None:
            raise HTTPException(status_code=404, detail=f"Sem consumo COMPLETED para item {item_id}")
        return {
            "item_id": item_id_out,
            "historical_months": historical,
            "forecast_months": forecast,
            "model_metadata": metadata
        }
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Erro ao recuperar consumo item {item_id}: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/predictions/all", response_model=List[PredictionResponse], tags=["Predictions"])
async def get_all_predictions_endpoint(
    db_url: Optional[str] = Query(None, description="String de conexão do banco (opcional)")
):
    try:
        logger.info("📊 Gerando previsões para todos os itens...")
        predictions = get_all_predictions(db_url)
        logger.info(f"✅ {len(predictions)} previsões geradas")
        return predictions
    except Exception as e:
        logger.error(f"❌ Erro ao gerar previsões: {e}")
        raise HTTPException(status_code=500, detail=f"Erro ao gerar previsões: {str(e)}")

@router.get("/predictions/item/{item_id}", response_model=PredictionResponse, tags=["Predictions"])
async def get_item_prediction(
    item_id: int,
    db_url: Optional[str] = Query(None, description="String de conexão do banco (opcional)")
):
    try:
        logger.info(f"📊 Gerando previsão para item {item_id}...")
        prediction = get_item_prediction(item_id, db_url)
        if prediction is None:
            raise HTTPException(status_code=404, detail=f"Item {item_id} não encontrado no banco")
        logger.info(f"✅ Previsão gerada para item {item_id}")
        return prediction
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Erro ao gerar previsão: {e}")
        raise HTTPException(status_code=500, detail=f"Erro ao gerar previsão: {str(e)}")

@router.get("/predictions/items", response_model=List[PredictionResponse], tags=["Predictions"])
async def get_multiple_items_predictions(
    item_ids: str = Query(..., description="IDs dos itens separados por vírgula (ex: 1,2,3,4)"),
    db_url: Optional[str] = Query(None, description="String de conexão do banco (opcional)")
):
    try:
        try:
            ids = [int(id.strip()) for id in item_ids.split(',')]
        except ValueError:
            raise HTTPException(status_code=400, detail="IDs inválidos. Use formato: 1,2,3")
        logger.info(f"📊 Gerando previsões para {len(ids)} itens...")
        predictions = get_multiple_items_predictions(ids, db_url)
        logger.info(f"✅ {len(predictions)} previsões geradas")
        return predictions
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ Erro ao gerar previsões: {e}")
        raise HTTPException(status_code=500, detail=f"Erro ao gerar previsões: {str(e)}")

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
