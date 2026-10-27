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

## Prerequisites

Before running the project, make sure you have installed:
- [Docker Desktop](https://www.docker.com/) (running)
- [Node.js](https://nodejs.org/) (v18+) & [pnpm](https://pnpm.io/) (`npm install -g pnpm`)
- [Java JDK](https://adoptium.net/) (v17 or v21)

---

## Quick Start (Step-by-Step for New Clones)

### Step 1: Install Frontend Dependencies
```bash
# In the root repository folder
pnpm install
```
*(Note: Docker runs PostgreSQL, MongoDB, and Kafka, so `pnpm install` is required for the Next.js apps).*

### Step 2: Configure Environment Variables
Copy the sample environment files:
```bash
cp apps/client/.env.example apps/client/.env.local
cp apps/admin/.env.example apps/admin/.env.local
```
*(Add your Clerk authentication keys in `.env.local` if you wish to test user sign-in).*

### Step 3: Start Databases & Kafka (Docker)
```bash
cd backend
./start-services.sh
# Kafka UI will be available at: http://localhost:8080
```

### Step 4: Build & Launch All 5 Java Microservices
```bash
cd backend
# 1. Build all services (Maven will automatically download dependencies)
./mvnw clean package -DskipTests

# 2. Start all 5 microservices in the background (Product, Order, Payment, Auth, Email)
./start-apps.sh

# To stop all services at any time:
# ./stop-apps.sh
```

### Step 5: Start the Frontends
From the project root:
```bash
# Terminal 1: Client Storefront (http://localhost:3002)
pnpm --filter client dev

# Terminal 2: Admin Dashboard (http://localhost:3003)
pnpm --filter admin dev
```
