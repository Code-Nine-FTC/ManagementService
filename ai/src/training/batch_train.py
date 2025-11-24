import os

from sqlalchemy import create_engine, text
from dotenv import load_dotenv

from .data_extraction import load_monthly_consumption
from .forecast import train_forecast
from .model_registry import save_model
from .data_extraction import get_item_series

from src.training.forecast import train_forecast
from src.training.model_registry import load_model
from src.training.data_extraction import load_monthly_consumption, get_item_series

load_dotenv()

def train_all(connection_string: str | None = None, forecast_periods: int = 3, min_points: int = 2):
    if connection_string is None:
        connection_string = os.getenv("DB_URL")
        if not connection_string:
            raise RuntimeError("DB_URL não configurado nas variáveis de ambiente ou .env")
    df = load_monthly_consumption(connection_string)
    item_ids = sorted(df['item_id'].unique()) if not df.empty else []
    results = {}
    for iid in item_ids:
        series = get_item_series(df, iid)
        if len(series.dropna()) < min_points:
            continue
        forecast, meta = train_forecast(series, forecast_periods)
        meta['history_points'] = len(series)
        meta['last_history_month'] = str(series.index.max())
        save_model(iid, forecast, meta)
        results[iid] = meta
    generate_and_save_stock_predictions(item_ids, connection_string)
    return results


def generate_and_save_stock_predictions(item_ids, connection_string: str | None = None):
    if connection_string is None:
        import os
        connection_string = os.getenv("DB_URL")
        if not connection_string:
            raise RuntimeError("DB_URL não configurado nas variáveis de ambiente ou .env")

    engine = create_engine(connection_string)
    df = load_monthly_consumption(connection_string)
    # Converter todos os ids para int nativo do Python
    item_ids_py = [int(i) for i in item_ids]
    with engine.connect() as conn:
        stock_query = "SELECT id, current_stock FROM items WHERE id = ANY(:ids)"
        result = conn.execute(text(stock_query), {"ids": item_ids_py})
        stock_map = {row.id: float(row.current_stock) for row in result}

        for item_id in item_ids:
            payload = load_model(item_id)
            if payload is None:
                continue
            series = get_item_series(df, item_id)
            if series.empty:
                continue
            forecast, _ = train_forecast(series, forecast_periods=3)
            stock = stock_map.get(item_id, 0.0)
            for idx, (period, predicted_consumption) in enumerate(forecast.items(), start=1):
                stock = stock - float(predicted_consumption)
                prediction_month = int(period.month)
                prediction_year = int(period.year)
                # Deduplicação explícita: remover previsão antiga antes de inserir
                delete_query = text("""
                    DELETE FROM prediction_stock_ai.stock_predictions
                    WHERE item_id = :item_id AND prediction_month = :prediction_month AND prediction_year = :prediction_year
                """)
                conn.execute(delete_query, {
                    "item_id": int(item_id),
                    "prediction_month": prediction_month,
                    "prediction_year": prediction_year
                })
                # Humanização: arredondar valores
                predicted_consumption_rounded = round(float(predicted_consumption), 2)
                predicted_stock_rounded = round(float(stock), 2)
                insert_query = text("""
                    INSERT INTO prediction_stock_ai.stock_predictions (item_id, prediction_month, prediction_year, predicted_consumption, predicted_stock)
                    VALUES (:item_id, :prediction_month, :prediction_year, :predicted_consumption, :predicted_stock)
                """)
                conn.execute(insert_query, {
                    "item_id": int(item_id),
                    "prediction_month": prediction_month,
                    "prediction_year": prediction_year,
                    "predicted_consumption": predicted_consumption_rounded,
                    "predicted_stock": predicted_stock_rounded
                })
        conn.commit()

if __name__ == '__main__':
    res = train_all()
    print('Treinados:', res.keys())
