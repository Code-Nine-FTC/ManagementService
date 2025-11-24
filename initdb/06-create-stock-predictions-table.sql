CREATE TABLE IF NOT EXISTS prediction_stock_ai.stock_predictions (
    id SERIAL PRIMARY KEY,
    item_id INTEGER NOT NULL,
    prediction_month INTEGER NOT NULL,
    prediction_year INTEGER NOT NULL,
    predicted_consumption FLOAT NOT NULL,
    predicted_stock FLOAT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(item_id, prediction_month, prediction_year)
);

COMMENT ON TABLE prediction_stock_ai.stock_predictions IS 'Previsões de estoque futuro para cada item, mês e ano';
COMMENT ON COLUMN prediction_stock_ai.stock_predictions.predicted_consumption IS 'Consumo previsto pelo modelo para o mês';
COMMENT ON COLUMN prediction_stock_ai.stock_predictions.predicted_stock IS 'Estoque previsto após consumo do mês';
