# AI Trainer (Python)

Este diretório contém o job Python responsável por treinar modelos de previsão por item e gravar as previsões na tabela `model_predictions` no Postgres.

## Tecnologias

- pandas, numpy, SQLAlchemy, psycopg2-binary
- statsmodels (SARIMAX) com fallback para média móvel
- APScheduler para execução diária

## Variáveis de ambiente

Veja `.env.example` para referência:

- Fonte de dados:
  - PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD (quando TRAIN_INPUT_SOURCE=postgres)
  - TRAIN_INPUT_SOURCE (postgres | csv | parquet)
  - TRAIN_INPUT_PATH, TRAIN_INPUT_FORMAT, INPUT_COL_ITEM, INPUT_COL_DATE, INPUT_COL_QTY, INPUT_DATE_TZ (para arquivo)
- Janela/modelo:
  - TRAIN_WINDOW_DAYS (padrão 120)
  - FORECAST_HORIZON_DAYS (padrão 14)
  - MODEL_VERSION (ex.: sarimax_v1)
- Execução:
  - MODE (once | schedule | report | evaluate)
  - TRAIN_FORCE (false por padrão; se true, reprocessa o dia mesmo que já exista previsão)
  - TZ (ex.: America/Sao_Paulo; controla o fuso do agendamento)
- Destino das previsões:
  - TRAIN_TARGET (database | file)
  - OUTPUT_DIR (quando TRAIN_TARGET=file)

## Execução local (venv)

1. Crie um virtualenv e instale dependências:
   python -m venv .venv
   source .venv/bin/activate
   pip install -r requirements.txt
2. Configure as variáveis de ambiente (crie `.env` a partir de `.env.example`).
3. Execute uma vez:
   python train_forecast.py
4. Ou rode em modo agendado:
   MODE=schedule python train_forecast.py

## Docker / docker-compose

- O serviço `ai-trainer` já está definido no `docker-compose.yml` na raiz.
- Build e subir:
  docker compose --profile ai up -d --build ai-trainer

O container roda continuamente com APScheduler e executa o treinamento diariamente no horário configurado (fuso em TZ). Se previsões do dia já existirem e TRAIN_FORCE=false, ele pula a execução (uma vez por dia).

## Avaliação de efetividade (métricas)

- As métricas são calculadas quando o horizonte já “venceu” (quando já é possível comparar previsão vs. realizado).
- O script mantém duas tabelas de métricas:
  - `prediction_metrics`: por item (detalhado) com `y_hat`, `y_true`, `abs_error`, `ape`.
  - `model_prediction_metrics`: agregado por rodada (`ref_date`, `horizon_days`) com `MAE`, `RMSE`, `MAPE`, `sMAPE`, `WAPE`, `pairs`.
- Como usar:
  - Executar avaliação manual (detalhada + agregada):
    MODE=evaluate python train_forecast.py
  - No modo agendado (`MODE=schedule`), a avaliação também roda na inicialização do container.
  - Consultas úteis:
    - Detalhado: SELECT ref_date, horizon_days, COUNT(\*), AVG(abs_error) AS mae, AVG(ape) AS mape FROM prediction_metrics GROUP BY 1,2 ORDER BY 1 DESC;
    - Agregado: SELECT \* FROM model_prediction_metrics ORDER BY ref_date DESC, horizon_days;

## Contrato de dados

- Escreve em `model_predictions(item_id, ref_date, horizon_days, y_hat, model_version, created_at)`
- Se a tabela não existir, o script cria automaticamente.
- Antes de inserir, remove previsões da mesma `ref_date` e `horizon_days`.
- Escreve em `prediction_metrics(item_id, ref_date, horizon_days, y_hat, y_true, abs_error, ape, created_at)` quando houver previsões maduras.
- Quando `TRAIN_TARGET=file`, grava CSV em `OUTPUT_DIR/predictions_<refDate>_h<horizon>.csv` com colunas: `item_id, ref_date, horizon_days, y_hat, model_version`.
- Quando `TRAIN_INPUT_SOURCE=file`, espera colunas mapeáveis via `INPUT_COL_*` e agrega por dia/item.

## Observações

- Recomendamos validar a qualidade com um período de testes e ajustar ordem/parametrização do SARIMAX conforme o perfil da sua série.
- Se preferir Prophet ou outro modelo, basta substituí-lo dentro de `fit_predict_series` e atualizar o `MODEL_VERSION`.
- As métricas agregadas também são mostradas no `MODE=report` quando disponíveis para a rodada mais recente do horizonte configurado.
- Observação: `MODE=evaluate` e métricas agregadas dependem do banco (histórico real). Para dataset externo em arquivo, use backtesting offline para avaliação.
