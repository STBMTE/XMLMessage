# XMLMessage

- `XSDSchema`: XSD + XMLBeans code generation (single source of truth).
- `XMLServer`: Spring Boot XML server using classes from `XSDSchema`.
- `XMLClient`: Spring Boot CLI client using classes from `XSDSchema`.

## Build

```
.\gradlew clean build
```

## PostgreSQL container up

Start PostgreSQL:

```
docker-compose up -d postgres
```

## Run Server

```
.\gradlew :XMLServer:bootRun
```

Server endpoint: `POST /api/xml/message` (`application/xml`).

## Run Client

```
.\gradlew :XMLClient:bootRun
```