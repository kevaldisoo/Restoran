# Restoran

Restaurant table booking application with a Spring Boot backend and Vue 3 frontend.

## Prerequisites

- Java 21+
- Node.js 20.19+ or 22.12+
- npm

## Running the Backend

From the project root:

```bash
./gradlew bootRun
```

The backend starts on **http://localhost:8080**.

H2 console available at **http://localhost:8080/h2-console**
(JDBC URL: `jdbc:h2:mem:restorandb`, username: `sa`, password: *(empty)*)

## Running the Frontend

From the `frontend/` directory:

```bash
npm install
npm run dev
```

The frontend dev server starts on **http://localhost:5173**.
