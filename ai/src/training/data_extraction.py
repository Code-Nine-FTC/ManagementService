import os
from typing import Optional
import pandas as pd
from sqlalchemy import create_engine, text

CONSUMPTION_QUERY = """
SELECT 
    oi.item_id AS item_id,
    DATE_TRUNC('month', o.created_at) AS month,
    SUM(oi.quantity) AS consumed_quantity
FROM order_item oi
JOIN orders o ON o.id = oi.order_id
WHERE o.status = 'COMPLETED'
GROUP BY oi.item_id, DATE_TRUNC('month', o.created_at)
ORDER BY oi.item_id, month;
"""


def get_engine(connection_string: Optional[str] = None):
    if connection_string:
        return create_engine(connection_string)
    db_url = os.getenv("DB_URL")
    db_user = os.getenv("DB_USER")
    db_password = os.getenv("DB_PASSWORD")
    if not (db_url and db_user and db_password):
        raise RuntimeError("Variáveis de ambiente DB_URL, DB_USER, DB_PASSWORD ausentes para conexão.")
    if db_url.startswith("jdbc:"):
        db_url_clean = db_url.replace("jdbc:", "")
    else:
        db_url_clean = db_url
    return create_engine(f"{db_url_clean}", connect_args={})


def load_monthly_consumption(connection_string: Optional[str] = None) -> pd.DataFrame:
    engine = get_engine(connection_string)
    with engine.connect() as conn:
        df = pd.read_sql(text(CONSUMPTION_QUERY), conn)
    if df.empty:
        return pd.DataFrame(columns=["item_id", "month", "consumed_quantity"])
    df['month'] = pd.to_datetime(df['month']).dt.to_period('M')
    return df


def pivot_monthly(df: pd.DataFrame) -> pd.DataFrame:
    if df.empty:
        return df
    return df.pivot_table(index='month', columns='item_id', values='consumed_quantity', fill_value=0)


def get_item_series(df: pd.DataFrame, item_id: int) -> pd.Series:
    item_df = df[df['item_id'] == item_id]
    if item_df.empty:
        return pd.Series(dtype=float)
    s = item_df.set_index('month')['consumed_quantity'].sort_index()
    return s.asfreq('M').fillna(0)

if __name__ == "__main__":
    data = load_monthly_consumption()
    print(data.head())
