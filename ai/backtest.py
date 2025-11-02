import os
from datetime import datetime, timedelta

import numpy as np
import pandas as pd
import pytz
from sqlalchemy import text

from train_forecast import (
    get_engine,
    load_daily_series,
    fit_predict_series,
    get_env_int,
    TZ,
)


def mape_safe(y_true, y_pred):
    y_true = np.asarray(y_true, dtype=float)
    y_pred = np.asarray(y_pred, dtype=float)
    mask = y_true != 0
    if mask.sum() == 0:
        return np.nan
    return np.mean(np.abs((y_true[mask] - y_pred[mask]) / y_true[mask])) * 100.0


def smape(y_true, y_pred):
    y_true = np.asarray(y_true, dtype=float)
    y_pred = np.asarray(y_pred, dtype=float)
    denom = (np.abs(y_true) + np.abs(y_pred))
    mask = denom != 0
    if mask.sum() == 0:
        return np.nan
    return np.mean(2.0 * np.abs(y_pred[mask] - y_true[mask]) / denom[mask]) * 100.0


def wape(y_true, y_pred):
    y_true = np.asarray(y_true, dtype=float)
    y_pred = np.asarray(y_pred, dtype=float)
    denom = np.sum(np.abs(y_true))
    if denom == 0:
        return np.nan
    return np.sum(np.abs(y_pred - y_true)) / denom * 100.0


def backtest():
    tz = pytz.timezone(TZ)
    now = datetime.now(tz)

    # Parâmetros do backtest
    horizon = get_env_int("BACKTEST_HORIZON_DAYS", get_env_int("FORECAST_HORIZON_DAYS", 14))
    train_window_days = get_env_int("BACKTEST_TRAIN_WINDOW_DAYS", get_env_int("TRAIN_WINDOW_DAYS", 120))
    eval_span_days = get_env_int("BACKTEST_EVAL_SPAN_DAYS", horizon * 4)  # por padrão, ~4 janelas
    step_days = get_env_int("BACKTEST_STEP_DAYS", horizon)

    # Janela de dados ampla para carregar séries
    total_window = train_window_days + eval_span_days + 7  # margem de segurança
    start = (now - timedelta(days=total_window)).replace(hour=0, minute=0, second=0, microsecond=0)
    end = now.replace(hour=0, minute=0, second=0, microsecond=0)

    engine = get_engine()
    df = load_daily_series(engine, start, end)
    if df.empty:
        print("[backtest] Sem dados de histórico suficientes.")
        return

    # Reamostrar por item em grade diária
    by_item = {}
    for item_id, g in df.groupby("item_id"):
        s = g.sort_values("dia").set_index("dia")["quantidade"].asfreq("D", fill_value=0.0)
        by_item[int(item_id)] = s

    origins = []
    origin = end - timedelta(days=eval_span_days)
    while origin + timedelta(days=horizon) <= end:
        origins.append(origin)
        origin += timedelta(days=step_days)

    y_true_all, y_pred_all = [], []
    pairs = 0

    for item_id, s in by_item.items():
        if len(s) < max(30, horizon + 7):
            continue
        for ori in origins:
            train_start = ori - timedelta(days=train_window_days)
            train_slice = s.loc[train_start: ori - timedelta(days=1)]
            if train_slice.notna().sum() < 10:
                continue
            yhat = fit_predict_series(train_slice, horizon)
            test_slice = s.loc[ori: ori + timedelta(days=horizon - 1)]
            ytrue = float(test_slice.sum())
            if np.isnan(ytrue):
                continue
            y_pred_all.append(yhat)
            y_true_all.append(ytrue)
            pairs += 1

    if pairs == 0:
        print("[backtest] Não foi possível formar pares treino/teste.")
        return

    y_true_arr = np.asarray(y_true_all)
    y_pred_arr = np.asarray(y_pred_all)
    errors = y_pred_arr - y_true_arr

    mae = float(np.mean(np.abs(errors)))
    rmse = float(np.sqrt(np.mean(errors ** 2)))
    mape = float(mape_safe(y_true_arr, y_pred_arr)) if not np.isnan(mape_safe(y_true_arr, y_pred_arr)) else float("nan")
    smape_v = float(smape(y_true_arr, y_pred_arr)) if not np.isnan(smape(y_true_arr, y_pred_arr)) else float("nan")
    wape_v = float(wape(y_true_arr, y_pred_arr)) if not np.isnan(wape(y_true_arr, y_pred_arr)) else float("nan")

    print("[backtest] resultados")
    print(f"  pares_avaliados={pairs}")
    print(f"  horizon={horizon} train_window_days={train_window_days}")
    print(f"  MAE={mae:.2f}")
    print(f"  RMSE={rmse:.2f}")
    print(f"  MAPE={mape:.2f}%")
    print(f"  sMAPE={smape_v:.2f}%")
    print(f"  WAPE={wape_v:.2f}%")


if __name__ == "__main__":
    backtest()
