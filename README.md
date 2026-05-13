# Castellanos Post-2 U12 — Pipeline CI/CD con GitHub Actions y Docker Hub

![CI/CD Status](https://github.com/KeiverJ/castellanos-post2-u12/actions/workflows/ci.yml/badge.svg)

## Descripcion

Aplicacion Spring Boot que expone una API REST de productos, contenedorizada con Docker y desplegada automaticamente mediante un pipeline CI/CD en GitHub Actions.

Este laboratorio implementa la automatizacion completa del ciclo de vida del software: compilacion, ejecucion de pruebas unitarias con reporte de cobertura JaCoCo, construccion de imagen Docker con multi-stage build y publicacion en Docker Hub.

---

## Pipeline CI/CD

El pipeline se activa automaticamente en cada push a `main` y realiza:

1. **Compilacion** con Maven y ejecucion de pruebas unitarias/integracion
2. **Generacion** de reporte de cobertura JaCoCo (artefacto descargable)
3. **Construccion** de imagen Docker con multi-stage build
4. **Publicacion** de la imagen en Docker Hub con tags `latest` y `sha-<commit>`

### Diagrama del Pipeline

```
push a main
    |
    v
+-------------------+     +---------------------+
| build-and-test    | --> | docker-publish       |
| (CI)              |     | (CD)                 |
|                   |     |                      |
| - Checkout        |     | - Checkout           |
| - JDK 21 + cache  |     | - Login Docker Hub   |
| - mvn clean verify|     | - Extraer metadata   |
| - Upload JaCoCo   |     | - Build & push image |
+-------------------+     +---------------------+
```

### Jobs del Workflow

| Job | Proposito | Trigger |
|-----|-----------|---------|
| `build-and-test` | Compila con Maven, ejecuta pruebas y genera reporte JaCoCo | Push y PR a `main` |
| `docker-publish` | Construye imagen Docker multi-stage y publica en Docker Hub | Solo push a `main` |

El job `docker-publish` depende del exito de `build-and-test` mediante `needs: build-and-test`. Ademas, solo se ejecuta en push directo a `main` (no en pull requests) gracias a la condicion:

```yaml
if: github.ref == 'refs/heads/main' && github.event_name == 'push'
```

---

## GitHub Secrets Requeridos

Para que el pipeline funcione, se deben configurar los siguientes Secrets en el repositorio:

**Ruta:** Settings > Secrets and variables > Actions > New repository secret

| Secret | Descripcion | Ejemplo |
|--------|-------------|---------|
| `DOCKERHUB_USERNAME` | Nombre de usuario de Docker Hub | `keiverj` |
| `DOCKERHUB_TOKEN` | Access Token de Docker Hub (no la contrasena) | `dckr_pat_xxxxx` |

> **Nota:** El Access Token se genera en [Docker Hub > Account Settings > Security > New Access Token](https://hub.docker.com/settings/security) con permisos de lectura y escritura.

---

## Tecnologias y Versiones

| Tecnologia | Version |
|------------|---------|
| Java (JDK) | 21 (Temurin) |
| Spring Boot | 3.4.5 |
| Maven | 3.9+ |
| JaCoCo | 0.8.13 |
| Docker | Multi-stage build |
| H2 Database | Runtime (perfil dev) |
| PostgreSQL | 16 (perfil prod) |
| GitHub Actions | v4 (checkout, setup-java, upload-artifact) |

---

## Estructura del Proyecto

```
castellanos-post2-u12/
├── .github/
│   └── workflows/
│       └── ci.yml                  # Workflow CI/CD
├── src/
│   ├── main/
│   │   ├── java/.../post2u12/
│   │   │   ├── config/             # Data seeder
│   │   │   ├── domain/             # Entidad JPA
│   │   │   ├── repository/         # Repositorio Spring Data
│   │   │   ├── service/            # Logica de negocio
│   │   │   └── web/                # Controladores REST + DTOs + Excepciones
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       └── data.sql
│   └── test/
│       └── java/.../post2u12/
│           └── Post2U12ApplicationTests.java
├── Dockerfile                      # Multi-stage build
├── docker-compose.yml              # Entorno local con PostgreSQL
├── pom.xml                         # Dependencias + JaCoCo
├── capturas/                       # Evidencia visual
└── README.md
```

---

## Prerequisitos

- **Git** instalado y configurado
- **Java 21** (JDK Temurin)
- **Maven 3.9+**
- **Docker** (opcional, para ejecucion local con contenedores)
- Cuenta en [Docker Hub](https://hub.docker.com) con Access Token

---

## Ejecucion Local

### Sin Docker (perfil dev con H2)

```bash
mvn clean verify
mvn spring-boot:run
```

La aplicacion estara disponible en `http://localhost:8080`

### Con Docker Compose (perfil prod con PostgreSQL)

```bash
docker compose up --build
```

### Desde Docker Hub

```bash
docker pull keiverj/castellanos-post2-u12:latest
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=dev keiverj/castellanos-post2-u12:latest
```

---

## Endpoints Principales

| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| `GET` | `/` | Pagina de bienvenida |
| `GET` | `/actuator/health` | Estado de salud de la aplicacion |
| `GET` | `/api/productos` | Listar todos los productos |
| `GET` | `/api/productos/{id}` | Obtener producto por ID |
| `POST` | `/api/productos` | Crear nuevo producto |
| `PUT` | `/api/productos/{id}` | Actualizar producto existente |
| `DELETE` | `/api/productos/{id}` | Eliminar producto |

---

## Pruebas Ejecutadas

La suite de pruebas incluye **9 tests de integracion** que cubren:

| Prueba | Descripcion | Estado |
|--------|-------------|--------|
| `contextLoads` | Verifica que el contexto Spring se carga correctamente | ✅ |
| `debeResponderPaginaRaiz` | Valida respuesta del endpoint raiz | ✅ |
| `debeResponderHealthEnEstadoUp` | Comprueba que el actuator reporta UP | ✅ |
| `debeListarProductosSembrados` | Verifica listado de productos sembrados | ✅ |
| `debeObtenerProductoPorId` | Obtiene un producto existente por ID | ✅ |
| `debeCrearUnProductoNuevo` | Crea un producto y valida respuesta 201 | ✅ |
| `debeActualizarUnProductoExistente` | Actualiza un producto y valida cambios | ✅ |
| `debeEliminarUnProductoExistente` | Elimina un producto y valida respuesta 204 | ✅ |
| `debeRetornar404CuandoProductoNoExiste` | Valida respuesta 404 para ID inexistente | ✅ |
| `debeRetornar400CuandoDatosInvalidos` | Valida respuesta 400 para datos invalidos | ✅ |

El reporte de cobertura JaCoCo se genera automaticamente con `mvn clean verify` y queda disponible como artefacto descargable en la pestana Actions de GitHub.

---

## Imagen Docker

```bash
docker pull keiverj/castellanos-post2-u12:latest
```

La imagen se publica automaticamente con dos tags:
- `latest` — siempre apunta al ultimo build exitoso
- `sha-<commit>` — tag inmutable vinculado al commit especifico

---

## Decisiones Tecnicas

| Decision | Justificacion |
|----------|---------------|
| **Multi-stage build** | Reduce el tamano de la imagen final (solo JRE, sin Maven ni fuentes) |
| **JaCoCo en fase verify** | Genera el reporte de cobertura despues de ejecutar las pruebas |
| **Cache de Maven** | Configurado con `actions/setup-java cache: maven` para acelerar builds |
| **Condicion de rama para CD** | `docker-publish` solo en push a main, evita publicaciones desde PRs |
| **Tags semanticos** | `latest` + `sha-<commit>` para trazabilidad de versiones |
| **GitHub Secrets** | Las credenciales nunca se exponen en el YAML del workflow |
| **H2 en CI** | El perfil `dev` usa H2 en memoria, eliminando dependencia de BD externa en el runner |

---

## Soluciones a Problemas Frecuentes

| Problema | Solucion |
|----------|----------|
| Pipeline falla en `docker-publish` | Verificar que los Secrets `DOCKERHUB_USERNAME` y `DOCKERHUB_TOKEN` estan configurados correctamente |
| Token de Docker Hub rechazado | Generar un nuevo Access Token en Docker Hub con permisos Read & Write |
| Pruebas fallan en CI | El perfil `dev` debe estar activo (usa H2 en memoria, no requiere BD externa) |
| Imagen Docker muy grande | El multi-stage build ya optimiza esto usando `eclipse-temurin:21-jre-alpine` |
| Cache de Maven no funciona | Verificar que `actions/setup-java` tiene `cache: maven` configurado |