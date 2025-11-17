import pickle
import logging
from pathlib import Path
from typing import Dict, List, Optional
from datetime import datetime, timedelta
import pandas as pd
from prophet import Prophet

from src.utils.database import DatabaseConnector

logger = logging.getLogger(__name__)


class PredictionService:
    """
    Serviço de previsão baseado em consumo diário.
    
    Os modelos preveem o consumo diário de cada item.
    O serviço calcula o estoque futuro subtraindo o consumo previsto do estoque atual.
    """
    def __init__(self, model_dir: str = None):
        if model_dir is None:
            # O diretório de modelos agora está em 'ai/models'
            model_dir = Path(__file__).parent.parent / 'models'
        else:
            model_dir = Path(model_dir)
        
        self.model_dir = model_dir
        self.models: Dict[int, Prophet] = {}  # Dicionário para guardar {item_id: model}
        self.metadata = None
        self.db = None
        
        self._load_artifacts()
    
    def _load_artifacts(self):
        """
        Carrega todos os modelos Prophet de consumo diário do diretório de modelos.
        """
        try:
            self.models = {}
            model_files = list(self.model_dir.glob('prophet_daily_consumption_*.pkl'))
            
            if not model_files:
                raise FileNotFoundError(f"Nenhum modelo 'prophet_daily_consumption_*.pkl' encontrado em {self.model_dir}")

            for model_path in model_files:
                try:
                    # Extrai o item_id do nome do arquivo
                    item_id = int(model_path.stem.split('_')[-1])
                    with open(model_path, 'rb') as f:
                        self.models[item_id] = pickle.load(f)
                except (IndexError, ValueError):
                    logger.warning(f"⚠️ Não foi possível extrair item_id do arquivo: {model_path.name}")
                    continue

            logger.info(f"✅ {len(self.models)} modelos Prophet carregados.")
            
            # Carregar metadados
            metadata_path = self.model_dir / 'prophet_daily_metadata.pkl'
            if metadata_path.exists():
                with open(metadata_path, 'rb') as f:
                    self.metadata = pickle.load(f)
                logger.info(f"✅ Metadados carregados. Tipo: {self.metadata.get('model_type', 'N/A')}")
            
        except Exception as e:
            logger.error(f"❌ Erro ao carregar artefatos: {e}")
            raise
    
    def connect_database(self, connection_string: str = None):
        self.db = DatabaseConnector(connection_string)
        logger.info("✅ Conectado ao banco de dados")
    
    def predict_daily_consumption(self, item_id: int, forecast_days: int = 30) -> Dict:
        """
        Prevê o consumo diário de um item para os próximos N dias.
        
        Args:
            item_id: ID do item
            forecast_days: Número de dias para prever (padrão: 30)
        
        Returns:
            Dict com previsões diárias e métricas calculadas
        """
        try:
            if item_id not in self.models:
                raise ValueError(f"Modelo não encontrado para item_id={item_id}")
            
            model = self.models[item_id]
            
            # Carregar dados históricos para pegar informações do item
            if self.db is None:
                self.connect_database()
            
            raw_data = self.db.load_raw_data()
            item_data = raw_data[raw_data['item_id'] == item_id]
            
            if item_data.empty:
                raise ValueError(f"Nenhum dado histórico encontrado para item_id={item_id}")
            
            # Informações do item
            current_stock = float(item_data['current_stock'].iloc[-1])
            minimum_stock = float(item_data['minimum_stock'].iloc[-1])
            maximum_stock = float(item_data['maximum_stock'].iloc[-1])
            item_name = item_data['item_name'].iloc[-1] if 'item_name' in item_data.columns else f"Item {item_id}"
            
            # Criar dataframe futuro
            future = model.make_future_dataframe(periods=forecast_days, freq='D')
            
            # Preencher regressores
            future['day_of_week'] = future['ds'].dt.dayofweek
            future['is_weekend'] = (future['day_of_week'] >= 5).astype(int)
            
            # Para ma_7, usar último valor conhecido do histórico
            if len(model.history) > 0:
                last_ma7 = model.history['y'].tail(7).mean()
                future['ma_7'] = last_ma7
            else:
                future['ma_7'] = 0
            
            # Fazer previsão
            forecast = model.predict(future)
            forecast['yhat'] = forecast['yhat'].clip(lower=0)  # Consumo não pode ser negativo
            
            # Pegar apenas previsões futuras
            future_forecast = forecast[forecast['ds'] > model.history['ds'].max()].copy()
            
            # Calcular consumo total previsto
            total_consumption = float(future_forecast['yhat'].sum())
            avg_daily_consumption = float(future_forecast['yhat'].mean())
            
            # Calcular estoque futuro (estoque atual - consumo acumulado)
            future_forecast['cumulative_consumption'] = future_forecast['yhat'].cumsum()
            future_forecast['predicted_stock'] = current_stock - future_forecast['cumulative_consumption']
            
            # Detectar quando o estoque atinge o mínimo
            days_to_minimum = None
            restock_needed = False
            restock_quantity = 0
            
            below_minimum = future_forecast[future_forecast['predicted_stock'] < minimum_stock]
            if not below_minimum.empty:
                days_to_minimum = int((below_minimum.iloc[0]['ds'] - datetime.now()).days)
                restock_needed = True
                # Calcular quanto precisa repor (diferença entre máximo e estoque previsto no final)
                final_stock = float(future_forecast['predicted_stock'].iloc[-1])
                restock_quantity = max(0, maximum_stock - final_stock)
            
            result = {
                'item_id': int(item_id),
                'item_name': item_name,
                'current_stock': current_stock,
                'minimum_stock': minimum_stock,
                'maximum_stock': maximum_stock,
                'forecast_days': forecast_days,
                'predicted_daily_consumption': round(avg_daily_consumption, 2),
                'predicted_total_consumption': round(total_consumption, 2),
                'predicted_final_stock': round(float(future_forecast['predicted_stock'].iloc[-1]), 2),
                'needs_restock': restock_needed,
                'days_until_minimum_stock': days_to_minimum,
                'recommended_restock_quantity': round(restock_quantity, 2),
                'daily_predictions': [
                    {
                        'date': row['ds'].strftime('%Y-%m-%d'),
                        'predicted_consumption': round(float(row['yhat']), 2),
                        'predicted_stock': round(float(row['predicted_stock']), 2),
                        'lower_bound': round(float(row['yhat_lower']), 2),
                        'upper_bound': round(float(row['yhat_upper']), 2)
                    }
                    for _, row in future_forecast.iterrows()
                ],
                'model_used': 'prophet_daily_consumption',
                'timestamp': datetime.now().isoformat()
            }
            
            return result
            
        except Exception as e:
            logger.error(f"❌ Erro ao prever item {item_id}: {e}")
            raise
    
    def predict_all_items(self, connection_string: str = None, forecast_days: int = 30) -> List[Dict]:
        """
        Gera previsões de consumo diário para todos os itens com modelos treinados.
        
        Args:
            connection_string: String de conexão do banco (opcional)
            forecast_days: Número de dias para prever (padrão: 30)
        
        Returns:
            Lista de dicionários com previsões resumidas para cada item
        """
        if self.db is None:
            self.connect_database(connection_string)
        
        logger.info(f"🚀 Gerando previsões para {len(self.models)} itens...")
        
        predictions = []
        
        for item_id in self.models.keys():
            try:
                pred = self.predict_daily_consumption(item_id, forecast_days)
                
                # Versão resumida para a lista (sem daily_predictions detalhadas)
                summary = {
                    'item_id': pred['item_id'],
                    'item_name': pred['item_name'],
                    'current_stock': pred['current_stock'],
                    'minimum_stock': pred['minimum_stock'],
                    'maximum_stock': pred['maximum_stock'],
                    'predicted_daily_consumption': pred['predicted_daily_consumption'],
                    'predicted_total_consumption': pred['predicted_total_consumption'],
                    'predicted_final_stock': pred['predicted_final_stock'],
                    'needs_restock': pred['needs_restock'],
                    'days_until_minimum_stock': pred['days_until_minimum_stock'],
                    'recommended_restock_quantity': pred['recommended_restock_quantity'],
                    'forecast_days': forecast_days,
                    'model_used': pred['model_used'],
                    'timestamp': pred['timestamp']
                }
                
                predictions.append(summary)
                
            except Exception as e:
                logger.warning(f"⚠️ Erro ao prever item {item_id}: {e}")
                continue
        
        logger.info(f"✅ {len(predictions)} previsões geradas com sucesso!")
        return predictions
    
    def _create_monthly_features(self, data: pd.DataFrame) -> pd.DataFrame:
        """
        DEPRECATED: Este método não é mais usado com o novo modelo de consumo diário.
        Mantido por compatibilidade.
        """
        logger.warning("⚠️ _create_monthly_features está deprecated. Use dados diários.")
        return pd.DataFrame()
    
    def get_model_info(self) -> Dict:
        """Retorna informações sobre os modelos carregados"""
        return {
            'models_loaded': len(self.models) > 0,
            'num_models': len(self.models),
            'model_type': 'Prophet - Daily Consumption Forecast',
            'item_ids': list(self.models.keys()),
            'metadata': self.metadata if self.metadata else {}
        }
