# ⚙️ OmniCart Backend Microservices

A resilient, event-driven microservices backend built with **Java 21 LTS**, **Spring Boot 3.4.3**, **PostgreSQL 16**, and **Apache Kafka**.

---

## 🏛️ Microservices Breakdown

| Service | Port | Database | Key Responsibilities |
| :--- | :--- | :--- | :--- |
| **`common-dto`** | Library | - | Immutable Java 21 `record` classes (`ProductDTO`, `OrderItemDTO`, `UserDTO`) and Kafka event contracts |
| **`product-service`** | `http://localhost:8000` | PostgreSQL (`productdb`) | Product & Category CRUD, dynamic shoe/clothing size associations, Bayesian Gamma-Poisson stochastic demand forecasting |
| **`order-service`** | `http://localhost:8001` | PostgreSQL (`orderdb`) | JPA order repository, normalized `orders` & `order_items` (3NF), Kafka consumer for `payment.successful`, order cancellation |
| **`payment-service`** | `http://localhost:8002` | In-Memory / Stateless | Native mock checkout session creation, status retrieval, Kafka publisher for `payment.successful` (Zero Stripe dependency) |
| **`auth-service`** | `http://localhost:8003` | PostgreSQL (`authdb`) | Native BCrypt password hashing, stateless JWT issuance (`/auth/login`), registration (`/auth/register`), profile validation (`/auth/me`) |
| **`email-service`** | Worker | - | Asynchronous Kafka consumer for `order.created` and `user.created`, order receipt & welcome dispatching |

---

## 🚀 Getting Started

### 1. Start Infrastructure (Docker)

Start PostgreSQL, Kafka, and Kafka UI in the background:

```bash
cd backend
./start-services.sh
```

- **Kafka UI Console:** [http://localhost:8080](http://localhost:8080)
- **PostgreSQL 16:** `localhost:5432` (Username: `postgres`, Password: `postgres`)
  - Databases initialized: `productdb`, `orderdb`, `authdb`

---

### 2. Build All Services

Compile and package all microservices using the Maven wrapper:

```bash
cd backend
./mvnw clean package -DskipTests
```

---

### 3. Launch Microservices

Start all 5 microservices as background processes:

```bash
./start-apps.sh
```

To stop all services:
```bash
./stop-apps.sh
```

To inspect logs in real time:
```bash
tail -f logs/product-service.log
tail -f logs/order-service.log
tail -f logs/payment-service.log
tail -f logs/auth-service.log
tail -f logs/email-service.log
```

---

## 📨 Kafka Event Topics

| Topic | Producer | Consumer | Payload |
| :--- | :--- | :--- | :--- |
| **`payment.successful`** | `payment-service` | `order-service` | `PaymentSuccessfulEvent` (userId, email, amount, products) |
| **`order.created`** | `order-service` | `email-service` | `OrderCreatedEvent` (orderId, email, amount, status) |
| **`user.created`** | `auth-service` | `email-service` | `UserCreatedEvent` (userId, username, email) |
| **`product.created`** | `product-service` | - | `ProductCreatedEvent` (id, name, price, category) |
| **`product.deleted`** | `product-service` | - | `ProductDeletedEvent` (id) |
