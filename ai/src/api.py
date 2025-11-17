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


class PredictionResponse(BaseModel):
    """Resposta de previsão para um item"""
    item_id: int
    predicted_quantity: float = Field(..., description="Quantidade prevista para o próximo mês")
    current_stock: float = Field(..., description="Estoque atual")
    minimum_stock: float = Field(..., description="Estoque mínimo")
    maximum_stock: float = Field(..., description="Estoque máximo")
    prediction_month: int = Field(..., description="Mês da previsão (1-12)")
    prediction_year: int = Field(..., description="Ano da previsão")
    needs_restock: bool = Field(..., description="Se precisa repor estoque")
    restock_quantity: float = Field(..., description="Quantidade a repor")
    confidence_score: float = Field(..., description="Score de confiança do modelo (R²)")
    model_used: str = Field(..., description="Nome do modelo usado")
    timestamp: str = Field(..., description="Timestamp da previsão")
    
    class Config:
        json_schema_extra = {
            "example": {
                "item_id": 1,
                "predicted_quantity": 150.5,
                "current_stock": 100.0,
                "minimum_stock": 50.0,
                "maximum_stock": 300.0,
                "prediction_month": 12,
                "prediction_year": 2024,
                "needs_restock": True,
                "restock_quantity": 50.5,
                "confidence_score": 0.94,
                "model_used": "gradient_boosting",
                "timestamp": "2024-11-03T10:30:00"
            }
        }


class ModelInfo(BaseModel):
    model_loaded: bool
    model_type: Optional[str]
    num_features: int
    metadata: dict


class HealthResponse(BaseModel):
    status: str
    timestamp: str
    model_loaded: bool
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
    return HealthResponse(
        status="healthy" if prediction_service else "unhealthy",
        timestamp=datetime.now().isoformat(),
        model_loaded=prediction_service is not None and prediction_service.model is not None,
        database_connected=prediction_service is not None and prediction_service.db is not None
    )


@app.get("/model/info", response_model=ModelInfo, tags=["Model"])
async def get_model_info():
    if prediction_service is None:
        raise HTTPException(status_code=503, detail="Serviço de previsão não inicializado")
    
    return prediction_service.get_model_info()

# previsão pra todos os itens
@app.get("/predictions/all", response_model=List[PredictionResponse], tags=["Predictions"])
async def get_all_predictions(
    db_url: Optional[str] = Query(None, description="String de conexão do banco (opcional)")
):
    if prediction_service is None:
        raise HTTPException(status_code=503, detail="Serviço de previsão não inicializado")
    
    try:
        logger.info("📊 Gerando previsões para todos os itens...")
        predictions = prediction_service.predict_all_items(connection_string=db_url)
        logger.info(f"✅ {len(predictions)} previsões geradas")
        return predictions
    except Exception as e:
        logger.error(f"❌ Erro ao gerar previsões: {e}")
        raise HTTPException(status_code=500, detail=f"Erro ao gerar previsões: {str(e)}")

# previsão pra 1 item especifico
@app.get("/predictions/item/{item_id}", response_model=PredictionResponse, tags=["Predictions"])
async def get_item_prediction(
    item_id: int,
    db_url: Optional[str] = Query(None, description="String de conexão do banco (opcional)")
):
    if prediction_service is None:
        raise HTTPException(status_code=503, detail="Serviço de previsão não inicializado")
    
    try:
        logger.info(f"📊 Gerando previsão para item {item_id}...")
        
        if prediction_service.db is None:
            prediction_service.connect_database(db_url)
        
        raw_data = prediction_service.db.load_raw_data()
        historical_data = prediction_service._create_monthly_features(raw_data)
        
        if item_id not in historical_data['item_id'].values:
            raise HTTPException(status_code=404, detail=f"Item {item_id} não encontrado no banco")
        
        prediction = prediction_service.predict_next_month(item_id, historical_data)
        
        logger.info(f"✅ Previsão gerada para item {item_id}")
        return prediction
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Erro ao gerar previsão: {e}")
        raise HTTPException(status_code=500, detail=f"Erro ao gerar previsão: {str(e)}")

# previsão pra múltiplos itens
@app.get("/predictions/items", response_model=List[PredictionResponse], tags=["Predictions"])
async def get_multiple_items_predictions(
    item_ids: str = Query(..., description="IDs dos itens separados por vírgula (ex: 1,2,3,4)"),
    db_url: Optional[str] = Query(None, description="String de conexão do banco (opcional)")
):
    if prediction_service is None:
        raise HTTPException(status_code=503, detail="Serviço de previsão não inicializado")
    
    try:
        try:
            ids = [int(id.strip()) for id in item_ids.split(',')]
        except ValueError:
            raise HTTPException(status_code=400, detail="IDs inválidos. Use formato: 1,2,3")
        
        logger.info(f"📊 Gerando previsões para {len(ids)} itens...")
        
        if prediction_service.db is None:
            prediction_service.connect_database(db_url)
        
        raw_data = prediction_service.db.load_raw_data()
        historical_data = prediction_service._create_monthly_features(raw_data)
        
        predictions = []
        for item_id in ids:
            try:
                if item_id not in historical_data['item_id'].values:
                    logger.warning(f"⚠️ Item {item_id} não encontrado")
                    continue
                
                pred = prediction_service.predict_next_month(item_id, historical_data)
                predictions.append(pred)
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

@app.get("/test/demo", tags=["Test"])
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


if __name__ == "__main__":
    import uvicorn
    
    uvicorn.run(
        "api:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )
