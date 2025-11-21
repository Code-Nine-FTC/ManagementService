from pydantic import BaseModel, Field
from typing import List, Optional

class PredictionResponse(BaseModel):
    item_id: int
    predicted_quantity: float
    current_stock: float
    minimum_stock: float
    maximum_stock: float
    prediction_month: int
    prediction_year: int
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