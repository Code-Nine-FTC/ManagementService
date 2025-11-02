FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    TZ=UTC

WORKDIR /app

COPY requirements.txt /app/
RUN pip install --no-cache-dir -r requirements.txt

COPY train_forecast.py /app/

# default: run once; override with MODE=schedule to run via APScheduler
ENV MODE=once

CMD ["python", "-u", "/app/train_forecast.py"]
