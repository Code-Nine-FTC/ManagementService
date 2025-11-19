import pickle
import pandas as pd
import numpy as np
from pathlib import Path
from typing import Dict, List, Optional
from datetime import datetime, timedelta
import logging

from src.utils.database import DatabaseConnector

logger = logging.getLogger(__name__)


class PredictionService:    
    def __init__(self, model_dir: str = None):
        if model_dir is None:
            model_dir = Path(__file__).parent.parent.parent / 'models'
        else:
            model_dir = Path(model_dir)
        
        self.model_dir = model_dir
        self.model = None
        self.scaler = None
        self.feature_columns = None
        self.metadata = None
        self.db = None
        
        self._load_artifacts()
    
    def _load_artifacts(self):
        try:
            model_files = list(self.model_dir.glob('*_v2.pkl'))
            if not model_files:
                raise FileNotFoundError(f"Nenhum modelo _v2.pkl encontrado em {self.model_dir}")
            
            model_path = [f for f in model_files if 'gradient_boosting' in f.name or 'random_forest' in f.name or 'linear' in f.name][0]
            
            with open(model_path, 'rb') as f:
                self.model = pickle.load(f)
            logger.info(f"✅ Modelo carregado: {model_path.name}")
            
            # Carregar scaler
            scaler_path = self.model_dir / 'scaler_v2.pkl'
            with open(scaler_path, 'rb') as f:
                self.scaler = pickle.load(f)
            logger.info(f"✅ Scaler carregado")
            
            # Carregar feature columns
            features_path = self.model_dir / 'feature_columns_v2.pkl'
            with open(features_path, 'rb') as f:
                self.feature_columns = pickle.load(f)
            logger.info(f"✅ Features carregadas: {len(self.feature_columns)} colunas")
            
            # Carregar metadata
            metadata_path = self.model_dir / 'model_metadata_v2.pkl'
            if metadata_path.exists():
                with open(metadata_path, 'rb') as f:
                    self.metadata = pickle.load(f)
                logger.info(f"✅ Metadata carregada - R²: {self.metadata.get('r2_score', 'N/A'):.4f}")
            
        except Exception as e:
            logger.error(f"❌ Erro ao carregar artefatos: {e}")
            raise
    
    def connect_database(self, connection_string: str = None):
        self.db = DatabaseConnector(connection_string)
        logger.info("✅ Conectado ao banco de dados")
    
    # prepara as previsões pro proximo mês
    def _prepare_features_for_prediction(self, historical_data: pd.DataFrame, item_id: int) -> pd.DataFrame:
        item_data = historical_data[historical_data['item_id'] == item_id].copy()
        
        if len(item_data) == 0:
            raise ValueError(f"Nenhum dado histórico encontrado para item_id={item_id}")
        
        item_data = item_data.sort_values('year_month')
        
        latest = item_data.iloc[-1].copy()
        
        next_month_features = {}
        
        next_month_features['item_id'] = item_id
        
        current_year = int(latest['year'])
        current_month = int(latest['month'])
        
        if current_month == 12:
            next_month_features['year'] = current_year + 1
            next_month_features['month'] = 1
        else:
            next_month_features['year'] = current_year
            next_month_features['month'] = current_month + 1
        
        # Features de lag
        next_month_features['prev_total_quantity'] = latest['total_quantity']
        next_month_features['prev_avg_quantity'] = latest['avg_quantity']
        next_month_features['prev_num_orders'] = latest['num_orders']
        next_month_features['prev_stock'] = latest['current_stock']
        
        # Médias móveis
        if len(item_data) >= 3:
            next_month_features['ma3_quantity'] = item_data['total_quantity'].tail(3).mean()
            next_month_features['ma3_orders'] = item_data['num_orders'].tail(3).mean()
        else:
            next_month_features['ma3_quantity'] = latest['total_quantity']
            next_month_features['ma3_orders'] = latest['num_orders']
        
        # Taxa de crescimento
        if len(item_data) >= 2:
            prev_quantity = item_data['total_quantity'].iloc[-2]
            if prev_quantity > 0:
                next_month_features['quantity_growth_rate'] = (latest['total_quantity'] - prev_quantity) / prev_quantity
            else:
                next_month_features['quantity_growth_rate'] = 0
        else:
            next_month_features['quantity_growth_rate'] = 0
        
        # Features de estoque
        next_month_features['minimum_stock'] = latest['minimum_stock']
        next_month_features['maximum_stock'] = latest['maximum_stock']
        
        # Features temporais
        next_month_features['weekend_orders'] = latest.get('weekend_orders', 0)
        next_month_features['avg_days_to_delivery'] = latest.get('avg_days_to_delivery', 0)
        
        # Stock coverage
        if latest['total_quantity'] > 0:
            next_month_features['stock_coverage'] = latest['current_stock'] / latest['total_quantity']
        else:
            next_month_features['stock_coverage'] = 0
        
        # Unique order count
        next_month_features['unique_order_count'] = latest.get('unique_order_count', 0)
        
        # Converter para DataFrame
        features_df = pd.DataFrame([next_month_features])
        
        # Garantir que todas as features necessárias existem
        for col in self.feature_columns:
            if col not in features_df.columns:
                features_df[col] = 0
        
        # Ordenar colunas na mesma ordem do treinamento
        features_df = features_df[self.feature_columns]
        
        return features_df
    
    def predict_next_month(self, item_id: int, historical_data: pd.DataFrame) -> Dict:
        try:
            # Preparar features
            features = self._prepare_features_for_prediction(historical_data, item_id)
            
            # Normalizar
            features_scaled = self.scaler.transform(features)
            
            # Fazer previsão
            prediction = self.model.predict(features_scaled)[0]
            
            # Garantir que não seja negativo
            prediction = max(0, prediction)
            
            # Pegar informações adicionais do item
            item_info = historical_data[historical_data['item_id'] == item_id].iloc[-1]
            
            result = {
                'item_id': int(item_id),
                'predicted_quantity': round(float(prediction), 2),
                'current_stock': float(item_info['current_stock']),
                'minimum_stock': float(item_info['minimum_stock']),
                'maximum_stock': float(item_info['maximum_stock']),
                'prediction_month': int(features['month'].values[0]),
                'prediction_year': int(features['year'].values[0]),
                'needs_restock': prediction > item_info['current_stock'],
                'restock_quantity': max(0, round(float(prediction - item_info['current_stock']), 2)),
                'confidence_score': float(self.metadata.get('r2_score', 0)) if self.metadata else 0.0,
                'model_used': self.metadata.get('model_name', 'unknown') if self.metadata else 'unknown',
                'timestamp': datetime.now().isoformat()
            }
            
            return result
            
        except Exception as e:
            logger.error(f"❌ Erro ao prever item {item_id}: {e}")
            raise
    
    def predict_all_items(self, connection_string: str = None) -> List[Dict]:
        if self.db is None:
            self.connect_database(connection_string)
        
        logger.info("📊 Carregando dados do banco...")
        raw_data = self.db.load_raw_data()
        
        logger.info("🔧 Criando features agregadas...")
        historical_data = self._create_monthly_features(raw_data)
        
        # Gerar previsões para cada item
        item_ids = historical_data['item_id'].unique()
        predictions = []
        
        logger.info(f"🚀 Gerando previsões para {len(item_ids)} itens...")
        
        for item_id in item_ids:
            try:
                pred = self.predict_next_month(item_id, historical_data)
                predictions.append(pred)
            except Exception as e:
                logger.warning(f"⚠️ Erro ao prever item {item_id}: {e}")
                continue
        
        logger.info(f"✅ {len(predictions)} previsões geradas com sucesso!")
        return predictions
    
    def _create_monthly_features(self, data: pd.DataFrame) -> pd.DataFrame:
        """
        Cria features mensais agregadas a partir dos dados brutos
        (Mesmo processo do notebook de treinamento)
        """
        df = data.copy()
        df['year_month'] = df['year'] * 100 + df['month']
        
        # Agregações por item e mês
        monthly_features = df.groupby(['item_id', 'year_month']).agg({
            'quantity': ['sum', 'mean', 'count', 'std'],
            'order_id': 'nunique',
            'current_stock': 'last',
            'minimum_stock': 'first',
            'maximum_stock': 'first',
            'is_weekend': 'sum',
            'days_to_delivery': ['mean', 'std'],
            'supplier_id': lambda x: x.mode()[0] if len(x.mode()) > 0 else None,
            'section_id': lambda x: x.mode()[0] if len(x.mode()) > 0 else None,
        }).reset_index()
        
        # Renomear colunas
        monthly_features.columns = [
            'item_id', 'year_month',
            'total_quantity', 'avg_quantity', 'num_orders', 'std_quantity',
            'unique_order_count',
            'current_stock', 'minimum_stock', 'maximum_stock',
            'weekend_orders',
            'avg_days_to_delivery', 'std_days_to_delivery',
            'main_supplier_id',
            'main_section_id'
        ]
        
        # Preencher NaN
        monthly_features['std_quantity'] = monthly_features['std_quantity'].fillna(0)
        monthly_features['std_days_to_delivery'] = monthly_features['std_days_to_delivery'].fillna(0)
        
        # Ordenar
        monthly_features = monthly_features.sort_values(['item_id', 'year_month']).reset_index(drop=True)
        
        # Adicionar year e month separados
        monthly_features['year'] = monthly_features['year_month'] // 100
        monthly_features['month'] = monthly_features['year_month'] % 100
        
        return monthly_features
    
    def get_model_info(self) -> Dict:
        """Retorna informações sobre o modelo carregado"""
        return {
            'model_loaded': self.model is not None,
            'model_type': type(self.model).__name__ if self.model else None,
            'num_features': len(self.feature_columns) if self.feature_columns else 0,
            'metadata': self.metadata if self.metadata else {}
        }
