import pandas as pd

from typing import Tuple
from statsmodels.tsa.statespace.sarimax import SARIMAX


def train_forecast(series: pd.Series, forecast_periods: int = 3) -> Tuple[pd.Series, dict]:
    """Treina modelo SARIMAX simples ou retorna média móvel se série for curta."""
    series = series.astype(float)
    meta = {}
    if len(series.dropna()) < 4:
        avg = series.mean() if not series.empty else 0.0
        forecast_index = pd.period_range(series.index.max() + 1, periods=forecast_periods, freq='M')
        forecast = pd.Series([avg] * forecast_periods, index=forecast_index)
        meta['model'] = 'mean_fallback'
        meta['avg'] = avg
        return forecast, meta
    try:
        model = SARIMAX(series, order=(1,1,1), seasonal_order=(0,0,0,0), trend='n', enforce_stationarity=False, enforce_invertibility=False)
        fitted = model.fit(disp=False)
        forecast_index = pd.period_range(series.index.max() + 1, periods=forecast_periods, freq='M')
        preds = fitted.forecast(steps=forecast_periods)
        preds.index = forecast_index
        meta['model'] = 'sarimax'
        meta['aic'] = fitted.aic
        meta['bic'] = fitted.bic
        return preds, meta
    except Exception as e:
        avg = series.mean()
        forecast_index = pd.period_range(series.index.max() + 1, periods=forecast_periods, freq='M')
        forecast = pd.Series([avg] * forecast_periods, index=forecast_index)
        meta['model'] = 'mean_fallback_error'
        meta['error'] = str(e)
        return forecast, meta
