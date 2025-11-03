import os
import sys
import time
from datetime import datetime, timedelta, date

import numpy as np
import pandas as pd
import pytz
from apscheduler.schedulers.background import BackgroundScheduler
from sqlalchemy import create_engine, text
from sqlalchemy.engine import Engine
from dotenv import load_dotenv

try:
    import statsmodels.api as sm
except Exception as e:
    sm = None

TZ = os.environ.get("TZ", "UTC")

def get_env_int(name: str, default: int) -> int:
    try:
        return int(os.environ.get(name, str(default)))
    except Exception:
        return default


def get_env_str(name: str, default: str) -> str:
    val = os.environ.get(name)
    return val if val is not None and str(val).strip() != "" else default


def get_engine() -> Engine:
    host = os.environ.get("PGHOST", "db")
    port = os.environ.get("PGPORT", "5432")
    db = os.environ.get("PGDATABASE", "teste")
    user = os.environ.get("PGUSER", "postgres")
    pwd = os.environ.get("PGPASSWORD", "fatec")
    url = f"postgresql+psycopg2://{user}:{pwd}@{host}:{port}/{db}"
    return create_engine(url)

def load_series_from_postgres(engine: Engine, start: datetime, end: datetime) -> pd.DataFrame:
    df = pd.read_sql(text(SERIES_SQL), engine, params={"start": start, "end": end})
    if df.empty:
        return df
    df["dia"] = pd.to_datetime(df["dia"]).dt.tz_localize("UTC")
    return df


def load_series_from_file(
    path: str,
    file_format: str,
    col_item: str,
    col_date: str,
    col_qty: str,
    start: datetime,
    end: datetime,
    tz_name: str,
) -> pd.DataFrame:
    if file_format.lower() == "csv":
        raw = pd.read_csv(path)
    elif file_format.lower() == "parquet":  # Apache Parquet
        raw = pd.read_parquet(path)
    else:
        raise ValueError(f"Formato de arquivo não suportado: {file_format}")

    if col_item not in raw.columns or col_date not in raw.columns or col_qty not in raw.columns:
        raise ValueError(
            f"Colunas requeridas não encontradas no arquivo. Esperado: {col_item}, {col_date}, {col_qty}. Encontrado: {list(raw.columns)}"
        )

    df = raw[[col_item, col_date, col_qty]].copy()
    df.columns = ["item_id", "dia", "quantidade"]
    # parse datas
    df["dia"] = pd.to_datetime(df["dia"], errors="coerce")
    df = df.dropna(subset=["dia"])  # remove datas inválidas
    # localiza timezone e recorta janelas
    if df["dia"].dt.tz is None:
        df["dia"] = df["dia"].dt.tz_localize(tz_name)
    else:
        df["dia"] = df["dia"].dt.tz_convert(tz_name)
    mask = (df["dia"] >= start) & (df["dia"] <= end)
    df = df.loc[mask]
    if df.empty:
        return df

    # Normaliza tipos e agrega por dia/item
    df["quantidade"] = pd.to_numeric(df["quantidade"], errors="coerce").fillna(0.0)
    df = (
        df.assign(dia=df["dia"].dt.tz_convert("UTC").dt.tz_localize(None))
          .groupby(["item_id", df["dia"].dt.tz_convert("UTC").dt.date], as_index=False)["quantidade"].sum()
    )
    df.rename(columns={"dia": "dia", "quantidade": "quantidade"}, inplace=True)
    # Reajusta a coluna de dia para datetime com tz UTC
    df["dia"] = pd.to_datetime(df["dia"]).dt.tz_localize("UTC")
    return df[["item_id", "dia", "quantidade"]]


