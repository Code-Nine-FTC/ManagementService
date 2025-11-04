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
        port = os.getenv('PGPORT', '5432')
        database = os.getenv('PGDATABASE', 'teste')
        user = os.getenv('PGUSER', 'postgres')
        password = os.getenv('PGPASSWORD', 'fatec')
        
        return f'postgresql://{user}:{password}@{host}:{port}/{database}'
    
    def save_predictions(self, predictions: List[Dict[str, Any]]):
        with self.engine.connect() as conn:
            current_month = datetime.now().month
            
            for pred in predictions:
                item_id = pred.get('item_id')
                
                # Salvar previsão de estoque
                if 'stock_predicted' in pred:
                    conn.execute(text("""
                        INSERT INTO item_prediction (item_id, month, forecast_type, value, created_at)
                        VALUES (:item_id, :month, 'stock_quantity', :value, :created_at)
                    """), {
                        'item_id': item_id,
                        'month': current_month,
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
                        'month': current_month,
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
                        'month': current_month,
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
    
    def load_raw_data(self, start_date: str = None, end_date: str = None, limit: int = None) -> 'pd.DataFrame':
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
            s.id as section_id,
            s.title as section_name,
            po.id as purchase_order_id,
            po.status as po_status,
            po.total_value as po_total_value,
            po.issue_date as po_issue_date,
            po.issuing_body,
            po.commitment_note_number,
            sc.id as supplier_id,
            sc.name as supplier_name,
            sc.email as supplier_email,
            it.id as item_type_id,
            it.name as item_type_name 
        FROM order_item oi
        INNER JOIN items i ON oi.item_id = i.id
        INNER JOIN orders o ON oi.order_id = o.id
        LEFT JOIN sections s ON o.section_id = s.id
        LEFT JOIN purchase_orders po ON po.order_id = o.id
        LEFT JOIN suppliers_companies sc ON po.supplier_company_id = sc.id
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
        
        # Criar features temporais adicionais
        df['order_date'] = pd.to_datetime(df['order_date'])
        df['withdraw_day'] = pd.to_datetime(df['withdraw_day'])
        df['po_issue_date'] = pd.to_datetime(df['po_issue_date'])
        df['expire_date'] = pd.to_datetime(df['expire_date'])
        df['expire_at'] = pd.to_datetime(df['expire_at'])
        
        # Features temporais
        df['year'] = df['order_date'].dt.year
        df['month'] = df['order_date'].dt.month
        df['day'] = df['order_date'].dt.day
        df['day_of_week'] = df['order_date'].dt.dayofweek  # 0=Segunda, 6=Domingo
        df['week_of_year'] = df['order_date'].dt.isocalendar().week
        df['quarter'] = df['order_date'].dt.quarter
        df['is_weekend'] = df['day_of_week'].isin([5, 6]).astype(int)
        
        # Tempo até a retirada (em dias)
        df['days_to_delivery'] = (df['withdraw_day'] - df['order_date']).dt.days
        
        # Tempo até expiração do item (em dias)
        df['days_to_expire'] = (df['expire_date'] - df['order_date']).dt.days
        
        print(f"✅ {len(df)} registros carregados do banco")
        print(f"📊 Período: {df['order_date'].min()} até {df['order_date'].max()}")
        print(f"📦 Total de itens únicos: {df['item_id'].nunique()}")
        print(f"🏢 Total de fornecedores: {df['supplier_id'].nunique()}")
        print(f"📋 Total de pedidos: {df['order_id'].nunique()}")
        
        return df
