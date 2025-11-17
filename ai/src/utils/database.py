from sqlalchemy import create_engine, text
from datetime import datetime
from typing import List, Dict, Any
import os


class DatabaseConnector:    
    def __init__(self, connection_string: str = None):
        if connection_string is None:
            connection_string = self._build_connection_string()
        
        self.engine = create_engine(connection_string)
    
    @staticmethod
    def _build_connection_string() -> str:
        host = os.getenv('PGHOST', 'localhost')
        port = os.getenv('PGPORT', '5433')
        database = os.getenv('PGDATABASE', 'teste')
        user = os.getenv('PGUSER', 'postgres')
        password = os.getenv('PGPASSWORD', 'fatec')
        
        return f'postgresql://{user}:{password}@{host}:{port}/{database}'
    
    def save_predictions(self, predictions: List[Dict[str, Any]]):
        with self.engine.connect() as conn:
            current_month = datetime.now().month
            
            for pred in predictions:
                item_id = pred.get('item_id')
                # permitir informar explicitamente o mês alvo (YYYY-MM) ou usar mês atual
                target_month = pred.get('target_month')
                if target_month:
                    # tentar extrair mês numérico se for YYYY-MM
                    try:
                        month_val = int(str(target_month).split('-')[1])
                    except Exception:
                        month_val = current_month
                else:
                    month_val = current_month
                
                # Salvar previsão de estoque
                if 'stock_predicted' in pred:
                    conn.execute(text("""
                        INSERT INTO item_prediction (item_id, month, forecast_type, value, created_at)
                        VALUES (:item_id, :month, 'stock_quantity', :value, :created_at)
                    """), {
                        'item_id': item_id,
                        'month': month_val,
                        'value': pred['stock_predicted'],
                        'created_at': datetime.now()
                    })
                
                # Salvar previsão de pedidos
                if 'orders_predicted' in pred:
                    conn.execute(text("""
                        INSERT INTO item_prediction (item_id, month, forecast_type, value, created_at)
                        VALUES (:item_id, :month, 'orders_placed', :value, :created_at)
                    """), {
                        'item_id': item_id,
                        'month': month_val,
                        'value': pred['orders_predicted'],
                        'created_at': datetime.now()
                    })
                
                # Salvar previsão de consumo
                if 'consumption_predicted' in pred:
                    conn.execute(text("""
                        INSERT INTO item_prediction (item_id, month, forecast_type, value, created_at)
                        VALUES (:item_id, :month, 'average_consumed', :value, :created_at)
                    """), {
                        'item_id': item_id,
                        'month': month_val,
                        'value': pred['consumption_predicted'],
                        'created_at': datetime.now()
                    })
            
            conn.commit()
        
        print(f"✅ {len(predictions)} previsões salvas no banco de dados!")
    
    # LEGADO - NÃO USADO ATUALMENTE
    # def load_snapshots(self, limit: int = None) -> 'pd.DataFrame':
    #     import pandas as pd
        
    #     print("🗄️  Carregando dados do banco de dados (snapshots)...")
    #     query = """
    #     SELECT 
    #         ims.id,
    #         ims.item_id,
    #         ims.year_month,
    #         ims.stock_quantity,
    #         ims.orders_placed,
    #         ims.average_consumed,
    #         ims.created_at,
    #         i.name as item_name,
    #         i.minimum_stock,
    #         i.maximum_stock
    #     FROM item_monthly_snapshot ims
    #     LEFT JOIN items i ON ims.item_id = i.id
    #     ORDER BY ims.created_at DESC
    #     """
    #     if limit:
    #         query += f" LIMIT {limit}"
        
    #     with self.engine.connect() as conn:
    #         df = pd.read_sql(query, conn)
        
    #     print(f"✅ {len(df)} registros carregados do banco")
    #     return df
    
    def load_raw_data(self, start_date: str = None, end_date: str = None, limit: int = None):
        import pandas as pd
                
        # Creio que não será necessário criar um view, por não usar o tempo inteiro
        query = """
        SELECT 
            i.id as item_id,
            i.name as item_name,
            i.item_code,
            i.current_stock,
            i.minimum_stock,
            i.maximum_stock,
            i.measure,
            i.expire_date,
            o.id as order_id,
            o.order_number,
            o.created_at as order_date,
            o.withdraw_day,
            o.status as order_status,
            o.expire_at,
            oi.quantity,
            po.id as purchase_order_id,
            po.status as po_status,
            po.total_value as po_total_value,
            po.issue_date as po_issue_date,
            po.issuing_body,
            po.commitment_note_number,
            it.id as item_type_id,
            it.name as item_type_name 
        FROM order_item oi
        INNER JOIN items i ON oi.item_id = i.id
        INNER JOIN orders o ON oi.order_id = o.id
        LEFT JOIN purchase_orders po ON po.order_id = o.id
        LEFT JOIN item_item_type iit ON iit.item_id = i.id
        LEFT JOIN items_type it ON iit.item_type_id = it.id
        
        WHERE 1=1
        """
        
        # Adicionar filtros de data se fornecidos
        params = {}
        if start_date:
            query += " AND o.created_at >= :start_date"
            params['start_date'] = start_date
        if end_date:
            query += " AND o.created_at <= :end_date"
            params['end_date'] = end_date
        
        query += " ORDER BY o.created_at ASC"
        
        if limit:
            query += f" LIMIT {limit}"
        
        with self.engine.connect() as conn:
            if params:
                df = pd.read_sql(text(query), conn, params=params)
            else:
                df = pd.read_sql(query, conn)
        # Normalizar colunas de datas (algumas podem não existir dependendo do JOINs)
        date_cols = ['order_date', 'withdraw_day', 'po_issue_date', 'expire_date', 'expire_at']
        for c in date_cols:
            if c in df.columns:
                df[c] = pd.to_datetime(df[c], errors='coerce')

        # Features temporais básicas, se existir order_date
        if 'order_date' in df.columns and not df['order_date'].isna().all():
            df['year'] = df['order_date'].dt.year
            df['month'] = df['order_date'].dt.month
            df['day'] = df['order_date'].dt.day
            df['day_of_week'] = df['order_date'].dt.dayofweek
            df['week_of_year'] = df['order_date'].dt.isocalendar().week
            df['quarter'] = df['order_date'].dt.quarter
            df['is_weekend'] = df['day_of_week'].isin([5, 6]).astype(int)

        # Tempo até a retirada/expiração quando disponíveis
        if 'withdraw_day' in df.columns and 'order_date' in df.columns:
            df['days_to_delivery'] = (df['withdraw_day'] - df['order_date']).dt.days
        if 'expire_date' in df.columns and 'order_date' in df.columns:
            df['days_to_expire'] = (df['expire_date'] - df['order_date']).dt.days

        print(f"✅ {len(df)} registros carregados do banco")
        if 'order_date' in df.columns and not df['order_date'].isna().all():
            print(f"📊 Período: {df['order_date'].min()} até {df['order_date'].max()}")
        print(f"📦 Total de itens únicos: {df['item_id'].nunique()}")
        if 'order_id' in df.columns:
            print(f"📋 Total de pedidos: {df['order_id'].nunique()}")

        return df

    def load_monthly_features(self, start_date: str = None, end_date: str = None):
        """Retorna agregados mensais por item no formato esperado pelo notebook.
        Colunas principais: item_id, year_month (YYYYMM), total_quantity, current_stock, minimum_stock, maximum_stock
        """
        import pandas as pd

        # Carrega dados brutos (puxando um intervalo maior por padrão)
        df = self.load_raw_data(start_date=start_date, end_date=end_date)

        if df is None or df.empty:
            return pd.DataFrame()

        # Garantir colunas de data
        if 'order_date' in df.columns and not df['order_date'].isna().all():
            df['year_month'] = df['order_date'].dt.year * 100 + df['order_date'].dt.month
        else:
            # Se não houver order_date, tentar usar created_at ou issue_date
            for cand in ['created_at', 'po_issue_date', 'issue_date']:
                if cand in df.columns and not df[cand].isna().all():
                    df['year_month'] = pd.to_datetime(df[cand], errors='coerce').dt.year * 100 + pd.to_datetime(df[cand], errors='coerce').dt.month
                    break
        
        if 'year_month' not in df.columns:
            print("⚠️ Não foi possível criar a coluna 'year_month'. Verifique as colunas de data.")
            return pd.DataFrame()

        # Agregações mensais por item
        agg_dict = {
            'total_quantity': pd.NamedAgg(column='quantity', aggfunc='sum'),
            'avg_quantity': pd.NamedAgg(column='quantity', aggfunc='mean'),
            'current_stock': pd.NamedAgg(column='current_stock', aggfunc='last'),
            'minimum_stock': pd.NamedAgg(column='minimum_stock', aggfunc='first'),
            'maximum_stock': pd.NamedAgg(column='maximum_stock', aggfunc='first'),
        }
        if 'order_id' in df.columns:
            agg_dict['num_orders'] = pd.NamedAgg(column='order_id', aggfunc='nunique')
        if 'is_weekend' in df.columns:
            agg_dict['weekend_orders'] = pd.NamedAgg(column='is_weekend', aggfunc='sum')

        grouped = df.groupby(['item_id', 'year_month']).agg(**agg_dict).reset_index()

        # preencher NaNs e ordenar
        fill_cols = ['total_quantity', 'avg_quantity', 'num_orders', 'weekend_orders']
        for col in fill_cols:
            if col in grouped.columns:
                grouped[col] = grouped[col].fillna(0)
        
        if 'num_orders' in grouped.columns:
            grouped['num_orders'] = grouped['num_orders'].astype(int)

        # Forward fill para o estoque, depois preenche o resto com 0
        if 'current_stock' in grouped.columns:
            grouped['current_stock'] = grouped.groupby('item_id')['current_stock'].transform(lambda x: x.ffill().bfill())
            grouped['current_stock'] = grouped['current_stock'].fillna(0)

        grouped = grouped.sort_values(['item_id', 'year_month']).reset_index(drop=True)
        return grouped
