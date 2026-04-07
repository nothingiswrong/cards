# Web (Vue 3 + Vite)

Короткая справка по фронтенду. **Общий обзор репозитория и Docker - в [README.md](../README.md) в корне проекта.**

## Разработка

Сначала поднимите бэкенд из **`../cards_simulation`** (порт **8081**):

```bash
cd ../cards_simulation
./mvnw spring-boot:run
# Windows: mvnw.cmd spring-boot:run
```

Затем в этой папке:

```bash
npm install
npm run dev
```

Откройте **http://localhost:8080**. Пути **`/api`** проксируются на `http://127.0.0.1:8081`.

## Сборка

```bash
npm run build
```

Артефакты в `dist/`; в Docker статику раздаёт nginx (см. `Dockerfile` и `nginx.conf`).
