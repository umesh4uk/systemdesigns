# E-Commerce Platform

A production-ready, modular-monolith e-commerce backend built with **Java 21 + Spring Boot 3.3**
and a **React 18 + TypeScript** frontend.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        ecommerce-app                                │
│  (assembles all modules; Spring Boot entry point + global config)   │
├───────────┬───────────┬───────────┬───────────┬───────────┬─────────┤
│ identity  │  catalog  │ inventory │  pricing  │   cart    │  order  │
│           │           │           │           │           │         │
│ payment   │ promotion │notification│           │           │         │
├───────────┴───────────┴───────────┴───────────┴───────────┴─────────┤
│                     ecommerce-shared-kernel                         │
│  (value objects, base entities, domain events, exceptions, DTOs)    │
└─────────────────────────────────────────────────────────────────────┘
```

Each module is a **bounded context** with its own domain model, repository ports,
application services, and REST controllers. Modules communicate via **Spring
`ApplicationEventPublisher`** (in-process) and **Kafka** (async integration events).

---

## Technology Stack

| Concern | Technology |
|---|---|
| Language | Java 21 (records, pattern matching, virtual threads ready) |
| Framework | Spring Boot 3.3 |
| Security | Spring Security + stateless JWT (jjwt 0.12) |
| Persistence | PostgreSQL 16 + Spring Data JPA + Hibernate 6 |
| Schema migrations | Flyway |
| Caching / Cart | Redis 7 (Spring Data Redis + Lettuce) |
| Async events | Apache Kafka 3 |
| API docs | SpringDoc OpenAPI 3 / Swagger UI |
| Observability | Spring Actuator + Micrometer + Prometheus + Grafana |
| Frontend | React 18 + TypeScript + Vite + TailwindCSS |
| State | Zustand + TanStack Query |
| Forms | React Hook Form + Zod |
| Containerisation | Docker (multi-stage) + Docker Compose |
| Orchestration | Kubernetes + Kustomize |
| CI/CD | GitHub Actions |

---

## Quick Start (Docker Compose)

### Prerequisites
- Docker 24+ and Docker Compose v2
- Java 21 (only if running the backend outside Docker)
- Node 20 (only if running the frontend outside Docker)

### 1 — Clone and configure

```bash
git clone https://github.com/YOUR_ORG/ecommerce-platform.git
cd ecommerce-platform
cp .env.example .env
# Edit .env and set a strong JWT_SECRET
```

### 2 — Start the full stack

```bash
docker compose up -d
```

This starts PostgreSQL, Redis, Kafka, MailPit, Prometheus, Grafana,
the API backend, and the React frontend.

| Service | URL |
|---|---|
| Frontend | http://localhost:3000 |
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Kafka UI | http://localhost:8090 |
| MailPit (email) | http://localhost:8025 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3001 (admin/admin) |

### 3 — Default admin account

| Field | Value |
|---|---|
| Email | `admin@ecommerce.example.com` |
| Password | `Admin@123` |

---

## Running the Backend Locally (without Docker)

Ensure PostgreSQL and Redis are running, then:

```bash
# Set environment variables (or export from .env)
export SPRING_PROFILES_ACTIVE=dev
export DB_URL=jdbc:postgresql://localhost:5432/ecommerce
export DB_USERNAME=ecommerce
export DB_PASSWORD=ecommerce
export REDIS_HOST=localhost
export JWT_SECRET=<your-base64-secret>

# Build and run
./mvnw spring-boot:run -pl ecommerce-app -am
```

Flyway migrations run automatically on startup.

---

## Running the Frontend Locally

```bash
cd frontend
cp ../.env.example .env.local
# Set VITE_API_BASE_URL=http://localhost:8080
npm install
npm run dev
```

---

## Running Tests

```bash
# All unit tests (fast, no containers needed)
./mvnw test -pl ecommerce-shared-kernel,ecommerce-identity,ecommerce-catalog,ecommerce-order

# Integration tests (requires Docker for Testcontainers)
./mvnw verify -pl ecommerce-app -am

