from fastapi import APIRouter, Query, HTTPException
from typing import Optional
import sqlalchemy

router = APIRouter()

def get_db_engine(db_url: Optional[str] = None):
	if db_url:
		return sqlalchemy.create_engine(db_url)
	import os
	db_url_env = os.getenv("DB_URL")
	if not db_url_env:
		raise RuntimeError("DB_URL não configurado")
	return sqlalchemy.create_engine(db_url_env)


@router.get("/stock-prediction/item/{item_id}", tags=["StockPrediction"])
async def get_stock_prediction_by_item(item_id: int, db_url: Optional[str] = Query(None, description="String de conexão do banco (opcional)")):
	engine = get_db_engine(db_url)
	with engine.connect() as conn:
		result = conn.execute(sqlalchemy.text("""
			SELECT item_id, prediction_month, prediction_year, predicted_consumption, predicted_stock
			FROM prediction_stock_ai.stock_predictions
			WHERE item_id = :item_id
			ORDER BY prediction_year, prediction_month
		"""), {"item_id": item_id})
		rows = list(result.mappings())
	if not rows:
		raise HTTPException(status_code=404, detail="Nenhuma previsão encontrada para o item informado.")
	return rows

# Endpoint para buscar previsões de estoque de vários itens
@router.get("/stock-prediction/items", tags=["StockPrediction"])
async def get_stock_prediction_by_items(item_ids: str = Query(..., description="IDs dos itens separados por vírgula (ex: 1,2,3)"), db_url: Optional[str] = Query(None, description="String de conexão do banco (opcional)")):
	try:
		ids = [int(id.strip()) for id in item_ids.split(',')]
	except Exception:
		return []
	engine = get_db_engine(db_url)
	with engine.connect() as conn:
		result = conn.execute(sqlalchemy.text("""
			SELECT item_id, prediction_month, prediction_year, predicted_consumption, predicted_stock
			FROM prediction_stock_ai.stock_predictions
			WHERE item_id = ANY(:ids)
			ORDER BY item_id, prediction_year, prediction_month
		"""), {"ids": ids})
		rows = list(result.mappings())
	return rows

# Endpoint para buscar previsões de estoque de todos os itens (apenas para testes)
@router.get("/stock-prediction/all", tags=["StockPrediction"])
async def get_stock_prediction_all(db_url: Optional[str] = Query(None, description="String de conexão do banco (opcional)")):
	engine = get_db_engine(db_url)
	with engine.connect() as conn:
		result = conn.execute(sqlalchemy.text("""
			SELECT item_id, prediction_month, prediction_year, predicted_consumption, predicted_stock
			FROM prediction_stock_ai.stock_predictions
			ORDER BY item_id, prediction_year, prediction_month
		"""))
		rows = list(result.mappings())
	return rows
