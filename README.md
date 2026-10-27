# E-Commerce Microservices Platform

A modern, full-stack e-commerce platform combining **Next.js 15 frontends** with a high-performance **Java 21 / Spring Boot 3.4.3 event-driven microservices backend**.

---

## Repository Structure

```
├── apps/
│   ├── client/               # Storefront UI (Next.js 15, React 19, Tailwind v4, Stripe Elements) - Port 3002
│   └── admin/                # Admin Dashboard (Next.js 15, TanStack Table/Query, Recharts) - Port 3003
│
├── backend/                  # Java 21 / Spring Boot 3.4.3 Multi-Module Microservices
│   ├── pom.xml               # Parent POM with Maven Wrapper (mvnw)
│   ├── docker-compose.yml    # PostgreSQL 16, MongoDB 7, Kafka KRaft, Kafka UI
│   ├── start-services.sh     # Infrastructure & service launcher
│   ├── common-dto/           # Shared Java DTOs and Kafka Event Contracts
│   ├── product-service/      # Spring Boot 3 + PostgreSQL/JPA + Kafka (Port 8000)
│   ├── order-service/        # Spring Boot 3 + MongoDB + Kafka (Port 8001)
│   ├── payment-service/      # Spring Boot 3 + Stripe SDK + Kafka (Port 8002)
│   ├── auth-service/         # Spring Boot 3 + Clerk API / Local Fallback + Kafka (Port 8003)
│   └── email-service/        # Spring Boot 3 + Spring Mail + Kafka (Event Worker)
│
└── packages/
    ├── types/                # Shared TypeScript types for Next.js frontends
    ├── eslint-config/        # Monorepo ESLint presets
    └── typescript-config/    # Monorepo TSConfig presets
```

---

## Quick Start

### 1. Start Infrastructure (PostgreSQL, MongoDB, Kafka)
```bash
cd backend
./start-services.sh
# Kafka UI: http://localhost:8080
```

### 2. Run the Java Microservices
```bash
cd backend
# Build all services
./mvnw clean package -DskipTests

# Run any service (e.g. Product Service on 8000)
cd product-service && ../mvnw spring-boot:run
```

### 3. Run the Frontend Applications
```bash
# Run Client Storefront (Port 3002)
pnpm --filter client dev

# Run Admin Dashboard (Port 3003)
pnpm --filter admin dev
```
