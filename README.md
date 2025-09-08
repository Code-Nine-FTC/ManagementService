
# ManagementService

Instruções rápidas para rodar localmente usando Docker Compose para o Database (PostgreSQL 15).

## Variáveis e profiles

O projeto usa profiles Spring (`dev`/`prod`). As propriedades de conexão estão em `src/main/resources/application-*.properties`.

Por padrão o profile `dev` usa variáveis de ambiente `DB_URL`, `DB_USER`, `DB_PASSWORD` com defaults internos.

## Docker Compose (desenvolvimento)

O arquivo `docker-compose.yml` do projeto inicia um container Postgres configurado para desenvolvimento. Por padrão ele mapeia a porta do host `5432` para a `5432` do container (NÃO RECOMENDADO).

Subir o banco:

```bash
cd /Users/pedromartinsdeoliveira/api-mobile/ManagementService
docker compose up -d
```

Ver status e logs:

```bash
docker compose ps
docker compose logs -f db
```

Se quiser recriar o volume e forçar a execução dos scripts de inicialização (`POSTGRES_INITDB_ARGS` e `./initdb`), pare e remova volumes (ATENÇÃO: isto apaga os dados):

```bash
docker compose down -v
docker compose up -d
```

## Testar a configuração do Postgres (dentro do container)

```bash
docker compose exec db psql -U postgres -d teste -c 'show shared_buffers; show effective_cache_size; show work_mem; show maintenance_work_mem; show log_min_duration_statement;'
```

Alternativa:

```bash
psql "postgresql://postgres:fatec@localhost:5432/teste"
```

## Deploy

Linux (exportar variáveis para o ambiente de execução):

```bash
export DB_URL=jdbc:postgresql://dbroute:5432/dbname
export DB_USER=username
export DB_PASSWORD=dbpassword
export SPRING_PROFILES_ACTIVE=prod
```

Windows (setx):

```powershell
setx DB_URL "jdbc:postgresql://dbroute:5432/dbname"
setx DB_USER "username"
setx DB_PASSWORD "dbpassword"
setx SPRING_PROFILES_ACTIVE "prod"
```


### macOS

No macOS exporte as variáveis no shell (zsh/bash) e rode o jar:

```bash
export DB_URL=jdbc:postgresql://dbroute:5432/dbname
export DB_USER=username
export DB_PASSWORD=dbpassword
export SPRING_PROFILES_ACTIVE=prod

./mvnw package
java -jar target/managementservice-0.0.1-SNAPSHOT.jar
```

## Variáveis do sistema Postgres e parâmetros usados no Compose

O `docker-compose.yml` do projeto define várias variáveis/parametros que afetam tanto a inicialização do cluster quanto o comportamento em tempo de execução.

- `DB_URL` — URL JDBC que a aplicação Spring usará para conectar ao banco. Ex.: `jdbc:postgresql://db:5432/teste`. Quando a app roda dentro do mesmo compose, use `db` como hostname.
- `DB_USER` / `DB_PASSWORD` — credenciais usadas pela aplicação para conectar.
- `POSTGRES_USER` / `POSTGRES_PASSWORD` / `POSTGRES_DB` — variáveis de entrypoint do Postgres que, na primeira inicialização do volume, são usadas para criar o usuário e database iniciais.
- `POSTGRES_INITDB_ARGS` — argumentos passados para `initdb` na criação do cluster; útil para definir `--encoding` e `--locale` (só tem efeito na primeira inicialização do volume de dados).

Parâmetros de runtime passados via `command` (sintaxe `postgres -c chave=valor`):

- `shared_buffers` — memória reservada pelo Postgres para cache de páginas. Recomendado ~25% da RAM em setups simples. No compose foi configurado para `2GB` (em máquina de 8GB).
- `effective_cache_size` — estimativa da quantidade de memória que o sistema operacional e o Postgres usarão para cache; ajuda no planner para estimar custos. Configurado para `4GB`.
- `work_mem` — memória por operação de ordenação/hash; se muitas operações simultâneas usarem muito work_mem, o consumo total pode crescer. Configurado para `8MB` aqui.
- `maintenance_work_mem` — memória para operações de manutenção (VACUUM, CREATE INDEX). Configurado para `256MB`.
- `log_min_duration_statement` — controla o log de queries lentas em ms; `10000` significa logar queries que demoram >= 10s.

Boas práticas rápidas:
- Se mudar `POSTGRES_INITDB_ARGS`, recrie o volume (`docker compose down -v`) para que `initdb` seja re-executado.
- Ajuste `shared_buffers`/`effective_cache_size` conforme memória disponível no host.
- Em produção, considere usar um arquivo `postgresql.conf` versionado ou uma solução de tuning mais avançada.

