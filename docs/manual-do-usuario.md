# ManagementService – Manual do Usuário

## Visão Geral

O ManagementService é um sistema backend para gerenciamento de usuários, seções (setores), fornecedores e itens de estoque. Possui controle de acesso baseado em papéis (ADMIN, ASSISTANT, MANAGER) e oferece endpoints para operações CRUD em todas as entidades principais.

---

## 1. Papéis de Usuário

- **ADMIN**: Acesso total a todas as funcionalidades e seções.
- **MANAGER**: Acesso parcial, geralmente limitado aos setores vinculados.
- **ASSISTANT**: Acesso restrito, normalmente para consulta ou atualização de itens.

---

## 2. Funcionalidades Principais

- **Gestão de Usuários**: Criar, atualizar, ativar/desativar e listar usuários. Usuários são associados a uma ou mais seções.
- **Gestão de Seções**: Criar, atualizar, desabilitar ou excluir setores (ex: Almoxarifado, Farmácia).
- **Gestão de Fornecedores**: Cadastro e gerenciamento de empresas fornecedoras.
- **Gestão de Itens e Estoque**: Cadastro, importação e atualização de itens, tipos de itens e quantidades em estoque.

---

## 3. Autenticação e Autorização

- Autenticação via JWT (JSON Web Token).
- O usuário faz login com e-mail e senha e recebe um token.
- O token contém o papel do usuário e os IDs das seções para controle de acesso.

---

## 4. Fluxos de Uso Comuns

### 4.1. Login de Usuário

- Autentique-se usando e-mail e senha.
- Receba um token JWT para as próximas requisições.

### 4.2. Gerenciamento de Usuários

- **Criar Usuário**: Requer ADMIN ou MANAGER com permissão.
- **Campos**: `name`, `email`, `password`, `role` (ADMIN/ASSISTANT/MANAGER), `sectionIds` (IDs dos setores).

### 4.3. Gerenciamento de Seções

- **Criar/Atualizar Seção**: Requer ADMIN.
- **Desabilitar/Excluir Seção**: Apenas ADMIN pode desabilitar ou excluir.
- **Filtrar Seções**: Por status ativo, papel de acesso ou último usuário.

### 4.4. Gerenciamento de Fornecedores

- **Operações CRUD** para empresas fornecedoras, incluindo CNPJ, contato e status.

### 4.5. Gerenciamento de Itens

- **Criar/Importar Itens**: Itens podem ser importados de arquivos Excel (veja `src/main/resources/almoxarifado.xlsx`, `farmacia.xlsx`).
- **Operações CRUD**: Para itens e tipos de itens, vinculados a setores.

---

## 5. DataLoader: Dados de Teste em Desenvolvimento

No perfil `dev`, o sistema popula automaticamente:

- Seções de exemplo: "Almoxarifado", "Farmácia"
- Fornecedores de exemplo: "Indústria Militar Brasileira", etc.
- Usuário admin: `codenine@email.com` / `codenine123`

---

## 6. Estrutura da API

> **Obs:** Para detalhes completos dos endpoints, consulte o código (package `controller`, não exibido acima) ou a documentação Swagger/OpenAPI gerada automaticamente, se disponível.

- **Usuários**: `/users`
    - Criar, atualizar, desabilitar, buscar por ID, listar com filtros.
- **Seções**: `/sections`
    - Criar, atualizar, excluir, listar, filtrar.
- **Fornecedores**: `/suppliers`
    - CRUD.
- **Itens**: `/items`, `/item-types`
    - CRUD, importação, associação com setores.
- **Autenticação**: `/auth/login`

---

## 7. Segurança

- Todos os endpoints críticos requerem JWT válido.
- O acesso é controlado por papel e, em muitos casos, pelas seções vinculadas.

---

## 8. Arquivos Internos Úteis

- `src/main/java/com/codenine/managementservice/config/DataLoader.java`: Populador de dados em desenvolvimento.
- `src/main/resources/application-dev.properties` e `application-prod.properties`: Configurações por ambiente.
- `src/main/resources/almoxarifado.xlsx` e `farmacia.xlsx`: Dados de exemplo para itens.

---

## 9. Boas Práticas

- Mantenha seu token seguro.
- O admin deve trocar senhas padrão e criar usuários reais para produção.
- Rode periodicamente os linters e verificadores de boas práticas (PMD, Spotless) conforme explicado no manual de instalação.

---

## 10. Solução de Problemas

- **Falha no login**: Confira as credenciais e se o banco está rodando.
- **Dados sumiram após reinício**: Se usar Docker Compose, verifique volumes persistentes do PostgreSQL.
- **Erros de permissão**: Confira o papel do usuário e as seções associadas.

---

Para mais detalhes técnicos, consulte o código-fonte do repositório.
