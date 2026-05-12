# GestãoSerralharia

Aplicação para gestão operacional de uma serralharia, com backend REST em Spring Boot, cliente desktop em Java Swing e um frontend web em React/Vite.

## Estrutura do projeto

```text
GestaoSerralharia/
├── backend/   # API REST + regras de negócio + acesso a dados (Spring Boot)
├── desktop/   # Cliente desktop (Java Swing) que consome a API
├── web/       # Frontend web (React + Vite)
├── pom.xml    # Projeto Maven pai (agrega backend e desktop)
└── mvnw       # Maven Wrapper
```

Notas:
- Os módulos Maven ativos estão em `backend` e `desktop`.
- A pasta `web` é independente do Maven e usa `npm`.

## Tecnologias

- Java 21
- Spring Boot 3.4.x
- Maven Wrapper (`./mvnw`)
- PostgreSQL
- Java Swing (desktop)
- React 19 + Vite (web)

## Pré-requisitos

Antes de executar, instala:

- JDK 21
- PostgreSQL (com servidor ativo)
- Node.js 20+ e npm (para a pasta `web`)

## Configuração da base de dados

As configurações atuais estão em:

- `backend/src/main/resources/application.properties`
- `desktop/src/main/resources/application.properties`

Valores por defeito no repositório:

- URL: `jdbc:postgresql://localhost:5432/gestao_serralharia`
- Utilizador: `postgres`
- Password: `root`

Cria a base de dados antes de arrancar:

```sql
CREATE DATABASE gestao_serralharia;
```

Se necessário, altera utilizador/password no `application.properties`.

## Como executar

### 1) Backend (API)

Na raiz do projeto:

```bash
./mvnw -pl backend spring-boot:run
```

A API fica disponível em `http://localhost:8080`.

### 2) Desktop (cliente Java)

Com o backend já a correr, noutra consola:

```bash
./mvnw -pl desktop spring-boot:run
```

O cliente desktop usa por defeito:

- `desktop.api.base-url=http://localhost:8080`

Se a API estiver noutra porta/host, ajusta em `desktop/src/main/resources/application.properties`.

### 3) Web (React + Vite)

Na pasta `web`:

```bash
npm install
npm run dev
```

Para build de produção:

```bash
npm run build
npm run preview
```

## Testes

Executar testes do backend:

```bash
./mvnw -pl backend test
```

Executar testes de tudo (módulos Maven):

```bash
./mvnw test
```

## Build do projeto

Build Maven completo (backend + desktop):

```bash
./mvnw clean install
```

## Problemas comuns

- Erro de ligação à base de dados:
  - Verifica se o PostgreSQL está ativo.
  - Confirma nome da base de dados, utilizador e password no `application.properties`.

- Desktop sem comunicar com backend:
  - Confirma se o backend está ativo em `http://localhost:8080`.
  - Confirma `desktop.api.base-url`.

- Porta 8080 ocupada:
  - Fecha o processo que usa a porta ou muda `server.port` no `application.properties` do backend.

## Sugestão para entrega no GitHub

No topo do repositório, mantém este `README.md` como ponto de entrada e inclui:

- O que o projeto faz
- Estrutura por módulos
- Pré-requisitos
- Passos de execução por componente
- Problemas comuns

Assim qualquer utilizador consegue clonar, configurar e arrancar sem depender de contexto externo.
