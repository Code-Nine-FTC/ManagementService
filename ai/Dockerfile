FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    TZ=America/Sao_Paulo

WORKDIR /app

# Instalar curl para health checks
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Instalar dependências
COPY requirements.txt /app/
RUN pip install --no-cache-dir -r requirements.txt

# Copiar código fonte
COPY . /app/

# Expor porta da API
EXPOSE 8000

# Variáveis de ambiente padrão
ENV PGHOST=db \
    PGPORT=5432 \
    PGDATABASE=teste \
    PGUSER=postgres \
    PGPASSWORD=fatec \
    API_HOST=0.0.0.0 \
    API_PORT=8000

# Comando padrão: rodar API
CMD ["uvicorn", "src.api:app", "--host", "0.0.0.0", "--port", "8000"]
