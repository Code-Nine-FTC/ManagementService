from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import datetime
import logging

from src.services.prediction_service_v2 import PredictionService

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="API de Previsão de Consumo Diário",
    description="API para prever consumo diário de itens e calcular necessidades de reposição usando Prophet",
    version="3.0.0"
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


class DailyPrediction(BaseModel):
    """Previsão para um dia específico"""
    date: str = Field(..., description="Data da previsão (YYYY-MM-DD)")
    predicted_consumption: float = Field(..., description="Consumo previsto para este dia")
    predicted_stock: float = Field(..., description="Estoque previsto após o consumo deste dia")
    lower_bound: float = Field(..., description="Limite inferior da previsão")
    upper_bound: float = Field(..., description="Limite superior da previsão")


class PredictionResponse(BaseModel):
    """Resposta de previsão completa para um item"""
    item_id: int
    item_name: str = Field(..., description="Nome do item")
    current_stock: float = Field(..., description="Estoque atual")
    minimum_stock: float = Field(..., description="Estoque mínimo")
    maximum_stock: float = Field(..., description="Estoque máximo")
    forecast_days: int = Field(..., description="Número de dias previstos")
    predicted_daily_consumption: float = Field(..., description="Consumo médio diário previsto")
    predicted_total_consumption: float = Field(..., description="Consumo total previsto no período")
    predicted_final_stock: float = Field(..., description="Estoque previsto ao final do período")
    needs_restock: bool = Field(..., description="Se precisa repor estoque")
    days_until_minimum_stock: Optional[int] = Field(None, description="Dias até atingir estoque mínimo")
    recommended_restock_quantity: float = Field(..., description="Quantidade recomendada para reposição")
    daily_predictions: List[DailyPrediction] = Field(..., description="Previsões detalhadas por dia")
    model_used: str = Field(..., description="Nome do modelo usado")
    timestamp: str = Field(..., description="Timestamp da previsão")
    
    class Config:
        json_schema_extra = {
            "example": {
                "item_id": 1,
                "item_name": "Caneta Azul",
                "current_stock": 500.0,
                "minimum_stock": 100.0,
                "maximum_stock": 1000.0,
                "forecast_days": 30,
                "predicted_daily_consumption": 15.5,
                "predicted_total_consumption": 465.0,
                "predicted_final_stock": 35.0,
                "needs_restock": True,
                "days_until_minimum_stock": 25,
                "recommended_restock_quantity": 965.0,
                "daily_predictions": [
                    {
                        "date": "2024-11-18",
                        "predicted_consumption": 15.2,
                        "predicted_stock": 484.8,
                        "lower_bound": 10.5,
                        "upper_bound": 20.0
                    }
                ],
                "model_used": "prophet_daily_consumption",
                "timestamp": "2024-11-17T10:30:00"
            }
        }


class PredictionSummary(BaseModel):
    """Resumo de previsão para um item (sem detalhes diários)"""
    item_id: int
    item_name: str
    current_stock: float
    minimum_stock: float
    maximum_stock: float
    predicted_daily_consumption: float
    predicted_total_consumption: float
    predicted_final_stock: float
    needs_restock: bool
    days_until_minimum_stock: Optional[int]
    recommended_restock_quantity: float
    forecast_days: int
    model_used: str
    timestamp: str


class ModelInfo(BaseModel):
    models_loaded: bool
    num_models: int
    model_type: str
    item_ids: List[int]
    metadata: dict


class HealthResponse(BaseModel):
    status: str
    timestamp: str
    models_loaded: bool
    num_models: int
    database_connected: bool


# ===== EVENTOS DE STARTUP/SHUTDOWN =====

@app.on_event("startup")
async def startup_event():
    """Inicializa o serviço de previsão ao iniciar a API"""
    global prediction_service
    try:
        logger.info("🚀 Inicializando serviço de previsão...")
        prediction_service = PredictionService()
        logger.info("✅ Serviço de previsão inicializado com sucesso!")
    except Exception as e:
        logger.error(f"❌ Erro ao inicializar serviço: {e}")
        raise


@app.on_event("shutdown")
async def shutdown_event():
    logger.info("Desligando...")


# Rotas

@app.get("/", tags=["Root"])
async def root():
    return {
        "message": "API MachineLearning CodeNine",
        "version": "1.0.2",
        "docs": "/docs",
        "health": "/health"
    }


@app.get("/health", response_model=HealthResponse, tags=["Health"])
async def health_check():
    num_models = len(prediction_service.models) if prediction_service else 0
    return HealthResponse(
        status="healthy" if prediction_service and num_models > 0 else "unhealthy",
        timestamp=datetime.now().isoformat(),
        models_loaded=num_models > 0,
        num_models=num_models,
        database_connected=prediction_service is not None and prediction_service.db is not None
    )


@app.get("/model/info", response_model=ModelInfo, tags=["Model"])
async def get_model_info():
    if prediction_service is None:
        raise HTTPException(status_code=503, detail="Serviço de previsão não inicializado")
    
    return prediction_service.get_model_info()

