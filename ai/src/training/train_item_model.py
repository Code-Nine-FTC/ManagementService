from .data_extraction import load_monthly_consumption, get_item_series
from .forecast import train_forecast
from .model_registry import save_model


def train_single_item(item_id: int, connection_string: str | None = None, forecast_periods: int = 3):
    df = load_monthly_consumption(connection_string)
    series = get_item_series(df, item_id)
    if series.empty:
        raise ValueError(f"Sem dados de consumo para item {item_id}")
    forecast, meta = train_forecast(series, forecast_periods)
    meta['history_points'] = len(series)
    meta['last_history_month'] = str(series.index.max())
    save_model(item_id, forecast, meta)
    return forecast, meta

if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser(description='Treina modelo de consumo para um item específico.')
    parser.add_argument('item_id', type=int, help='ID do item')
    parser.add_argument('--forecast_periods', type=int, default=3, help='Meses a prever')
    parser.add_argument('--db', type=str, default=None, help='String de conexão opcional')
    args = parser.parse_args()
    fcast, meta = train_single_item(args.item_id, args.db, args.forecast_periods)
    print('Forecast salvo:', fcast)
    print('Meta:', meta)
