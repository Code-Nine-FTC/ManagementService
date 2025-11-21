from .data_extraction import load_monthly_consumption
from dotenv import load_dotenv
from .forecast import train_forecast
from .model_registry import save_model
from .data_extraction import get_item_series

load_dotenv()


def train_all(connection_string: str | None = None, forecast_periods: int = 3, min_points: int = 2):
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
    return results

if __name__ == '__main__':
    res = train_all()
    print('Treinados:', res.keys())
