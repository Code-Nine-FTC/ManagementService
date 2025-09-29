# ManagementService – Manual de Instalação

## Visão Geral

O ManagementService é um backend Java Spring Boot para o gerenciamento de seções (como almoxarifado e farmácia), usuários, fornecedores e inventário de itens, utilizando PostgreSQL como banco de dados. O projeto utiliza perfis Spring (`dev` e `prod`) e pode ser executado localmente usando Docker Compose para facilitar a configuração.

---

## 1. Requisitos

- **JDK**: Java 17 ou superior
- **Maven**: 3.6+ (opcional, wrapper incluído)
- **Docker**: Para banco de dados PostgreSQL local (recomendado)
- **Git**: Para clonar o repositório

---

## 2. Clonando o Repositório

```bash
git clone https://github.com/Code-Nine-FTC/ManagementService.git
cd ManagementService
```

---

## 3. Configuração de Ambiente

### Perfis Spring

- O projeto utiliza perfis Spring: `dev` (padrão para desenvolvimento), `prod` (para produção).
- As propriedades de conexão estão em `src/main/resources/application-*.properties`.
- Por padrão, o perfil `dev` lê as variáveis de ambiente: `DB_URL`, `DB_USER`, `DB_PASSWORD`. Valores padrão são definidos para desenvolvimento local.

---

## 4. Banco de Dados com Docker Compose (Desenvolvimento)

O arquivo `docker-compose.yml` disponibiliza um container PostgreSQL.

#### Subir o Banco de Dados

```bash
docker compose up -d
```

#### Verificar status/logs

```bash
docker compose ps
docker compose logs -f db
```

#### Recriar Volume do Banco (para executar init scripts novamente; ATENÇÃO: apaga os dados)

```bash
docker compose down -v
docker compose up -d
```

---

## 5. Executando a Aplicação

### Usando o Maven Wrapper

```bash
./mvnw spring-boot:run
```

Ou, se possuir Maven instalado:

```bash
mvn spring-boot:run
```

---

## 6. Build & Execução via JAR

1. **Gerar o pacote:**

    ```bash
    ./mvnw package
    ```

2. **Definir variáveis de ambiente** (exemplos abaixo):

    - **Linux/macOS:**
      ```bash
      export DB_URL=jdbc:postgresql://localhost:5432/seudb
      export DB_USER=seuusuario
      export DB_PASSWORD=suasenha
      export SPRING_PROFILES_ACTIVE=prod
      ```
    - **Windows:**
      ```powershell
      setx DB_URL "jdbc:postgresql://localhost:5432/seudb"
      setx DB_USER "seuusuario"
      setx DB_PASSWORD "suasenha"
      setx SPRING_PROFILES_ACTIVE "prod"
      ```

3. **Executar:**
    ```bash
    java -jar target/managementservice-0.0.1-SNAPSHOT.jar
    ```

---

## 7. Lint e Análise Estática

### PMD (análise estática Java)

```bash
./mvnw pmd:check
# ou
mvn pmd:check
```
Relatório HTML:
```bash
./mvnw pmd:pmd
# ou
mvn pmd:pmd
# Relatório em: target/site/pmd.html
```

### Spotless (formatação de código)

```bash
./mvnw spotless:apply   # corrige formatação automaticamente
./mvnw spotless:check   # verifica formatação
```

---

## 8. Variáveis de Configuração

- `DB_URL` — URL JDBC para o Spring conectar ao banco (ex.: `jdbc:postgresql://db:5432/teste`)
- `DB_USER` / `DB_PASSWORD` — credenciais do banco de dados
- `POSTGRES_USER` / `POSTGRES_PASSWORD` / `POSTGRES_DB` — variáveis de inicialização do container PostgreSQL
- `POSTGRES_INITDB_ARGS` — argumentos para o `initdb` (apenas na primeira inicialização do volume)

---

## 9. Boas Práticas

- Ao alterar `POSTGRES_INITDB_ARGS`, recrie o volume Docker para reexecutar os scripts de inicialização.
- Ajuste configurações do PostgreSQL para produção conforme necessário.
- Em produção, use um arquivo `postgresql.conf` versionado se possível.

---

## 10. Estrutura de Pastas

- `src/main/java/com/codenine/managementservice/` — código-fonte Java principal
    - `config/` — configurações e carregamento de dados
    - `entity/` — entidades JPA (User, Section, SupplierCompany, etc)
    - `repository/` — repositórios Spring Data
    - `service/` — regras de negócio/serviços
    - `security/` — autenticação e JWT
    - `utils/` — utilidades
- `src/main/resources/` — recursos como propriedades e dados Excel
- `docker-compose.yml` — banco local
- `README.md` — guia rápido e detalhes

---

## 11. Solução de Problemas

- Problemas de conexão: confira as variáveis de ambiente e a configuração do banco no Docker Compose.
- Para dados de teste, a classe **DataLoader** popula seções, fornecedores e usuários admin no perfil de desenvolvimento.

---
