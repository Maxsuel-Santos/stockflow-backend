# 📈 Agregador de Investimentos API - Backend

![Status do Projeto](https://img.shields.io/badge/status-active-brightgreen)
![Java Version](https://img.shields.io/badge/java-21-orange)
![Spring Boot](https://img.shields.io/badge/spring--boot-4.0.1-brightgreen)
![Docker](https://img.shields.io/badge/docker-ready-blue)

Esta é uma API RESTful de alta performance projetada para gestão de ativos financeiros. O sistema oferece desde o controle básico de carteiras até o rastreamento detalhado de transações (Buy/Sell), com monitoramento em tempo real e integração com o mercado financeiro.

---

## 🖼️ Arquitetura e Modelo de Dados

Abaixo está o diagrama que ilustra o relacionamento entre as entidades do sistema (User, Transactions, Stock, Account, Account Stock e Billing Address).

![Diagrama de Relacionamento](agregador-de-investimentos-MER.png)

---

## 🛠️ Tecnologias Utilizadas

### Core Backend
* **Java 21** & **Spring Boot 4.0.1**: Base moderna com foco em performance.
* **Spring Security & JWT**: Autenticação stateless com renovação de tokens e proteção de rotas.
* **Spring Data JPA**: Persistência robusta com PostgreSQL.
* **Spring Cloud OpenFeign**: Integração declarativa com a API **Brapi** para cotações em tempo real.
* **MapStruct**: Mapeamento de objetos (DTO <-> Entity) performático e sem boilerplate.
* **Resilience4j**: Circuit Breaker e Retry para garantir estabilidade nas chamadas externas.

### Observabilidade & Monitoramento (Grafana Stack) 📊
O projeto implementa o conceito de "Full Observability":
* **Micrometer Tracing & OTLP**: Instrumentação para rastreamento distribuído.
* **Prometheus**: Coleta de métricas detalhadas da JVM e do tráfego HTTP.
* **Grafana Tempo**: Armazenamento de *Traces* para analisar o ciclo de vida de cada requisição.
* **Grafana Dashboards**: Visualização centralizada de métricas e saúde do sistema.

### Infraestrutura
* **Docker & Docker Compose**: Orquestração completa (App, DB, Prometheus, Grafana, Tempo).
* **Global Exception Handling**: Tratamento de erros padronizado com suporte a fusos horários (Brasília).
* **SpringDoc OpenAPI**: Documentação interativa via Swagger UI.

---

## 🔒 Funcionalidades de Segurança (Destaques)

### 🛡️ Hashing de Senhas
As senhas nunca são armazenadas em texto plano. Utilizamos o **BCryptPasswordEncoder** tanto na criação quanto na atualização do perfil.

### 🚫 Global Exception Handler
Tratamento centralizado de erros que fornece respostas claras e seguras via DTOs, evitando vazamento de stacktraces do servidor.

---
## 📊 Principais Endpoints (API Reference)

A documentação completa e interativa (Swagger UI) pode ser acessada em: `http://localhost:8080/swagger-ui.html`.

### 🔐 Autenticação & Perfil (Acesso Público e Privado)
| Método | Endpoint         | Descrição                                |
|:-------|:-----------------|:-----------------------------------------|
| `POST` | `/auth/register` | Registra um novo usuário.                |
| `POST` | `/auth/login`    | Autentica e retorna o Bearer Token JWT.  |
| `GET`  | `/auth/me`       | Retorna dados do usuário logado (JWT).   |
| `POST` | `/auth/logout`   | Invalida o token atual.                  |

### 👤 Gestão de Usuários (Requer ADMIN)
| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `GET` | `/users/all` | Lista todos os usuários do sistema. |
| `GET` | `/users/{userId}` | Detalhes de um perfil específico. |
| `PUT` | `/users/{userId}` | Atualiza dados (nome, e-mail). |
| `DELETE` | `/users/{userId}` | Remove permanentemente o usuário. |
| `POST` | `/users/{userId}/avatar` | Upload da foto de perfil (Multipart - Max 5MB). |
| `DELETE` | `/users/{userId}/avatar` | Remove a foto de perfil do storage. |

### 📈 Mercado & Ativos (Catálogo)
| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `POST` | `/stocks` | Cadastra uma nova ação/ticker no sistema. |
| `GET` | `/stocks` | Lista resumo de ativos do usuário logado. |

### 💸 Operações Financeiras (Trades)
| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `POST` | `/trades/buy` | **Executa Compra:** Valida saldo e preço real (Brapi). |
| `POST` | `/trades/sell` | **Executa Venda:** Valida posse e credita valor. |
| `GET` | `/trades/history` | Histórico cronológico de operações (Brasília Time). |
| `GET` | `/trades/portfolio/{accountId}` | Portfólio completo: Patrimônio + Valor de Mercado. |

### 💳 Contas & Carteiras
| Método   | Endpoint                        | Descrição                                          |
|:---------|:--------------------------------|:---------------------------------------------------|
| `POST`   | `/users/{userId}/accounts`      | Cria uma nova carteira para o usuário.             |
| `GET`    | `/users/{userId}/accounts`      | Lista carteiras e saldos consolidados.             |
| `GET`    | `/accounts/{accountId}/balance` | Cálculo de patrimônio simplificado (Total Equity). |
| `DELETE` | `/accounts/{accountId}`         | Deleta uma conta caso não haja ações vinculadas.   |
---

## ⚙️ Configuração e Execução

### 1. Requisitos Prévios
* **Java 21** e **Maven** (para execução local).
* **Docker & Docker Compose** (para execução via containers).
* **Token Brapi**: Obtenha sua chave gratuita em [brapi.dev](https://brapi.dev).

### 2. Variáveis de Ambiente
O projeto utiliza variáveis de ambiente para proteger dados sensíveis. Se estiver utilizando o **IntelliJ IDEA**, você pode configurá-las facilmente:
1. Vá ao menu `Run` > `Edit Configurations...`.
2. No campo **Environment Variables**, adicione as seguintes chaves:
   ```env
   TOKEN=seu_token_aqui;JWT_SECRET=sua_chave_secreta_segura

### 3. Execução com Docker 🐳
A aplicação está preparada para rodar em containers, gerenciando a API, o banco de dados PostgreSQL e o monitoramento automaticamente.

```bash
# Limpa caches antigos e sobe todo o ecossistema:
docker compose down -v
# Na raiz do projeto, execute o comando abaixo para subir o ecossistema:
docker-compose up --build
```

### 4. Acesso ao Banco de Dados (PostgreSQL)
Caso precise validar dados ou realizar queries manualmente, você pode acessar o terminal do Postgres diretamente pelo container Docker:
```bash
# 1. Acesse o terminal interativo do container de banco de dados
docker exec -it agregador-de-investimento psql -U postgres -d db_investimentos

# 2. Comandos úteis dentro do terminal psql:
\dt                  -> Lista todas as tabelas (users, accounts, stocks, etc.)
SELECT * FROM users; -> Visualiza os usuários cadastrados
\q                   -> Sai do terminal do banco de dados.
```

### 5. Configuração do application.properties
Para desenvolvimento local sem Docker (ex: usando banco H2 ou Postgres local), certifique-se de que as propriedades abaixo apontem para o seu ambiente no arquivo ```src/main/resources/application.properties```:
```bash
# Brapi API Token (Configurado via Variável de Ambiente no IntelliJ)
TOKEN=${TOKEN}

# JWT Security
api.security.token.secret=${JWT_SECRET:minha-chave-secreta-padrao}

# Database (Exemplo PostgreSQL Local)
spring.datasource.url=jdbc:postgresql://localhost:5432/investdb
spring.datasource.username=postgres
spring.datasource.password=sua_senha

# Observabilidade (Actuator & Prometheus)
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.health.show-details=always
```
### 6. Painéis disponíveis após o boot
1. API: http://localhost:8080
2. Swagger UI: http://localhost:8080/swagger-ui.html
3. Grafana: http://localhost:3000 (Login: admin / admin)
4. Prometheus: http://localhost:9090

## 📊 Observabilidade e Monitoramento

O projeto conta com uma stack completa de monitoramento para garantir a saúde da aplicação e facilitar o debug em tempo real.

### 📈 Grafana Dashboards
Ao acessar o Grafana (`localhost:3000`), você pode importar dashboards para visualizar:
* **Métricas da JVM**: Uso de memória Heap, Threads e Coleta de Lixo (GC).
* **Tráfego HTTP**: Taxa de sucesso (2xx), erros (4xx/5xx) e latência dos endpoints.
* **Conexões com Banco**: Performance do pool de conexões (HikariCP).
> **Dica**: Utilize o ID `19022` no menu "Import" do Grafana para um dashboard Spring Boot completo.

### 🔍 Rastreamento Distribuído (Distributed Tracing)
Com o **Grafana Tempo**, é possível rastrear o caminho exato de uma requisição.
* **Onde foi o gargalo?** Você consegue ver exatamente quantos milissegundos a aplicação gastou processando a lógica interna vs. quanto tempo levou a resposta da API externa (Brapi).
* **Debug de Erros**: Se uma transação de compra falhar, o rastro mostrará em qual camada a exceção foi lançada.

### 🛠️ Prometheus Metrics
As métricas brutas são expostas via Spring Actuator em `/actuator/prometheus`, onde o Prometheus faz o "scrape" periódico dos dados.

## 🧪 Testes e Qualidade

O projeto utiliza as principais bibliotecas do ecossistema Java para garantir que a lógica de negócio e as camadas de segurança funcionem conforme o esperado.

### 🛠️ Tecnologias de Teste
* **JUnit 5**: Framework principal para execução dos testes unitários e de integração.
* **Mockito**: Utilizado para criação de mocks, permitindo isolar a lógica de serviço das dependências de banco de dados e APIs externas.
* **AssertJ**: Biblioteca para asserções fluídas e de fácil leitura.
* **Spring Security Test**: Ferramentas específicas para simular usuários autenticados e testar permissões de rotas.

### 📂 Estrutura de Testes
Os testes estão localizados em `src/test/java/` e seguem a hierarquia dos pacotes da aplicação:

1. **Unit Tests (Services)**: Validação de regras de negócio, como o cálculo de patrimônio e validação de senhas no `AuthService`.
2. **Integration Tests (Controllers)**: Garantem que os endpoints estão respondendo corretamente (status 200, 201, 401, 403, 404) e que o `GlobalExceptionHandler` está capturando os erros.

### 🚀 Como Executar os Testes

Você pode rodar todos os testes através da sua IDE (IntelliJ) ou via terminal usando o Maven:

```bash
# Executar todos os testes
mvn test

# Executar um teste específico
mvn test -Dtest=AuthServiceTest
```

## ⚙️ CI/CD (Integração Contínua)

O projeto utiliza **GitHub Actions** para garantir a qualidade do código em cada contribuição. O pipeline é executado automaticamente em cada `push` ou `pull request` para a branch `main`.

* **Build & Test**: O workflow compila o projeto usando Java 21 e executa toda a suíte de testes unitários e de integração.
* **Segurança**: Garante que novas alterações não quebrem o fluxo de autenticação JWT ou o tratamento global de exceções.
* **Status**: O selo de "Build" no topo do repositório indica se a versão atual está estável.

## 📄 Licença
Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---
