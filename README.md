# 🛒 OmniCart — Modern Distributed E-Commerce Platform

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-green.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft-red.svg)](https://kafka.apache.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-purple.svg)](https://openjfx.io/)
[![Next.js](https://img.shields.io/badge/Next.js-15-black.svg)](https://nextjs.org/)

A production-grade, event-driven distributed e-commerce ecosystem. **OmniCart** pairs a high-performance **JavaFX 21 Dual-Portal Desktop Workstation** with a resilient **Spring Boot 3.4.3 Microservices Backend**, an **Apache Kafka** event fabric, domain-isolated **PostgreSQL 16** databases, and dual **Next.js 15** web applications.

---

## 🌟 Key Architectural Features

- **🚀 Domain-Isolated PostgreSQL Microservices:** Independent databases per service (`productdb`, `orderdb`, `authdb`) on PostgreSQL 16, replacing legacy MongoDB for full ACID order guarantees.
- **⚡ Native Authentication & Security:** Custom Spring Security architecture featuring BCrypt password hashing and stateless JWT bearer tokens — zero external SaaS dependency.
- **💳 Native Mock Payment & Kafka Fulfillment:** Purged third-party Stripe bloat; transactions publish `payment.successful` directly to Kafka for sub-second asynchronous order creation.
- **👟 Dynamic Footwear & Apparel Retail Sizing:** Category-aware catalog management dynamically switching between shoe sizes (`UK 5` to `UK 12`) and apparel sizes (`S` to `XXL`).
- **📊 Bayesian Stochastic Inventory Engine:** Integrated Gamma-Poisson conjugate posterior modeling in `product-service` providing real-time demand forecasting, safety stock reorder thresholds, and margin optimization.
- **☕ Modern Java 21 Architecture:** Built on immutable Java `record` classes, switch expressions, and virtual-thread-compatible non-blocking HTTP APIs.

---

## ⚡ Quick Launch: JavaFX Desktop Client

Launch the desktop client immediately (runs in standalone mode with embedded offline catalog and mock data if backend services are offline):

```bash
./run-desktop.sh
```

*(Or via Maven directly:)*
```bash
cd javafx-frontend
./mvnw javafx:run
```

---

## 🏗️ Architecture & Repository Structure

```
OmniCart/
│
├── javafx-frontend/          # 🌟 Primary Desktop Executive GUI (JavaFX 21, FXML, CSS)
│   ├── src/main/java/        # MVC Controllers, Non-blocking API Clients, Reactive Store
│   ├── src/main/resources/   # FXML Layouts, CSS Themes, Asset Icons
│   └── data/                 # Offline seed state (orders.json, users.json)
│
├── backend/                  # ⚙️ Java 21 / Spring Boot 3.4.3 Microservices
│   ├── common-dto/           # Immutable Java 21 Records & Kafka Event Contracts
│   ├── product-service/      # PostgreSQL (productdb) + Stochastic Demand Engine (:8000)
│   ├── order-service/        # PostgreSQL (orderdb) + JPA Order Processor (:8001)
│   ├── payment-service/      # Native Mock Payment & Kafka Dispatcher (:8002)
│   ├── auth-service/         # PostgreSQL (authdb) + BCrypt + JWT Auth (:8003)
│   ├── email-service/        # Background Kafka Worker (Order & Welcome Dispatcher)
│   ├── docker-compose.yml    # PostgreSQL 16, Apache Kafka, Kafka-UI (:8080)
│   ├── start-services.sh     # Docker infrastructure launcher
│   └── start-apps.sh         # Microservice JAR process supervisor
│
├── apps/                     # 🌐 Next.js 15 Web Applications (Monorepo)
│   ├── client/               # B2C Customer Storefront (:3002)
│   └── admin/                # B2B Admin Management Web Portal (:3003)
│
├── PROJECT_REPORT.md         # 📄 Comprehensive Technical Academic Report (Markdown)
├── PROJECT_REPORT.html       # 🖨️ Interactive PDF-Ready Technical Report (Browser Print)
└── run-desktop.sh            # 1-Click Desktop Platform Launcher
```

---

## 🔌 Microservices & Infrastructure Ports Reference

| Service / Component | Port | Persistence | Key Responsibilities |
| :--- | :--- | :--- | :--- |
| **`product-service`** | `:8000` | PostgreSQL (`productdb`) | Product CRUD, category trees, dynamic filters, Bayesian demand forecasting |
| **`order-service`** | `:8001` | PostgreSQL (`orderdb`) | JPA order persistence, JSON line-item conversion, sales velocity analytics, order cancellation |
| **`payment-service`** | `:8002` | Stateless / In-Memory | Native mock checkout session creation, status query, Kafka `payment.successful` dispatch |
| **`auth-service`** | `:8003` | PostgreSQL (`authdb`) | BCrypt user registration, JWT issuance (`/auth/login`), profile verification (`/auth/me`) |
| **`email-service`** | *Worker* | - | Kafka consumer for `order.created` and `user.created`, email dispatching |
| **`ecom-postgres`** | `:5432` | Docker Volume | Polyglot multi-database PostgreSQL 16 container (`productdb`, `orderdb`, `authdb`) |
| **`ecom-kafka`** | `:9094` | KRaft Volume | High-throughput distributed event broker |
| **`ecom-kafka-ui`** | `:8080` | Web Console | Live monitoring of topics, partitions, consumers, and message payloads |
| **JavaFX Desktop App** | *Native* | Desktop Local Cache | Multi-portal native GUI (Customer Storefront + Admin Executive Station) |
| **Next.js Storefront** | `:3002` | LocalStorage / Cookies | Web customer shopping portal |
| **Next.js Admin** | `:3003` | LocalStorage / Cookies | Web administrative management portal |

---

## 🚀 Full-Stack Setup Guide (Live Microservices)

### Prerequisites
- **JDK 21 LTS** (`java -version`)
- **Docker Desktop** (running)
- **Node.js 18+ & pnpm** *(only if running optional Next.js web frontends)*

---

### Step 1: Start Database & Messaging Containers

Ensure Docker Desktop is active, then launch PostgreSQL and Kafka:

```bash
cd backend
./start-services.sh
```
*Verify containers:*
- Kafka-UI: [http://localhost:8080](http://localhost:8080)
- PostgreSQL: `localhost:5432` (User: `postgres`, Password: `postgres`)

---

### Step 2: Build & Start Spring Boot Microservices

Compile all 7 backend modules with Java 21:

```bash
cd backend
./mvnw clean package -DskipTests
```

Start all microservices in the background:
```bash
./start-apps.sh
```

*To stop all running microservices:*
```bash
./stop-apps.sh
```

---

### Step 3: Launch Frontends

#### Primary: JavaFX Desktop Workstation (Recommended)
```bash
./run-desktop.sh
```

#### Secondary: Next.js Web Frontends (Optional)
```bash
pnpm install
pnpm dev
```
- Customer Storefront: [http://localhost:3002](http://localhost:3002)
- Admin Portal: [http://localhost:3003](http://localhost:3003)

---

## 🔑 Pre-Seeded Default Accounts

| Username | Password | Role | Description |
| :--- | :--- | :--- | :--- |
| **`admin`** | `admin123` | `admin` | Full administrator with catalog, pricing, user, and order controls |
| **`johndoe`** | `password123` | `user` | Standard buyer account with order history |
| **`priya`** | `password123` | `user` | Standard buyer account with completed purchases |

---

## 🧪 Testing & Verification

Run automated test suites across all backend services:
```bash
cd backend
./mvnw test
```

Run JavaFX desktop integration tests:
```bash
cd javafx-frontend
./mvnw test
```

Verify end-to-end native mock checkout via cURL:
```bash
curl -X POST http://localhost:8002/sessions/mock-checkout \
  -H "Content-Type: application/json" \
  -H "x-user-id: usr_test_01" \
  -d '{"cart":[{"id":1,"name":"Nike Air Max Pulse","price":120,"quantity":1}]}'
```
*Result:* Returns HTTP 200, triggers Kafka event, creates order in PostgreSQL `orderdb.orders`, and logs simulated customer dispatch in `email-service`.

---

## 📄 Project Documentation & Technical Report

A technical report formatted according to formal academic guidelines is available in the root directory:
- **Markdown Document:** [PROJECT_REPORT.md](file:///Users/chinnu/Ecom/PROJECT_REPORT.md)
- **PDF-Ready HTML:** [PROJECT_REPORT.html](file:///Users/chinnu/Ecom/PROJECT_REPORT.html) (Open in Chrome/Safari, press `Cmd + P` -> "Save as PDF")
