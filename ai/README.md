# 🤖 Sistema de Previsão de Demanda com Machine Learning

Sistema de previsão de demanda para gestão inteligente de estoque usando Machine Learning.

## 📋 Visão Geral

Este sistema usa **Gradient Boosting** com dados brutos transacionais para prever:

- 📦 **Demanda futura** de itens
- 🔄 **Necessidade de reposição** de estoque
- 📊 **Quantidade ideal** para pedidos

**Performance atual**: R² = 0.94, MAE = 6.17

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│                    Java Backend                         │
│                  (Spring Boot)                          │
│                   Porta 8080                            │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP Request
                     ▼
┌─────────────────────────────────────────────────────────┐
│              Python API (FastAPI)                       │
│          Serviço de Previsões ML                        │
│                   Porta 8000                            │
└────────────────────┬────────────────────────────────────┘
                     │ SQL Query
                     ▼
┌─────────────────────────────────────────────────────────┐
│              PostgreSQL Database                        │
│      Tables: items, orders, order_item, etc.           │
│                   Porta 5433                            │
└─────────────────────────────────────────────────────────┘
```

---

## 📂 Estrutura do Projeto

```
ai/
├── docs/
│   ├── GUIA_USO_API.md              # 📚 Guia completo de uso
│
├── models/                          # 🤖 Modelos treinados
│   ├── gradient_boosting_v2.pkl
│   ├── scaler_v2.pkl
│   ├── feature_columns_v2.pkl
│   └── model_metadata_v2.pkl
│
├── src/training/
│   ├── notebooks/
│   │   └── consumo_items.ipynb      # Notebook demonstrativo (pipeline consumo)
│   ├── data_extraction.py           # Extração de consumo mensal (orders COMPLETED)
│   ├── forecast.py                  # Lógica de previsão SARIMAX / fallback média
│   ├── train_item_model.py          # Treina modelo individual por item
│   ├── batch_train.py               # Treino em lote de todos os itens
│   └── model_registry.py            # Registro e carregamento de modelos por item
├── scripts/
│   ├── start_api.py                 # 🚀 Iniciar API REST
│   └── test_predictions.py          # 🧪 Testar previsões localmente
│
├── src/
│   ├── api.py                       # 🌐 API REST FastAPI
│   ├── models/
│   ├── services/
│   │   └── prediction_service_v2.py # 🔮 Serviço de previsões
│   └── utils/
│       ├── database.py              # 💾 Conexão com banco
│       └── logger.py
│
├── requirements.txt                 # 📦 Dependências Python
├── Dockerfile                       # 🐳 Container Docker
└── README.md                        # 📖 Este arquivo
```

---

## 🚀 Quick Start

### 1️⃣ Instalar Dependências

```bash
cd ai/
pip install -r requirements.txt
```

### 2️⃣ Treinar Modelos de Consumo (Novo Fluxo)

Agora o foco é consumo mensal por item a partir de `orders` com status **COMPLETED**.

Treinar todos os itens:

```bash
python -m src.training.batch_train
```

Treinar um item específico (ex: item 42):

```bash
python -m src.training.train_item_model 42 --forecast_periods 3
```

Notebook demonstrativo:

Notebook demonstrativo:

```bash
jupyter notebook src/training/notebooks/consumo_items.ipynb
```

### 3️⃣ Iniciar API

```bash
python scripts/start_api.py
```

Se ocorrer erro de módulo não encontrado, exporte `PYTHONPATH`:

```bash
export PYTHONPATH="$(pwd)/src:$PYTHONPATH"
```

A API estará disponível em:

- 🌐 http://localhost:8000
- 📚 http://localhost:8000/docs (Swagger)

### 4️⃣ Testar

```bash
# Health check
curl http://localhost:8000/health

# Previsão para item 1
curl http://localhost:8000/predictions/item/1
```

---

## 📡 Endpoints Principais

| Endpoint                            | Método | Descrição                     |
| ----------------------------------- | ------ | ----------------------------- |
| `/health`                           | GET    | Status da API                 |
| `/model/info`                       | GET    | Informações do modelo         |
| `/predictions/item/{id}`            | GET    | Previsão para 1 item          |
| `/items/{id}/consumption`           | GET    | Histórico + forecast consumo  |
| `/predictions/items?item_ids=1,2,3` | GET    | Previsão para múltiplos itens |
| `/predictions/all`                  | GET    | Previsão para todos os itens  |
| `/docs`                             | GET    | Documentação Swagger          |

---

## 🧪 Testes

### Teste Local Python

```bash
python scripts/test_predictions.py
```

### Teste API com cURL

```bash
# Previsão para item 1
curl http://localhost:8000/predictions/item/1

