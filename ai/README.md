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
├── notebooks/
│   ├── training_ai_v2.ipynb         # ✅ Notebook
│   
│
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

### 2️⃣ Treinar Modelo (se necessário)

```bash
# Abrir e executar o notebook
jupyter notebook notebooks/training_ai_v2.ipynb
```

### 3️⃣ Iniciar API

```bash
python scripts/start_api.py
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

### Dados Brutos

✅ Usa dados brutos diretamente das tabelas:

- `items` - Informações dos itens
- `orders` - Pedidos realizados
- `order_item` - Itens dos pedidos
- `purchase_orders` - Ordens de compra
- `sections` - Seções/categorias
- `supplier_company` - Fornecedores
- `item_types` - Tipos de itens

**Vantagens**:

- ✅ Sem data leakage
- ✅ Maior granularidade
- ✅ R² realista (0.60-0.95)
- ✅ Melhor generalização


---

## 🤖 Modelos Disponíveis

O sistema testa 3 algoritmos e escolhe o melhor:

1. **Linear Regression** - Baseline simples
2. **Random Forest** - Ensemble com árvores
3. **Gradient Boosting** - ⭐ **Melhor performance**

**Resultado atual**:

- Modelo: Gradient Boosting
- R²: 0.9426
- MAE: 6.17
- RMSE: 8.81

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