def load_daily_series(
    engine: Engine | None,
    start: datetime,
    end: datetime,
) -> pd.DataFrame:
    source = get_env_str("TRAIN_INPUT_SOURCE", "postgres").lower()
    if source == "postgres":
        if engine is None:
            raise ValueError("Engine obrigatório para TRAIN_INPUT_SOURCE=postgres")
        return load_series_from_postgres(engine, start, end)
    else:
        path = get_env_str("TRAIN_INPUT_PATH", "")
        if not path:
            raise ValueError("TRAIN_INPUT_PATH é obrigatório quando TRAIN_INPUT_SOURCE != postgres")
        fmt = get_env_str("TRAIN_INPUT_FORMAT", "csv")
        col_item = get_env_str("INPUT_COL_ITEM", "item_id")
        col_date = get_env_str("INPUT_COL_DATE", "date")
        col_qty = get_env_str("INPUT_COL_QTY", "quantity")
        tz_name = get_env_str("INPUT_DATE_TZ", TZ)
        return load_series_from_file(path, fmt, col_item, col_date, col_qty, start, end, tz_name)


def fit_predict_series(series: pd.Series, horizon: int) -> float:
    """Prevê soma dos próximos `horizon` passos para uma série diária.
    Tenta SARIMAX; se falhar, cai para média móvel.
    """
    y = series.astype(float)
    if y.notna().sum() < 10:
        # muito pouco dado
        daily = y.fillna(0.0).mean()
        return float(max(0.0, daily) * horizon)

    # SARIMAX simples com sazonalidade semanal (7)
    if sm is not None:
        try:
            # d e D pequenos para estabilidade com dados esparsos
            model = sm.tsa.statespace.SARIMAX(
                y.fillna(0.0),
                order=(1, 0, 0),
                seasonal_order=(0, 1, 1, 7),
                enforce_stationarity=False,
                enforce_invertibility=False,
            )
            res = model.fit(disp=False)
            fc = res.get_forecast(steps=horizon)
            mean = fc.predicted_mean.clip(lower=0.0)
            return float(mean.sum())
        except Exception:
            pass

    # fallback: média móvel dos últimos 30 dias (ou tamanho da série)
    window = min(30, len(y))
    daily = (
        y.fillna(0.0).rolling(window=window, min_periods=max(5, window // 2)).mean().iloc[-1]
    )
    daily = float(daily) if not np.isnan(daily) else float(y.fillna(0.0).mean())
    return float(max(0.0, daily) * horizon)


def train_once():
    tz = pytz.timezone(TZ)
    now = datetime.now(tz)
    ref_date = now.date()

    train_window_days = get_env_int("TRAIN_WINDOW_DAYS", 120)
    horizon = get_env_int("FORECAST_HORIZON_DAYS", 14)
    model_version = os.environ.get("MODEL_VERSION", "sarimax_v1")
    force = os.environ.get("TRAIN_FORCE", "false").lower() in ("1", "true", "yes", "on")
    target = get_env_str("TRAIN_TARGET", "database").lower()  # database | file

    start = (now - timedelta(days=train_window_days)).replace(hour=0, minute=0, second=0, microsecond=0)
    end = now.replace(hour=0, minute=0, second=0, microsecond=0)

    engine = None
    if target == "database" or get_env_str("TRAIN_INPUT_SOURCE", "postgres").lower() == "postgres":
        engine = get_engine()

    if target == "database":
        with engine.begin() as conn:
            # garante tabela/índices
            conn.execute(text(CREATE_TABLE_SQL))
            # se já existe previsão para hoje e não for forçado, pula
            exists = conn.execute(
                text("SELECT COUNT(1) FROM model_predictions WHERE ref_date = :ref_date AND horizon_days = :h"),
                {"ref_date": ref_date, "h": horizon},
            ).scalar()
            if (exists or 0) > 0 and not force:
                print(f"[trainer] Já existem previsões para ref_date={ref_date}, horizon={horizon}. Pulando (TRAIN_FORCE=false).")
                return
    else:
        # saída em arquivo
        out_dir = get_env_str("OUTPUT_DIR", ".")
        os.makedirs(out_dir, exist_ok=True)
        out_path = os.path.join(out_dir, f"predictions_{ref_date}_h{horizon}.csv")
        if os.path.exists(out_path) and not force:
            print(f"[trainer] Arquivo de saída já existe: {out_path}. Pulando (TRAIN_FORCE=false).")
            return

    df = load_daily_series(engine, start, end)
    if df.empty:
        print("[trainer] Sem dados para o período, nada a fazer.")
        return

    # Garante grade diária para cada item (preenchendo dias sem movimento com 0)
    results = []
    for item_id, g in df.groupby("item_id"):
        g = g.sort_values("dia").set_index("dia")["quantidade"].asfreq("D", fill_value=0.0)
        y_hat = fit_predict_series(g, horizon)
        results.append({
            "item_id": item_id,
            "ref_date": ref_date,
            "h": horizon,
            "y_hat": float(y_hat),
            "model_version": model_version,
        })

    if target == "database":
        # assegura que item_id é numérico para persistir
        try:
            for r in results:
                r["item_id"] = int(r["item_id"])
        except Exception:
            raise ValueError("item_id não numérico na fonte; para TRAIN_TARGET=database os IDs devem ser inteiros.")

        with engine.begin() as conn:
            # limpa previsões da mesma rodada e horizonte (idempotência) - garantir que operações não sejam duplicadas
            conn.execute(text(DELETE_SQL), {"ref_date": ref_date, "h": horizon})
            # insere
            for r in results:
                conn.execute(text(INSERT_SQL), r)
    else:
        out_dir = get_env_str("OUTPUT_DIR", ".")
        os.makedirs(out_dir, exist_ok=True)
        out_path = os.path.join(out_dir, f"predictions_{ref_date}_h{horizon}.csv")
        out_df = pd.DataFrame(results)[["item_id", "ref_date", "h", "y_hat", "model_version"]]
        out_df.rename(columns={"h": "horizon_days"}, inplace=True)
        out_df.to_csv(out_path, index=False)
        print(f"[trainer] Previsões salvas em arquivo: {out_path}")

    print(f"[trainer] {len(results)} previsões geradas para ref_date={ref_date} horizon={horizon}.")


def evaluate_matured_predictions():
    """Calcula métricas para previsões cujo horizonte já venceu (ref_date + horizon <= hoje)."""
    tz = pytz.timezone(TZ)
    today = datetime.now(tz).date()
    engine = get_engine()

    with engine.begin() as conn:
        conn.execute(text(CREATE_METRICS_SQL))

        # Seleciona previsões maduras e ainda não avaliadas
        rows = conn.execute(
            text(
                """
                SELECT mp.item_id, mp.ref_date, mp.horizon_days, mp.y_hat
                FROM model_predictions mp
                WHERE (mp.ref_date + mp.horizon_days) <= :today
                  AND NOT EXISTS (
                        SELECT 1 FROM prediction_metrics pm
                        WHERE pm.item_id = mp.item_id
                          AND pm.ref_date = mp.ref_date
                          AND pm.horizon_days = mp.horizon_days
                  )
                ORDER BY mp.ref_date ASC
                """
            ),
            {"today": today},
        ).fetchall()

        if not rows:
            print("[evaluate] Nenhuma previsão madura para avaliar.")
            return

        inserted = 0
        for item_id, ref_date, h, y_hat in rows:
            # y_true: soma de consumo realizado no intervalo [ref_date, ref_date + h - 1]
            y_true = conn.execute(
                text(
                    """
                    SELECT COALESCE(SUM(oi.quantity), 0) AS y_true
                    FROM order_item oi
                    JOIN orders o ON o.id = oi.order_id
                    WHERE oi.item_id = :item_id
                      AND DATE_TRUNC('day', COALESCE(o.withdraw_day, o.created_at))::date
                          BETWEEN :start AND :end
                    """
                ),
                {"item_id": item_id, "start": ref_date, "end": ref_date + timedelta(days=int(h) - 1)},
            ).scalar()

            abs_error = float(abs(float(y_hat) - float(y_true)))
            ape = None
            try:
                ape = float(abs_error / float(y_true) * 100.0) if float(y_true) != 0.0 else None
            except Exception:
                ape = None

            conn.execute(
                text(
                    """
                    INSERT INTO prediction_metrics (item_id, ref_date, horizon_days, y_hat, y_true, abs_error, ape)
                    VALUES (:item_id, :ref_date, :h, :y_hat, :y_true, :abs_error, :ape)
                    """
                ),
                {
                    "item_id": item_id,
                    "ref_date": ref_date,
                    "h": int(h),
                    "y_hat": float(y_hat),
                    "y_true": float(y_true),
                    "abs_error": abs_error,
                    "ape": ape,
                },
            )
            inserted += 1

    print(f"[evaluate] Métricas inseridas para {inserted} previsões maduras.")


def evaluate_round_metrics():
    """Calcula métricas agregadas por rodada (ref_date, horizon_days) usando prediction_metrics.
    Requer que prediction_metrics já tenha pares (y_true, y_hat) inseridos por item.
    """
    tz = pytz.timezone(TZ)
    today = datetime.now(tz).date()
    engine = get_engine()

    with engine.begin() as conn:
        conn.execute(text(CREATE_ROUND_METRICS_SQL))

        # Agrega métricas por (ref_date, horizon_days) apenas para rodadas maduras
        inserted = conn.execute(
            text(
                """
                WITH rounds AS (
                    SELECT DISTINCT ref_date, horizon_days
                    FROM model_predictions
                    WHERE (ref_date + horizon_days) <= :today
                ),
                agg AS (
                    SELECT
                        pm.ref_date,
                        pm.horizon_days,
                        AVG(pm.abs_error) AS mae,
                        sqrt(AVG(POWER((pm.y_hat - pm.y_true)::double precision, 2))) AS rmse,
                        AVG(pm.ape) FILTER (WHERE pm.ape IS NOT NULL) AS mape,
                        AVG(
                            CASE WHEN (abs(pm.y_hat) + abs(pm.y_true)) > 0
                                 THEN 2 * abs(pm.y_hat - pm.y_true) / (abs(pm.y_hat) + abs(pm.y_true)) * 100
                                 ELSE NULL END
                        ) AS smape,
                        CASE WHEN SUM(abs(pm.y_true)) > 0
                             THEN SUM(pm.abs_error) / SUM(abs(pm.y_true)) * 100
                             ELSE NULL END AS wape,
                        COUNT(*) AS pairs
                    FROM prediction_metrics pm
                    JOIN rounds r ON r.ref_date = pm.ref_date AND r.horizon_days = pm.horizon_days
                    GROUP BY pm.ref_date, pm.horizon_days
                )
                INSERT INTO model_prediction_metrics (ref_date, horizon_days, mae, rmse, mape, smape, wape, pairs)
                SELECT ref_date, horizon_days, mae, rmse, mape, smape, wape, pairs FROM agg
                ON CONFLICT (ref_date, horizon_days)
                DO UPDATE SET
                    mae = EXCLUDED.mae,
                    rmse = EXCLUDED.rmse,
                    mape = EXCLUDED.mape,
                    smape = EXCLUDED.smape,
                    wape = EXCLUDED.wape,
                    pairs = EXCLUDED.pairs,
                    created_at = NOW()
                RETURNING 1;
                """
            ),
            {"today": today},
        ).rowcount

    print(f"[evaluate] Métricas agregadas atualizadas para {inserted or 0} rodada(s).")


def run_scheduler():
    # Crontab-like no formato: M H DOM M DOW (minuto hora ...)
    # Ex.: "10 2 * * *" -> 02:10 todos os dias
    cron_expr = os.environ.get("SCHEDULE_CRON", "10 2 * * *").strip()
    parts = cron_expr.split()
    if len(parts) != 5:
        print("SCHEDULE_CRON inválido, executando uma vez e saindo.")
        train_once()
        return

    scheduler = BackgroundScheduler(timezone=TZ)
    scheduler.add_job(train_once, "cron",
                      minute=parts[0], hour=parts[1], day=parts[2], month=parts[3], day_of_week=parts[4])
    scheduler.start()
    print(f"[trainer] agendado com CRON='{cron_expr}' (tz={TZ}). Pressione Ctrl+C para sair.")
    try:
        while True:
            time.sleep(3600)
    except KeyboardInterrupt:
        scheduler.shutdown()


if __name__ == "__main__":
    load_dotenv()
    mode = os.environ.get("MODE", "once").lower()

    if mode == "schedule":
        # Agenda o treino diário; também avalia previsões maduras e agrega métricas ao iniciar
        evaluate_matured_predictions()
        evaluate_round_metrics()
        run_scheduler()
    elif mode == "report":
        # Relatório simples das últimas previsões (para o horizonte configurado)
        horizon = get_env_int("FORECAST_HORIZON_DAYS", 14)
        target = get_env_str("TRAIN_TARGET", "database").lower()
        if target == "database":
            engine = get_engine()
            with engine.begin() as conn:
                last_ref = conn.execute(
                    text("SELECT max(ref_date) FROM model_predictions WHERE horizon_days = :h"),
                    {"h": horizon},
                ).scalar()
                if not last_ref:
                    print(f"[report] Sem previsões para horizon={horizon}.")
                else:
                    total = conn.execute(
                        text(
                            "SELECT COUNT(*) FROM model_predictions WHERE ref_date = :r AND horizon_days = :h"
                        ),
                        {"r": last_ref, "h": horizon},
                    ).scalar()
                    print(f"[report] ref_date={last_ref} horizon={horizon} total={total}")
                    # Se existir métrica agregada da rodada, exibe
                    met = conn.execute(
                        text(
                            """
                            SELECT mae, rmse, mape, smape, wape, pairs
                            FROM model_prediction_metrics
                            WHERE ref_date = :r AND horizon_days = :h
                            """
                        ),
                        {"r": last_ref, "h": horizon},
                    ).fetchone()
                    if met:
                        mae, rmse, mape, smape, wape, pairs = met
                        print(
                            f"[report] métricas: pairs={pairs} MAE={mae:.2f} RMSE={rmse:.2f} "
                            f"MAPE={(mape if mape is not None else float('nan')):.2f}% sMAPE={(smape if smape is not None else float('nan')):.2f}% WAPE={(wape if wape is not None else float('nan')):.2f}%"
                        )
                    rows = conn.execute(
                        text(
                            """
                            SELECT item_id, y_hat
                            FROM model_predictions
                            WHERE ref_date = :r AND horizon_days = :h
                            ORDER BY y_hat DESC
                            LIMIT 10
                            """
                        ),
                        {"r": last_ref, "h": horizon},
                    ).fetchall()
                    for item_id, y_hat in rows:
                        print(f"  item_id={item_id} y_hat={y_hat}")
        else:
            tz = pytz.timezone(TZ)
            today = datetime.now(tz).date()
            out_dir = get_env_str("OUTPUT_DIR", ".")
            out_path = os.path.join(out_dir, f"predictions_{today}_h{horizon}.csv")
            if os.path.exists(out_path):
                df = pd.read_csv(out_path)
                print(f"[report] arquivo: {out_path} total={len(df)}")
                head = df.sort_values("y_hat", ascending=False).head(10)
                for _, row in head.iterrows():
                    print(f"  item_id={row['item_id']} y_hat={row['y_hat']}")
            else:
                print(f"[report] Sem arquivo de previsão encontrado: {out_path}")
    elif mode == "evaluate":
        evaluate_matured_predictions()
        evaluate_round_metrics()
    else:
        train_once()