# Frontend tests
cd frontend && npm test
```

---

## API Reference

Full API documentation is available at **http://localhost:8080/swagger-ui.html** when the
backend is running.

### Key endpoints

```
POST   /api/v1/auth/register          Register a new customer
POST   /api/v1/auth/login             Login and receive JWT tokens
POST   /api/v1/auth/refresh           Refresh access token

GET    /api/v1/products               Search / filter products
GET    /api/v1/products/{id}          Product detail
GET    /api/v1/categories             Category tree

GET    /api/v1/cart                   View cart
POST   /api/v1/cart/items             Add item to cart
PUT    /api/v1/cart/items/{productId} Update quantity
DELETE /api/v1/cart/items/{productId} Remove item
POST   /api/v1/cart/coupon            Apply coupon

POST   /api/v1/orders                 Place order (from cart)
GET    /api/v1/orders                 Order history
GET    /api/v1/orders/{id}            Order detail
POST   /api/v1/orders/{id}/cancel     Cancel eligible order
POST   /api/v1/orders/{id}/return     Request return

POST   /api/v1/payments               Initiate payment
GET    /api/v1/payments/{id}          Payment status

GET    /api/v1/customers/me           My profile
PUT    /api/v1/customers/me           Update profile
GET    /api/v1/customers/me/addresses List addresses
POST   /api/v1/customers/me/addresses Add address

# Admin endpoints (require ROLE_ADMIN)
POST   /api/v1/admin/products         Create product
PUT    /api/v1/admin/products/{id}    Update product
PATCH  /api/v1/admin/orders/{id}/status  Update order status
GET    /api/v1/admin/dashboard/summary   Dashboard metrics
```

---

## Project Structure

```
ecommerce-platform/
├── ecommerce-shared-kernel/    # Value objects, base classes, domain events
├── ecommerce-identity/         # Customer auth + profiles + JWT
├── ecommerce-catalog/          # Products + hierarchical categories
├── ecommerce-inventory/        # Stock management, reservations
├── ecommerce-pricing/          # Price rules, discount computation
├── ecommerce-cart/             # Redis-backed shopping cart
├── ecommerce-order/            # Order lifecycle state machine
├── ecommerce-payment/          # Payment + refund (provider-agnostic)
├── ecommerce-promotion/        # Coupons + discount codes
├── ecommerce-notification/     # Email dispatch via domain event listeners
├── ecommerce-app/              # Spring Boot entry point + global config
├── frontend/                   # React 18 + TypeScript SPA
├── k8s/                        # Kubernetes manifests (Kustomize)
│   ├── base/                   # Base resources
│   └── overlays/               # staging / production patches
├── infra/                      # Prometheus + Grafana config
├── .github/workflows/          # CI (ci.yml) + CD (cd.yml) + PR checks
├── docker-compose.yml          # Full local dev stack
├── Dockerfile                  # Multi-stage backend image
└── .env.example                # Environment variable template
```

---

## Design Decisions

- **Modular monolith first** — bounded contexts share a single JVM and DB but are
  structured so any module can be extracted into a microservice without logic changes.
  Inter-module calls go through application service interfaces, never directly between
  repositories.
- **Domain events** — aggregates register events; the application layer publishes them
  after successful persistence. `@Async` listeners in the Notification module keep
  email dispatch non-blocking.
- **No shared DB access between contexts** — each context owns its own tables.
  Cross-context reads use narrow query interfaces or published events, not shared JPA
  entities.
- **Stateless JWT** — refresh tokens are opaque Bearer tokens; access tokens are 15 min,
  refresh tokens 7 days. Token revocation can be layered onto the Redis deny-list pattern.
- **Idempotent payments** — `idempotencyKey` on the Payment entity prevents double-charging
  on network retries.
- **Oversell prevention** — `InventoryItem.reserve()` is transactional with optimistic
  locking. Available quantity is decremented at reservation time, not at shipment.

---

## Deployment

See [k8s/README.md](k8s/README.md) (generated on first deploy) or run:

```bash
# Staging
kubectl apply -k k8s/overlays/staging

# Production
kubectl apply -k k8s/overlays/production
```

The CD pipeline (`.github/workflows/cd.yml`) handles image builds and deploys
automatically on `main` merges and `v*.*.*` tags.

---

## License

Proprietary. All rights reserved.
