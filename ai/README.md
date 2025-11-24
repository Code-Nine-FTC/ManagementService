# Sistema de Previsão de Estoque

API Python (FastAPI) para previsão de consumo e estoque de itens, integrada ao banco PostgreSQL.

## Como usar

1. Instale as dependências:
   ```bash
   cd ai/
   pip install -r requirements.txt
   ```
2. Configure o banco no arquivo `.env`.
3. Treine os modelos:
   ```bash
   python -m src.training.batch_train
   ```
4. Inicie a API:
   ```bash
   python scripts/start_api.py
   # ou
   uvicorn src.api:app --reload
   ```

## Endpoints principais

- `GET /stock-prediction/item/{item_id}` — Previsão para um item
- `GET /stock-prediction/items?item_ids=1,2,3` — Previsão para vários itens
- `GET /stock-prediction/all` — Previsão para todos os itens

Documentação interativa: [http://localhost:8000/docs](http://localhost:8000/docs)

## Estrutura resumida

- `ai/src/api.py` — API principal
- `ai/src/controllers/item_controller.py` — Endpoints de previsão
- `ai/src/training/batch_train.py` — Treinamento em lote
- `ai/requirements.txt` — Dependências

Consulte o arquivo `docs/GUIA_USO_API.md` para exemplos de requisição e detalhes.
