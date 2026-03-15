# Restoran

Restorani rakendus, mis kasutab Vue frontendi ja Spring Boot backendi.

## Eeldused

- Java 21+
- Node.js 20.19+ või 22.12+
- npm

## Backend jooksutamine (ilma dockerita)

root kaustas:

```bash
./gradlew bootRun
```

Backend jookseb lehel **http://localhost:8080**.

H2 konsoolile saab ligi **http://localhost:8080/h2-console**
(JDBC URL: `jdbc:h2:mem:restorandb`, username: `sa`, password: *(tühi)*)

## Frontend jooksutamine (ilma dockerita)

`frontend/` kaustas:

```bash
npm install
npm run dev
```

Frontend serverile saab ligi veebilehel **http://localhost:5173**.

## Dockeris jooksutamine (soovitatav)

Veendu, et Docker Desktop on käivitatud.

root kaustas:

```bash
docker compose up --build
```

Rakendus on saadaval aadressil **http://localhost**.

- Frontend (nginx) jookseb pordil **80**
- Backend jookseb konteinerite sisevõrgus ja ei ole otse väljastpoolt ligipääsetav

Peatamiseks:

```bash
docker compose down
```

## E2E testide jooksutamine (Cypress)

Cypress testid jooksevad **preview buildil** (`localhost:4173`).

**Nii backend kui ka frontend server peavad olema käivitatud enne Cypressi tööle panemist**

### 1. Alusta Backend

root kaustas:

```bash
./gradlew bootRun
```

### 2. Alusta frontend preview server

`frontend/` kaustas:

```bash
npm run build
npm run preview
```

Preview server alustab aadressil **http://localhost:4173**.

### 3. Jooksuta Cypress

From the `frontend/` directory:

Avab Cypressi UI:

```bash
npx cypress open
```

Kõik testid terminalis:

```bash
npx cypress run
```

Testifailid on kaustas `frontend/cypress/e2e/`.
