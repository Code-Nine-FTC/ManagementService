# 🚀 Guia de Uso da API de Previsões ML

Este guia mostra como usar os modelos de Machine Learning treinados para gerar previsões e integrá-los com o serviço Java.

## 📋 Sumário

1. [Instalação](#instalação)
2. [Iniciando a API](#iniciando-a-api)
3. [Endpoints Disponíveis](#endpoints-disponíveis)
4. [Consumindo do Java](#consumindo-do-java)
5. [Exemplos de Uso](#exemplos-de-uso)

---

## 🔧 Instalação

### 1. Instalar dependências Python

```bash
cd ai/
pip install -r requirements.txt
```

### 2. Verificar se os modelos estão salvos

Os modelos devem estar em `ai/models/`:

- `gradient_boosting_v2.pkl` (ou `random_forest_v2.pkl`)
- `scaler_v2.pkl`
- `feature_columns_v2.pkl`
- `model_metadata_v2.pkl`

Se não estiverem, execute o notebook `training_ai_v2.ipynb` até o final.

---

## 🚀 Iniciando a API

### Método 1: Script automático

```bash
cd ai/
python scripts/start_api.py
```

### Método 2: Manual com uvicorn

```bash
cd ai/
uvicorn src.api:app --host 0.0.0.0 --port 8000 --reload
```

### Método 3: Usando Python direto

```bash
cd ai/
python -m src.api
```

A API estará disponível em:

- **Servidor**: http://localhost:8000
- **Documentação Swagger**: http://localhost:8000/docs
- **Documentação ReDoc**: http://localhost:8000/redoc

---

## 📡 Endpoints Disponíveis

### 1. Health Check

```http
GET /health
```

**Resposta**:

```json
{
  "status": "healthy",
  "timestamp": "2024-11-03T10:30:00",
  "model_loaded": true,
  "database_connected": false
}
```

### 2. Informações do Modelo

```http
GET /model/info
```

**Resposta**:

```json
{
  "model_loaded": true,
  "model_type": "GradientBoostingRegressor",
  "num_features": 16,
  "metadata": {
    "model_name": "gradient_boosting",
    "r2_score": 0.9426,
    "mae": 6.17,
    "rmse": 8.81
  }
}
```

### 3. Previsão para UM item

```http
GET /predictions/item/{item_id}
```

**Exemplo**:

```bash
curl http://localhost:8000/predictions/item/1
```

**Resposta**:

```json
{
  "item_id": 1,
  "predicted_quantity": 150.5,
  "current_stock": 100.0,
  "minimum_stock": 50.0,
  "maximum_stock": 300.0,
  "prediction_month": 12,
  "prediction_year": 2024,
  "needs_restock": true,
  "restock_quantity": 50.5,
  "confidence_score": 0.94,
  "model_used": "gradient_boosting",
  "timestamp": "2024-11-03T10:30:00"
}
```

### 4. Previsão para MÚLTIPLOS itens

```http
GET /predictions/items?item_ids=1,2,3,4,5
```

**Exemplo**:

```bash
curl "http://localhost:8000/predictions/items?item_ids=1,2,3"
```

**Resposta**: Array de previsões

### 5. Previsão para TODOS os itens

```http
GET /predictions/all
```

**Exemplo**:

```bash
curl http://localhost:8000/predictions/all
```

⚠️ **ATENÇÃO**: Este endpoint pode demorar dependendo do volume de dados!

---

## ☕ Consumindo do Java

### 1. Criar o DTO (Data Transfer Object)

Copie o arquivo `docs/exemplo-java-dto.java` para:

```
src/main/java/com/codenine/managementservice/dto/prediction/PredictionResponse.java
```

### 2. Criar o Service

Copie o arquivo `docs/exemplo-java-service.java` para:

```
src/main/java/com/codenine/managementservice/service/PredictionApiService.java
```

### 3. Configurar a URL da API

No `application.properties`:

```properties
# URL da API Python de previsões
prediction.api.url=http://localhost:8000
```

### 4. Configurar RestTemplate

Adicione ao seu `@Configuration`:

```java
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```

### 5. Usar no Controller

```java
@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    @Autowired
    private PredictionApiService predictionApiService;

    @GetMapping("/item/{itemId}")
    public ResponseEntity<PredictionResponse> getPrediction(@PathVariable Long itemId) {
        PredictionResponse prediction = predictionApiService.getPredictionForItem(itemId);
        return ResponseEntity.ok(prediction);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PredictionResponse>> getAllPredictions() {
        List<PredictionResponse> predictions = predictionApiService.getAllPredictions();
        return ResponseEntity.ok(predictions);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Boolean>> checkHealth() {
        boolean healthy = predictionApiService.isApiHealthy();
        return ResponseEntity.ok(Map.of("api_available", healthy));
    }
}
```

---

## 🧪 Exemplos de Uso

### Exemplo 1: Teste Local (Python)

```bash
cd ai/
python scripts/test_predictions.py
```

Este script irá:

1. Carregar o modelo
2. Conectar ao banco
3. Gerar previsões para todos os itens
4. Mostrar estatísticas

### Exemplo 2: Teste via cURL

```bash
# Health check
curl http://localhost:8000/health

# Previsão de um item
curl http://localhost:8000/predictions/item/1

# Previsão de múltiplos itens
curl "http://localhost:8000/predictions/items?item_ids=1,2,3,4,5"

# Todas as previsões
curl http://localhost:8000/predictions/all
```

### Exemplo 3: Teste via Postman

1. Abra o Postman
2. Importe a URL: `http://localhost:8000/docs` (OpenAPI)
3. Ou faça requisições manualmente

### Exemplo 4: Teste via navegador

Acesse a documentação interativa:

```
http://localhost:8000/docs
```

Você pode testar todos os endpoints diretamente na interface Swagger!

---

## 🔄 Fluxo Completo

### 1. Treinar Modelo (Uma vez)

```bash
# Executar notebook training_ai_v2.ipynb
jupyter notebook ai/notebooks/training_ai_v2.ipynb
```

### 2. Iniciar API Python

```bash
cd ai/
python scripts/start_api.py
```

### 3. Testar API

```bash
curl http://localhost:8000/health
curl http://localhost:8000/predictions/item/1
```

### 4. Iniciar serviço Java

```bash
./mvnw spring-boot:run
```

### 5. Java consome API Python

```
Java Backend (porta 8080)
    ↓ HTTP Request
Python API (porta 8000)
    ↓ ML Prediction
PostgreSQL Database
```

---

## 📊 Significado dos Campos

### Resposta de Previsão

| Campo                | Tipo   | Descrição                              |
| -------------------- | ------ | -------------------------------------- |
| `item_id`            | int    | ID do item no banco                    |
| `predicted_quantity` | float  | Quantidade prevista para o próximo mês |
| `current_stock`      | float  | Estoque atual do item                  |
| `minimum_stock`      | float  | Estoque mínimo configurado             |
| `maximum_stock`      | float  | Estoque máximo configurado             |
| `prediction_month`   | int    | Mês da previsão (1-12)                 |
| `prediction_year`    | int    | Ano da previsão                        |
| `needs_restock`      | bool   | Se precisa fazer reposição             |
| `restock_quantity`   | float  | Quanto precisa repor                   |
| `confidence_score`   | float  | Confiança do modelo (R²)               |
| `model_used`         | string | Qual modelo foi usado                  |
| `timestamp`          | string | Quando a previsão foi gerada           |

---

## ⚙️ Configuração Avançada

### Variáveis de Ambiente

Crie um arquivo `.env` em `ai/`:

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
API_RELOAD=True
```

### Docker (Opcional)

```dockerfile
# ai/Dockerfile
FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

CMD ["uvicorn", "src.api:app", "--host", "0.0.0.0", "--port", "8000"]
```

Rodar com Docker:

```bash
cd ai/
docker build -t prediction-api .
docker run -p 8000:8000 prediction-api
```

---

## 🐛 Troubleshooting

### Erro: "Modelo não encontrado"

- Verifique se os arquivos `*_v2.pkl` existem em `ai/models/`
- Execute o notebook de treinamento completamente

### Erro: "Não consegue conectar ao banco"

- Verifique se o PostgreSQL está rodando
- Confirme as credenciais em `.env`
- Teste a conexão: `psql -h localhost -p 5433 -U postgres -d teste`

### Erro: "FastAPI não instalado"

```bash
pip install fastapi uvicorn pydantic
```

### API não responde

- Verifique se a porta 8000 está livre: `lsof -i :8000`
- Veja os logs do servidor
- Teste o health endpoint: `curl http://localhost:8000/health`

---

## 📚 Recursos Adicionais

- **Swagger UI**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc
- **OpenAPI JSON**: http://localhost:8000/openapi.json

---

## ✅ Checklist de Implantação

- [ ] Modelos treinados e salvos em `models/`
- [ ] Dependências instaladas (`pip install -r requirements.txt`)
- [ ] API Python rodando (porta 8000)
- [ ] Health check OK (`/health`)
- [ ] DTO criado no Java
- [ ] Service criado no Java
- [ ] RestTemplate configurado
- [ ] URL da API configurada no `application.properties`
- [ ] Endpoints testados

---

## 🎯 Próximos Passos

1. **Automatizar retreinamento**: Agendar execução mensal do notebook
2. **Cache**: Implementar cache de previsões no Redis
3. **Logs**: Configurar logging centralizado
4. **Métricas**: Adicionar Prometheus/Grafana
5. **Autenticação**: Adicionar JWT ou API Key
6. **Rate Limiting**: Limitar requisições por cliente
7. **Deployment**: Implantar em produção (Docker/K8s)

---

**Criado em**: 03/11/2024  
**Versão**: 2.0.0  
**Modelo**: Gradient Boosting (R² = 0.94)
