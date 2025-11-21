import joblib
import pandas as pd
import logging
import sqlalchemy

from pathlib import Path
from typing import Dict

from src.training.model_registry import load_model
from src.training.data_extraction import load_monthly_consumption, get_item_series

logger = logging.getLogger(__name__)


class PredictionService:    
    def predict_all_items(self, connection_string: str = None):
        """
        Percorre todos os modelos salvos e retorna previsões para cada item,
        buscando os estoques reais da tabela items e removendo campos desnecessários do retorno.
        """
        # Carrega dados do banco
        df = load_monthly_consumption(connection_string)
        item_ids = sorted(df['item_id'].unique()) if not df.empty else []
        predictions = []

        # Conectar ao banco para buscar estoques reais
        engine = sqlalchemy.create_engine(connection_string)
        with engine.connect() as conn:
            stock_query = "SELECT id, current_stock, minimum_stock, maximum_stock FROM items WHERE id = ANY(:ids)"
            result = conn.execute(sqlalchemy.text(stock_query), {"ids": item_ids})
            stock_map = {row.id: row for row in result}

        for item_id in item_ids:
            payload = load_model(item_id)
            if payload is None:
                continue
            series = get_item_series(df, item_id)
            if series.empty:
                continue
            last_month = str(series.index.max())
            last_value = float(series.iloc[-1])
            stock = stock_map.get(item_id)
            predictions.append({
                'item_id': int(item_id),
                'predicted_quantity': last_value,
                'current_stock': float(stock.current_stock) if stock else None,
                'minimum_stock': float(stock.minimum_stock) if stock else None,
                'maximum_stock': float(stock.maximum_stock) if stock else None,
                'prediction_month': pd.Period(last_month).month,
                'prediction_year': pd.Period(last_month).year,
                'timestamp': pd.Timestamp.now().isoformat()
            })
        return predictions
    def __init__(self, model_dir: str = None):
        if model_dir is None:
            model_dir = Path(__file__).parent.parent / 'models'
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
            model_files = [f for f in self.model_dir.glob('item_*.pkl') if f.is_file() and not f.name.startswith('__')]
            if not model_files:
                raise FileNotFoundError(f"Nenhum modelo item_[id].pkl encontrado em {self.model_dir}")
            # Tenta carregar o primeiro arquivo pickle válido
            for model_path in model_files:
                try:
                    with open(model_path, 'rb') as f:
                        self.model = joblib.load(model_path)
                    logger.info(f"✅ Modelo carregado: {model_path.name}")
                    break
                except Exception as e:
                    logger.warning(f"Arquivo {model_path.name} ignorado: {e}")
            else:
                raise FileNotFoundError(f"Nenhum arquivo pickle válido encontrado em {self.model_dir}")
 
            
        except Exception as e:
            logger.error(f"❌ Erro ao carregar artefatos: {e}")
            raise
    
    
    def get_model_info(self) -> Dict:
        """Retorna informações sobre o modelo carregado"""
        return {
            'model_loaded': self.model is not None,
            'model_type': type(self.model).__name__ if self.model else None,
            'num_features': len(self.feature_columns) if self.feature_columns else 0,
            'metadata': self.metadata if self.metadata else {}
        }
