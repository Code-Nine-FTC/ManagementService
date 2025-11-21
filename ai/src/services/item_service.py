from typing import Optional, List
from src.training.data_extraction import load_monthly_consumption, get_item_series
from src.training.model_registry import load_model
from src.training.train_item_model import train_single_item
from src.services.prediction_service import PredictionService
import logging

logger = logging.getLogger(__name__)

prediction_service: Optional[PredictionService] = None


def get_consumption_and_forecast(item_id: int, retrain: bool, months_forecast: int, db_url: Optional[str]):
    df = load_monthly_consumption(db_url)
    series = get_item_series(df, item_id)
    if series.empty:
        return None, None, None, None
    payload = load_model(item_id)
    if payload is None and retrain:
        forecast, meta = train_single_item(item_id, db_url, months_forecast)
        payload = {'model': forecast, 'metadata': meta}
    elif payload is None:
        from src.training.forecast import train_forecast
        forecast, meta = train_forecast(series, months_forecast)
        payload = {'model': forecast, 'metadata': meta}
    forecast_series = payload['model']
    metadata = payload['metadata']
    historical = [
        {"month": str(idx), "consumed_quantity": float(val)}
        for idx, val in series.items()
    ]
    forecast = [
        {"month": str(idx), "predicted_consumed_quantity": float(val)}
        for idx, val in forecast_series.items()
    ]
    return item_id, historical, forecast, metadata


def get_all_predictions(db_url: Optional[str]):
    if prediction_service is None:
        raise RuntimeError("Serviço de previsão não inicializado")
    return prediction_service.predict_all_items(connection_string=db_url)


def get_item_prediction(item_id: int, db_url: Optional[str]):
    if prediction_service is None:
        raise RuntimeError("Serviço de previsão não inicializado")
    if prediction_service.db is None:
        prediction_service.connect_database(db_url)
    raw_data = prediction_service.db.load_raw_data()
    historical_data = prediction_service._create_monthly_features(raw_data)
    if item_id not in historical_data['item_id'].values:
        return None
    return prediction_service.predict_next_month(item_id, historical_data)


def get_multiple_items_predictions(item_ids: List[int], db_url: Optional[str]):
    if prediction_service is None:
        raise RuntimeError("Serviço de previsão não inicializado")
    if prediction_service.db is None:
        prediction_service.connect_database(db_url)
    raw_data = prediction_service.db.load_raw_data()
    historical_data = prediction_service._create_monthly_features(raw_data)
    predictions = []
    for item_id in item_ids:
        try:
            if item_id not in historical_data['item_id'].values:
                logger.warning(f"⚠️ Item {item_id} não encontrado")
                continue
            pred = prediction_service.predict_next_month(item_id, historical_data)
            predictions.append(pred)
        except Exception as e:
            logger.warning(f"⚠️ Erro ao prever item {item_id}: {e}")
            continue
    return predictions