# Previsão para múltiplos itens
curl "http://localhost:8000/predictions/items?item_ids=1,2,3"

# Todas as previsões
curl http://localhost:8000/predictions/all
```

### Teste Interativo

Acesse: http://localhost:8000/docs

---

### Base de Consumo

O consumo é calculado a partir de:

```sql
SELECT oi.item_id,
       DATE_TRUNC('month', o.created_at) AS month,
       SUM(oi.quantity) AS consumed_quantity
FROM order_items oi
JOIN orders o ON o.id = oi.order_id
WHERE o.status = 'COMPLETED'
GROUP BY oi.item_id, DATE_TRUNC('month', o.created_at)
ORDER BY oi.item_id, month;
```

Cada série mensal é usada para treinar um modelo SARIMAX simples. Se a série for muito curta (<4 pontos) usa-se média como fallback.

Modelos são persistidos em `ai/models/item_<id>.pkl` com metadata (histórico, último mês, métrica AIC/BIC quando disponível).

---

## 🤖 Modelos Disponíveis

Para consumo mensal por item:

1. **SARIMAX (1,1,1)** quando há histórico suficiente.
2. **Fallback Média Simples** quando pouca informação (<4 meses).

O modelo preditivo original (Gradient Boosting) permanece para rotas `/predictions/*` enquanto a nova rota usa abordagem por série temporal.

---

## 🔧 Features Usadas

Total: **16 features**

### Features Históricas (Lag)

- `prev_total_quantity` - Quantidade do mês anterior
- `prev_avg_quantity` - Média do mês anterior
- `prev_num_orders` - Número de pedidos anteriores
- `prev_stock` - Estoque anterior

### Features de Tendência

- `ma3_quantity` - Média móvel 3 meses (quantidade)
- `ma3_orders` - Média móvel 3 meses (pedidos)
- `quantity_growth_rate` - Taxa de crescimento

### Features de Estoque

- `current_stock` - Estoque atual
- `minimum_stock` - Estoque mínimo
- `maximum_stock` - Estoque máximo
- `stock_coverage` - Cobertura de estoque

### Features Temporais

- `year` - Ano
- `month` - Mês
- `weekend_orders` - Pedidos em fim de semana
- `avg_days_to_delivery` - Tempo médio de entrega

### Outras

- `item_id` - ID do item
- `unique_order_count` - Pedidos únicos

---

## 🐳 Docker

### Build

```bash
docker build -t prediction-api .
```

### Run

```bash
docker run -p 8000:8000 \
  -e PGHOST=host.docker.internal \
  -e PGPORT=5433 \
  -e PGDATABASE=teste \
  -e PGUSER=postgres \
  -e PGPASSWORD=fatec \
  prediction-api
```

---

## 🔐 Variáveis de Ambiente

Crie `.env` baseado em `.env.example`:

```env
# Banco de dados
PGHOST=localhost
PGPORT=5433
PGDATABASE=teste
PGUSER=postgres
PGPASSWORD=fatec

# API
API_HOST=0.0.0.0
API_PORT=8000
```

---

## 📈 Performance

### Métricas de Avaliação

- **R² (R-squared)**: 0.9426

  - Quanto mais próximo de 1, melhor
  - Entre 0.6-0.8 é considerado bom para séries temporais
  - Acima de 0.95 pode indicar overfitting

- **MAE (Mean Absolute Error)**: 6.17

  - Erro médio absoluto
  - Em unidades do target (quantidade)

- **RMSE (Root Mean Squared Error)**: 8.81
  - Penaliza erros maiores
  - Também em unidades do target

### Comparação de Modelos

| Modelo                | R²         | MAE      | RMSE     |
| --------------------- | ---------- | -------- | -------- |
| Linear Regression     | 0.6419     | 17.85    | 21.99    |
| Random Forest         | 0.9128     | 6.17     | 10.85    |
| **Gradient Boosting** | **0.9426** | **6.17** | **8.81** |

---

## 🚨 Troubleshooting

### Modelo não encontrado

```bash
# Execute o notebook de treinamento
jupyter notebook notebooks/training_ai_v2.ipynb
```

### Erro de conexão com banco

```bash
# Verifique se PostgreSQL está rodando
psql -h localhost -p 5433 -U postgres -d teste
```

### FastAPI não instalado

```bash
pip install fastapi uvicorn pydantic
```

### Porta 8000 ocupada

```bash
# Verificar processo na porta
lsof -i :8000

# Usar outra porta
uvicorn src.api:app --port 8001
```

---

**Última atualização**: 03/11/2024  
**Versão**: 2.0.0