# previsão pra todos os itens
@app.get("/predictions/all", response_model=List[PredictionSummary], tags=["Predictions"])
async def get_all_predictions(
    forecast_days: int = Query(30, description="Número de dias para prever"),
    db_url: Optional[str] = Query(None, description="String de conexão do banco (opcional)")
):
    """
    Gera previsões de consumo diário para todos os itens disponíveis.
    Retorna um resumo sem as previsões detalhadas dia a dia.
    """
    if prediction_service is None:
        raise HTTPException(status_code=503, detail="Serviço de previsão não inicializado")
    
    try:
        logger.info(f"📊 Gerando previsões para todos os itens ({forecast_days} dias)...")
        predictions = prediction_service.predict_all_items(
            connection_string=db_url,
            forecast_days=forecast_days
        )
        logger.info(f"✅ {len(predictions)} previsões geradas")
        return predictions
    except Exception as e:
        logger.error(f"❌ Erro ao gerar previsões: {e}")
        raise HTTPException(status_code=500, detail=f"Erro ao gerar previsões: {str(e)}")

# previsão pra 1 item especifico (com detalhes diários)
@app.get("/predictions/item/{item_id}", response_model=PredictionResponse, tags=["Predictions"])
async def get_item_prediction(
    item_id: int,
    forecast_days: int = Query(30, description="Número de dias para prever"),
    db_url: Optional[str] = Query(None, description="String de conexão do banco (opcional)")
):
    """
    Gera previsão detalhada de consumo diário para um item específico.
    Inclui previsões dia a dia e cálculo de estoque futuro.
    """
    if prediction_service is None:
        raise HTTPException(status_code=503, detail="Serviço de previsão não inicializado")
    
    try:
        logger.info(f"📊 Gerando previsão para item {item_id} ({forecast_days} dias)...")
        
        if prediction_service.db is None:
            prediction_service.connect_database(db_url)
        
        prediction = prediction_service.predict_daily_consumption(item_id, forecast_days)
        
        logger.info(f"✅ Previsão gerada para item {item_id}")
        return prediction
        
    except ValueError as ve:
        raise HTTPException(status_code=404, detail=str(ve))
    except Exception as e:
        logger.error(f"Erro ao gerar previsão: {e}")
        raise HTTPException(status_code=500, detail=f"Erro ao gerar previsão: {str(e)}")

# previsão pra múltiplos itens (resumo)
@app.get("/predictions/items", response_model=List[PredictionSummary], tags=["Predictions"])
async def get_multiple_items_predictions(
    item_ids: str = Query(..., description="IDs dos itens separados por vírgula (ex: 1,2,3,4)"),
    forecast_days: int = Query(30, description="Número de dias para prever"),
    db_url: Optional[str] = Query(None, description="String de conexão do banco (opcional)")
):
    """
    Gera previsões de consumo diário para múltiplos itens específicos.
    Retorna resumos sem as previsões detalhadas dia a dia.
    """
    if prediction_service is None:
        raise HTTPException(status_code=503, detail="Serviço de previsão não inicializado")
    
    try:
        try:
            ids = [int(id.strip()) for id in item_ids.split(',')]
        except ValueError:
            raise HTTPException(status_code=400, detail="IDs inválidos. Use formato: 1,2,3")
        
        logger.info(f"📊 Gerando previsões para {len(ids)} itens ({forecast_days} dias)...")
        
        if prediction_service.db is None:
            prediction_service.connect_database(db_url)
        
        predictions = []
        for item_id in ids:
            try:
                pred = prediction_service.predict_daily_consumption(item_id, forecast_days)
                
                # Criar resumo sem daily_predictions
                summary = {
                    'item_id': pred['item_id'],
                    'item_name': pred['item_name'],
                    'current_stock': pred['current_stock'],
                    'minimum_stock': pred['minimum_stock'],
                    'maximum_stock': pred['maximum_stock'],
                    'predicted_daily_consumption': pred['predicted_daily_consumption'],
                    'predicted_total_consumption': pred['predicted_total_consumption'],
                    'predicted_final_stock': pred['predicted_final_stock'],
                    'needs_restock': pred['needs_restock'],
                    'days_until_minimum_stock': pred['days_until_minimum_stock'],
                    'recommended_restock_quantity': pred['recommended_restock_quantity'],
                    'forecast_days': forecast_days,
                    'model_used': pred['model_used'],
                    'timestamp': pred['timestamp']
                }
                predictions.append(summary)
            except Exception as e:
                logger.warning(f"⚠️ Erro ao prever item {item_id}: {e}")
                continue
        
        logger.info(f"✅ {len(predictions)} previsões geradas")
        return predictions
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ Erro ao gerar previsões: {e}")
        raise HTTPException(status_code=500, detail=f"Erro ao gerar previsões: {str(e)}")


# Rota de Teste

@app.get("/test/demo", response_model=PredictionSummary, tags=["Test"])
async def demo_prediction():
    """Retorna uma previsão de exemplo para testes"""
    return PredictionSummary(
        item_id=999,
        item_name="Item de Teste",
        current_stock=500.0,
        minimum_stock=100.0,
        maximum_stock=1000.0,
        predicted_daily_consumption=15.5,
        predicted_total_consumption=465.0,
        predicted_final_stock=35.0,
        needs_restock=True,
        days_until_minimum_stock=25,
        recommended_restock_quantity=965.0,
        forecast_days=30,
        model_used="prophet_daily_consumption",
        timestamp=datetime.now().isoformat()
    )


if __name__ == "__main__":
    import uvicorn
    
    uvicorn.run(
        "api:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )
