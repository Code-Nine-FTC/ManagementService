import os
import joblib
from typing import Any

MODELS_DIR = os.path.join(os.path.dirname(__file__), '..', 'models')
os.makedirs(MODELS_DIR, exist_ok=True)


def model_path(item_id: int) -> str:
    return os.path.join(MODELS_DIR, f"item_{item_id}.pkl")


def save_model(item_id: int, model: Any, metadata: dict) -> None:
    payload = {
        'model': model,
        'metadata': metadata
    }
    joblib.dump(payload, model_path(item_id))


def load_model(item_id: int):
    path = model_path(item_id)
    if not os.path.exists(path):
        return None
    return joblib.load(path)
