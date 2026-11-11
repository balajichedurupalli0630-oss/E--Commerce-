# 🛒 OmniCart — Modern Full-Stack E-Commerce Platform

A production-grade, distributed e-commerce platform featuring a high-performance **JavaFX 21 Dual-Portal Desktop Frontend**, an event-driven **Java 21 / Spring Boot 3.4.3 Microservices Backend**, and optional **Next.js 15 Web Frontends**.

---

## ⚡ Quick Launch (JavaFX Desktop Frontend)

Launch the desktop client immediately with zero configuration:

```bash
./run-desktop.sh
```

*(Or run via Maven directly:)*
```bash
cd javafx-frontend
mvn javafx:run
```

> [!NOTE]
> **Standalone / Offline Mode**: The JavaFX client includes built-in offline product catalogs, local mock orders (`data/orders.json`), and image resources. You can run and explore the entire UI immediately without starting Docker or backend services. When backend microservices are running, it seamlessly communicates with live APIs.

---

## 🏗️ Architecture & Technology Stack

```
OmniCart/
│
├── javafx-frontend/          # 🌟 Primary Desktop GUI (Java 21, JavaFX 21, FXML, CSS)
│   ├── src/main/java/        # MVC Controllers, API Clients, Reactive Cart Store
│   ├── src/main/resources/   # FXML Views, CSS Themes, Product & User Images
│   └── data/orders.json      # Local offline persistence
│
├── backend/                  # ⚙️ Java 21 / Spring Boot 3.4.3 Event-Driven Microservices
│   ├── common-dto/           # Shared Java Records & Kafka Event Contracts
│   ├── product-service/      # PostgreSQL 16 + JPA + Kafka (Port 8000)
│   ├── order-service/        # MongoDB 7 + Reactive/Sync + Kafka (Port 8001)
│   ├── payment-service/      # Stripe SDK + Kafka (Port 8002)
│   ├── auth-service/         # Clerk API & Local Auth + Kafka (Port 8003)
│   ├── email-service/        # Spring Mail + Kafka Event Worker
│   ├── docker-compose.yml    # PostgreSQL, MongoDB, Kafka (KRaft), Kafka UI
│   ├── start-services.sh     # Infrastructure starter (Docker)
│   └── start-apps.sh         # Microservice jar launcher
│
├── apps/                     # 🌐 Web Frontends (Optional / Next.js 15)
│   ├── client/               # Customer Storefront (Port 3002)
│   └── admin/                # Admin Dashboard (Port 3003)
│
└── run-desktop.sh            # One-click desktop platform launcher
```

---

## 🔌 Microservices & Ports Reference

| Service / App | Port | Technology | Purpose |
| :--- | :--- | :--- | :--- |
| **JavaFX Desktop Client** | *Native GUI* | Java 21, JavaFX 21, FXML | Dual-portal desktop storefront & admin dashboard |
| **Product Service** | `:8000` | Spring Boot 3, PostgreSQL 16 | Product catalog, auto-seeding, search, category filters |
| **Order Service** | `:8001` | Spring Boot 3, MongoDB 7 | Order lifecycle, order cancellation, status tracking |
| **Payment Service** | `:8002` | Spring Boot 3, Stripe SDK | Stripe checkout sessions & webhook processing |
| **Auth Service** | `:8003` | Spring Boot 3, Clerk | User authentication, token validation, user lists |
| **Email Service** | *Worker* | Spring Boot 3, Spring Mail | Asynchronous Kafka event consumer for order notifications |
| **Kafka UI** | `:8080` | Provectus Kafka-UI | Web console for inspecting Kafka topics and events |
| **Next.js Storefront** | `:3002` | Next.js 15, React 19, Tailwind | Web customer storefront (alternative web client) |
| **Next.js Admin** | `:3003` | Next.js 15, TanStack Table | Web administration portal (alternative web client) |

---

## 🚀 Full-Stack Setup (Live Microservices)

To run the complete platform with live databases, Kafka, and microservices:

### Prerequisites
- **Java JDK 21** (`java -version`)
- **Maven 3.9+** (or use `./mvnw`)
- **Docker Desktop** (running)
- **Node.js 18+ & pnpm** *(only if running Next.js web frontends)*

---

### Step 1: Start Databases & Kafka (Docker)

Make sure Docker Desktop is running (`open -a Docker` on macOS), then:

```bash
cd backend
./start-services.sh
```
* PostgreSQL, MongoDB, Kafka, and Kafka UI (`http://localhost:8080`) will start up.

---

### Step 2: Build & Start All 5 Spring Boot Microservices

```bash
cd backend

# Build all microservices
./mvnw clean package -DskipTests

# Start all 5 microservices in the background
./start-apps.sh
```

To stop all services at any time:
```bash
./stop-apps.sh
```

---

### Step 3: Run the Frontends

#### Option A: JavaFX Desktop Client (Recommended)
```bash
./run-desktop.sh
```

#### Option B: Next.js Web Frontends (Optional)
```bash
# Install monorepo dependencies
pnpm install

# Run both web apps concurrently
pnpm dev
```
- Storefront: [http://localhost:3002](http://localhost:3002)
- Admin Portal: [http://localhost:3003](http://localhost:3003)

---

## 🧪 Testing

Run automated unit and integration smoke tests for the JavaFX client:

```bash
cd javafx-frontend
mvn test
```

Run tests across all backend microservices:
```bash
cd backend
./mvnw test
```

---

## 🛠️ Common Troubleshooting

### 1. `Cannot connect to the Docker daemon`
* **Cause**: Docker Desktop is not running.
* **Fix**: Run `open -a Docker` on macOS and wait ~20 seconds until the Docker engine starts.

### 2. `Address already in use :::3002 / :::3003`
* **Cause**: A previous Next.js instance is already running.
* **Fix**: Find and kill the process:
  ```bash
  kill -9 $(lsof -ti :3002 -ti :3003)
  ```

### 3. `Permission denied: ./mvnw` or `./run-desktop.sh`
* **Fix**: Grant execute permissions:
  ```bash
  chmod +x ./run-desktop.sh javafx-frontend/mvnw backend/mvnw
  ```
