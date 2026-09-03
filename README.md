# Metalworking Workshop Management

Application for operational management of a metalworking workshop, with a REST backend in Spring Boot, a desktop client in Java Swing, and a web frontend in React/Vite.

## Project structure

```text
GestaoSerralharia/
├── backend/   # REST API + business rules + data access (Spring Boot)
├── desktop/   # Desktop client (Java Swing) that consumes the API
├── web/       # Web frontend (React + Vite)
├── pom.xml    # Parent Maven project (aggregates backend and desktop)
└── mvnw       # Maven Wrapper
```

Notes:

* The active Maven modules are in `backend` and `desktop`.
* The `web` folder is independent of Maven and uses `npm`.

## Technologies

* Java 21
* Spring Boot 3.4.x
* Maven Wrapper (`./mvnw`)
* PostgreSQL
* Java Swing (desktop)
* React 19 + Vite (web)

## Prerequisites

Before running, install:

* JDK 21
* PostgreSQL (with an active server)
* Node.js 20+ and npm (for the `web` folder)

## Database configuration

The current configurations are in:

* `backend/src/main/resources/application.properties`
* `desktop/src/main/resources/application.properties`

Default values in the repository:

* URL: `jdbc:postgresql://localhost:5432/gestao_serralharia`
* User: `postgres`
* Password: `root`

Create the database before starting:

```sql
CREATE DATABASE gestao_serralharia;
```

If necessary, change the user/password in `application.properties`.

## How to run

### 1) Backend (API)

In the project root:

```bash
./mvnw -pl backend spring-boot:run
```

The API is available at `http://localhost:8080`.

### 2) Desktop (Java client)

With the backend already running, in another console:

```bash
./mvnw -pl desktop spring-boot:run
```

The desktop client uses by default:

* `desktop.api.base-url=http://localhost:8080`

If the API is on another port/host, adjust it in `desktop/src/main/resources/application.properties`.

### 3) Web (React + Vite)

In the `web` folder:

```bash
npm install
npm run dev
```

For a production build:

```bash
npm run build
npm run preview
```

## Tests

Run backend tests:

```bash
./mvnw -pl backend test
```

Run all tests (Maven modules):

```bash
./mvnw test
```

## Project build

Full Maven build (backend + desktop):

```bash
./mvnw clean install
```

## Common issues

* Database connection error:

  * Check that PostgreSQL is active.
  * Confirm the database name, user, and password in `application.properties`.

* Desktop not communicating with backend:

  * Confirm that the backend is running at `http://localhost:8080`.
  * Confirm `desktop.api.base-url`.

* Port 8080 is occupied:

  * Close the process using the port or change `server.port` in the backend's `application.properties`.
