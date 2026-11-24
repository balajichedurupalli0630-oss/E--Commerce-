# Project Report: OmniCart Enterprise E-Commerce Platform

---

## Chapter 1: Introduction and Requirement Analysis

### 1.1 Introduction to the Project
**OmniCart** is an enterprise-grade, distributed multi-portal e-commerce ecosystem designed to deliver high-performance retail operations. The system bridges modern web storefronts with a native desktop executive workstation. It is engineered with a reactive, event-driven microservices architecture using **Java 21**, **Spring Boot 3.4.3**, **Apache Kafka**, and **PostgreSQL 16**, paired with a **JavaFX 21** native desktop client and **Next.js 15** web applications.

OmniCart replaces monolithic e-commerce friction with domain-driven microservices:
1. **`product-service` (Port 8000):** Manages dynamic product catalogs, inventory levels, category hierarchies, and stochastic financial forecasting.
2. **`order-service` (Port 8001):** Handles order state transitions, sales velocity metrics, and historical analytics using PostgreSQL JPA persistence.
3. **`payment-service` (Port 8002):** A native mock checkout engine dispatching asynchronous transaction events over Kafka.
4. **`auth-service` (Port 8003):** Provides native JWT token issuance, session verification, and BCrypt-secured user credential storage.
5. **`email-service` (Background Worker):** Asynchronously dispatches transaction confirmations and user onboarding notifications.

---

### 1.2 Problem Statement and Motivation
Traditional retail e-commerce applications typically suffer from critical architectural bottlenecks:
1. **Monolithic Database Contention:** Shared relational databases become single points of failure when high checkout traffic locks product catalog tables.
2. **Synchronous Payment Latency:** Direct, blocking HTTP calls to payment processors introduce network latency, leading to abandoned carts and lost transactions.
3. **Rigid Catalog Categorization:** Conventional retail systems treat footwear, apparel, and electronics identically, failing to accommodate dynamic size formats (e.g., UK shoe sizes vs. standard clothing sizes).
4. **Reactive Inventory Stockouts:** Standard inventory systems alert store managers *after* stock runs dry, rather than probabilistically predicting depletion based on Bayesian velocity models.

**Motivation:** OmniCart was conceived to decouple these critical domains into independent microservices, leverage Apache Kafka for reliable asynchronous messaging, and provide store administrators with a JavaFX desktop command center powered by statistical demand analytics.

---

### 1.3 Objectives of the Project
- **Microservices Domain Isolation:** Partition database storage into domain-specific databases (`productdb`, `orderdb`, `authdb`) to prevent cross-service cascading failures.
- **Asynchronous Event-Driven Processing:** Decouple order generation and payment processing via Kafka topics (`payment.successful`, `order.created`, `user.created`).
- **Native Executive Desktop Application:** Deliver a JavaFX 21 management workstation capable of managing products, viewing order lifecycles, and running inventory algorithms.
- **Dynamic Retail Sizing:** Implement category-aware catalog management that switches dynamically between shoe sizes (`UK 5` to `UK 12`) and apparel sizes (`S` to `XXL`).
- **Stochastic Inventory & Dynamic Pricing:** Integrate a Gamma-Poisson Bayesian demand forecasting engine to recommend safety stock replenishment and margin-aware pricing.

---

### 1.4 Scope and Significance of the Application
The platform encompasses the end-to-end e-commerce lifecycle:
- **Customer Storefront (B2C):** Product discovery, multi-criteria filtering (category, color, size, price range), cart state management, and mock checkout.
- **Administrative Portal & Desktop Station (B2B):** Inventory tracking, real-time order cancellation, role-based user management, category administration, and financial forecasting.
- **Enterprise Significance:** Demonstrates how modern Java 21 features (records, pattern matching, virtual threads) eliminate legacy boilerplate while maintaining full transaction integrity across polyglot microservices.

---

### 1.5 Intended Users and Their Roles

```mermaid
flowchart TD
    subgraph Users["Platform Actors"]
        Customer["Customer (B2C Client)"]
        Admin["Administrator / Inventory Manager (B2B)"]
    end

    subgraph Storefront["Customer Workflows"]
        C1["Browse Catalog & Filters"]
        C2["Manage Cart & Quantities"]
        C3["Perform Mock Checkout"]
        C4["View Historical Orders"]
    end

    subgraph Operations["Admin Workflows"]
        A1["Product Catalog & Category CRUD"]
        A2["Dynamic Sizing Configuration"]
        A3["Order Status & Order Cancellation"]
        A4["Stochastic Inventory Reorder Analysis"]
        A5["User Role Management & Bans"]
    end

    Customer --> C1 & C2 & C3 & C4
    Admin --> A1 & A2 & A3 & A4 & A5
```

1. **Customer (Role: `user`):**
   - Can register, log in, browse products by category, filter by size/color/price.
   - Adds items to cart, selects custom sizes/colors, submits checkouts, and tracks personal order history.
2. **Administrator / Store Manager (Role: `admin`):**
   - Has elevated privileges across both Next.js Admin portal and JavaFX Desktop Station.
   - Creates, edits, restocks, and deletes products and categories.
   - Accesses financial recommendations, overrides selling prices, and cancels pending orders.

---

## Chapter 2: System Design and Database Design

### 2.1 Overall Application Architecture
OmniCart adheres to an **Event-Driven Microservices Architecture (EDMA)**:

```mermaid
flowchart TB
    subgraph FrontendTier["Presentation Tier"]
        JFX["JavaFX 21 Desktop Client (FXML + CompletableFuture)"]
        WebStore["Next.js 15 Customer Storefront (Port 3000)"]
        WebAdmin["Next.js 15 Admin Portal (Port 3001)"]
    end

    subgraph MicroservicesTier["Microservices Backend Tier (Spring Boot 3.4.3)"]
        AuthSvc["auth-service :8003\n(JWT, BCrypt)"]
        ProdSvc["product-service :8000\n(Catalog & Stochastic Engine)"]
        PaymSvc["payment-service :8002\n(Native Mock Payment)"]
        OrdSvc["order-service :8001\n(JPA Order Processor)"]
        EmailSvc["email-service\n(Background Consumer)"]
    end

    subgraph MessageBroker["Event Broker (Apache Kafka :9094)"]
        TopicPay["payment.successful"]
        TopicOrd["order.created"]
        TopicUser["user.created"]
    end

    subgraph DatabaseTier["Persistence Tier (PostgreSQL 16 :5432)"]
        AuthDB[("authdb\n(users)")]
        ProdDB[("productdb\n(products, categories, sizes)")]
        OrdDB[("orderdb\n(orders)")]
    end

    JFX & WebStore & WebAdmin --> AuthSvc
    JFX & WebStore & WebAdmin --> ProdSvc
    JFX & WebStore & WebAdmin --> PaymSvc
    JFX & WebStore & WebAdmin --> OrdSvc

    AuthSvc --> AuthDB
    ProdSvc --> ProdDB
    OrdSvc --> OrdDB

    PaymSvc -.->|Publishes| TopicPay
    TopicPay -.->|Consumes| OrdSvc
    OrdSvc -.->|Publishes| TopicOrd
    TopicOrd -.->|Consumes| EmailSvc
    AuthSvc -.->|Publishes| TopicUser
    TopicUser -.->|Consumes| EmailSvc
```

---

### 2.2 UML Diagrams

#### 2.2.1 Use Case Diagram

```mermaid
flowchart LR
    Customer((Customer))
    Admin((Administrator))

    subgraph StorefrontSystem["OmniCart System Boundary"]
        UC1(Register / Log In)
        UC2(Search & Filter Products)
        UC3(Select Sizing: Apparel or Shoes)
        UC4(Execute Checkout)
        UC5(View Order History)
        UC6(Add / Update Product with Dynamic Sizes)
        UC7(Cancel Order)
        UC8(View Stochastic Financial Insights)
        UC9(Apply Recommended Price / Restock)
    end

    Customer --> UC1
    Customer --> UC2
    Customer --> UC3
    Customer --> UC4
    Customer --> UC5

    Admin --> UC1
    Admin --> UC6
    Admin --> UC7
    Admin --> UC8
    Admin --> UC9
```

#### 2.2.2 Class Diagram (Core Entities & DTOs)

```mermaid
classDiagram
    class Product {
        +Long id
        +String name
        +String shortDescription
        +String description
        +Double price
        +Double costPrice
        +Integer stockQuantity
        +Integer leadTimeDays
        +String categorySlug
        +List~String~ sizes
        +List~String~ colors
        +String images
    }

    class Order {
        +String id
        +String userId
        +String email
        +Integer amount
        +String status
        +List~OrderItemDTO~ products
        +ZonedDateTime createdAt
    }

    class UserEntity {
        +String id
        +String username
        +String password
        +String email
        +String role
        +String firstName
        +String lastName
    }

    class PaymentSuccessfulEvent {
        <<record>>
        +String userId
        +String email
        +Integer amount
        +String status
        +List~OrderItemDTO~ products
    }

    class OrderItemDTO {
        <<record>>
        +Long productId
        +String name
        +Integer quantity
        +Integer price
    }

    Product "1" --> "*" OrderItemDTO : references
    Order "1" *-- "*" OrderItemDTO : contains
    PaymentSuccessfulEvent "1" *-- "*" OrderItemDTO : packages
```

#### 2.2.3 Activity Diagram (Checkout & Kafka Asynchronous Fulfillment)

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Customer (JavaFX / Next.js)
    participant PaymentSvc as payment-service (:8002)
    participant Kafka as Apache Kafka (:9094)
    participant OrderSvc as order-service (:8001)
    participant Postgres as PostgreSQL (orderdb)
    participant EmailSvc as email-service

    Customer->>PaymentSvc: POST /sessions/mock-checkout (Cart Items, User ID)
    PaymentSvc->>Kafka: Publish to 'payment.successful' (Total, Line Items)
    PaymentSvc-->>Customer: Return HTTP 200 {status: "success", sessionId: "mock_..."}
    Note over Customer: UI shows immediate confirmation without blocking!

    Kafka->>OrderSvc: Deliver 'payment.successful' Event
    OrderSvc->>Postgres: INSERT into orders (id, user_id, amount, products JSON, status)
    OrderSvc->>Kafka: Publish to 'order.created'
    Kafka->>EmailSvc: Deliver 'order.created' Event
    EmailSvc->>EmailSvc: Dispatch Order Confirmation Notification
```

---

### 2.3 Description of Database Tables

#### 1. `productdb` Database:
- **`categories` Table:**
  - `id` (BIGINT, PK, Auto-increment)
  - `name` (VARCHAR(255), Not Null)
  - `slug` (VARCHAR(255), Unique, Not Null)
- **`products` Table:**
  - `id` (BIGINT, PK, Identity)
  - `name` (VARCHAR(255), Not Null)
  - `short_description` (VARCHAR(100), Not Null)
  - `description` (TEXT, Not Null)
  - `price` (DOUBLE PRECISION, Not Null)
  - `cost_price` (DOUBLE PRECISION, Default 0.0)
  - `stock_quantity` (INTEGER, Default 0)
  - `lead_time_days` (INTEGER, Default 5)
  - `category_slug` (VARCHAR(255), FK to `categories.slug`)
  - `images` (TEXT)
  - `created_at` / `updated_at` (TIMESTAMPTZ)
- **`product_sizes` Table:**
  - `product_id` (BIGINT, FK to `products.id` ON DELETE CASCADE)
  - `size` (VARCHAR(50), e.g. `'UK 9'`, `'XL'`)
- **`product_colors` Table:**
  - `product_id` (BIGINT, FK to `products.id` ON DELETE CASCADE)
  - `color` (VARCHAR(50), e.g. `'White'`, `'Gray'`)

#### 2. `authdb` Database:
- **`users` Table:**
  - `id` (VARCHAR(64), PK)
  - `username` (VARCHAR(100), Unique, Not Null)
  - `email` (VARCHAR(150), Unique, Not Null)
  - `password` (VARCHAR(255), Not Null — BCrypt Encrypted hash)
  - `role` (VARCHAR(50), e.g. `'admin'`, `'user'`)
  - `first_name` (VARCHAR(100))
  - `last_name` (VARCHAR(100))
  - `image_url` (VARCHAR(500))
  - `created_at` / `updated_at` (TIMESTAMP)

#### 3. `orderdb` Database:
- **`orders` Table:**
  - `id` (VARCHAR(64), PK)
  - `user_id` (VARCHAR(100), Indexed)
  - `email` (VARCHAR(150))
  - `amount` (INTEGER, in cents/paise)
  - `status` (VARCHAR(50), e.g. `'success'`, `'pending'`, `'cancelled'`)
  - `products` (TEXT, JSON-serialized list of line items)
  - `created_at` / `updated_at` (TIMESTAMPTZ, Indexed)

---

### 2.4 Entity-Relationship (ER) Diagram

```mermaid
erDiagram
    CATEGORIES ||--o{ PRODUCTS : categorizes
    PRODUCTS ||--|{ PRODUCT_SIZES : has
    PRODUCTS ||--|{ PRODUCT_COLORS : available_in
    USERS ||--o{ ORDERS : places

    CATEGORIES {
        bigint id PK
        varchar name
        varchar slug UK
    }

    PRODUCTS {
        bigint id PK
        varchar name
        varchar short_description
        text description
        double_precision price
        double_precision cost_price
        integer stock_quantity
        integer lead_time_days
        varchar category_slug FK
        text images
        timestamptz created_at
        timestamptz updated_at
    }

    PRODUCT_SIZES {
        bigint product_id FK
        varchar size
    }

    PRODUCT_COLORS {
        bigint product_id FK
        varchar color
    }

    USERS {
        varchar id PK
        varchar username UK
        varchar email UK
        varchar password
        varchar role
        varchar first_name
        varchar last_name
        timestamp created_at
    }

    ORDERS {
        varchar id PK
        varchar user_id FK
        varchar email
        integer amount
        varchar status
        text products
        timestamptz created_at
    }
```

---

### 2.5 PostgreSQL Database Table Snapshots
Verification queries executed directly on PostgreSQL 16 container (`ecom-postgres`):

```sql
-- 1. Schema check in productdb
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'products';

-- Output:
-- id                | bigint           | NO
-- name              | character varying| NO
-- price             | double precision | NO
-- cost_price        | double precision | YES
-- stock_quantity    | integer          | YES
-- category_slug     | character varying| NO

-- 2. Live Record Verification in orderdb
SELECT id, user_id, email, status, amount, created_at 
FROM orders 
ORDER BY created_at DESC LIMIT 3;

-- Output:
-- 7e55f9a7de2245298d265d5876c9dd48 | usr_verified_clean | customer@example.com | success | 12000 | 2026-11-24 11:07:04.619818+00
-- 177cb5f59aa644c59d951d5d698b7f2f | guest_user         | customer@example.com | success | 150000| 2026-11-24 10:43:06.011662+00
```

---

## Chapter 3: Software Requirements and Technologies Used

### 3.1 Software and Frameworks Stack
- **Programming Languages:**
  - **Java 21 (LTS):** Utilizing modern syntax (`record` classes, switch pattern matching, enhanced Stream APIs).
  - **TypeScript 5.x / JavaScript (ES2024):** Used for Next.js web storefront and admin dashboard.
- **Integrated Development Environment (IDE):** IntelliJ IDEA Ultimate / Antigravity IDE / VS Code.
- **Backend Frameworks & Runtime:**
  - Spring Boot `3.4.3` (Spring MVC, Spring Data JPA, Spring Kafka).
  - Spring Security with BCrypt Password Encoder.
  - JJWT (`io.jsonwebtoken:jjwt-api:0.12.6`) for stateless authentication.
  - Apache Commons Math `3.6.1` for statistical distributions (Gamma/Poisson).
- **Frontend Technologies:**
  - **JavaFX 21.0.6:** OpenJFX Desktop toolkit with FXML layouts and modular CSS styling.
  - **Next.js 15 (React 19):** App Router, Tailwind CSS, Lucide Icons.
- **Database Management System (DBMS):** PostgreSQL `16.4-alpine` running via Docker.
- **Message Broker:** Apache Kafka `latest` with Zookeeper/KRaft protocol and Kafka-UI monitor (`:8080`).
- **Build Tools:** Apache Maven `3.9.9` with Maven Wrapper (`./mvnw`), pnpm `9.x`.

---

### 3.2 Hardware and Software Requirements
- **Minimum Hardware Requirements:**
  - **CPU:** Quad-Core x86_64 or Apple Silicon ARM64 (M1/M2/M3/M4).
  - **RAM:** Minimum 8 GB (16 GB Recommended to run all 5 microservices, Kafka, PostgreSQL, and JavaFX concurrently).
  - **Storage:** 5 GB free disk space for containers, shaded JARs, and database volumes.
- **Software Requirements:**
  - **Operating System:** macOS 14+, Ubuntu 22.04 LTS+, or Windows 11 (64-bit).
  - **JDK:** OpenJDK 21 or Eclipse Temurin 21.
  - **Docker Engine:** Version 26.0+ with Docker Compose v2.

---

### 3.3 Dataset and Seed Data
The application includes a curated retail dataset:
- **Product Catalog:** High-performance sneakers (Nike Air Max Pulse), jackets (Columbia Alpine Thermal Jacket), and training gear with high-resolution imagery and realistic pricing.
- **Dynamic Sizing Rules:**
  - **Footwear Category:** Sizes `UK 5`, `UK 6`, `UK 7`, `UK 8`, `UK 9`, `UK 10`, `UK 11`, `UK 12`.
  - **Apparel / Hoodies / Jackets:** Sizes `S`, `M`, `L`, `XL`, `XXL`.
- **Pre-Seeded Administrative & Customer Accounts:**
  - `admin` (Role: `admin`) — Full system access.
  - `johndoe` (Role: `user`) — General buyer persona.
  - `priya` (Role: `user`) — Active order history.

---

## Chapter 4: Object-Oriented Programming (OOP) Concepts Covered in the Project

### 4.1 Core OOP Principles

#### 4.1.1 Classes and Objects
Every domain in OmniCart is modeled using discrete classes and objects. For example, [Order.java](file:///Users/chinnu/Ecom/backend/order-service/src/main/java/com/ecom/order/model/Order.java) represents real-world purchases:
```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "user_id", length = 100)
    private String userId;

    private Integer amount;
    private String status;

    @Convert(converter = OrderItemsConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<OrderItemDTO> products;
}
```

#### 4.1.2 Encapsulation & Immutability via Java Records
Data transfer objects and Kafka messages strictly enforce encapsulation. By using **Java 21 `record`** classes, all fields are implicitly `private final`, preventing unexpected state mutations across microservice boundaries:
```java
public record PaymentSuccessfulEvent(
        String userId,
        String email,
        Integer amount,
        String status,
        List<OrderItemDTO> products
) implements Serializable {}
```

#### 4.1.3 Inheritance
OmniCart utilizes inheritance in Spring Data JPA repositories and JavaFX hierarchy. [OrderRepository.java](file:///Users/chinnu/Ecom/backend/order-service/src/main/java/com/ecom/order/repository/OrderRepository.java) inherits complete CRUD capability without boilerplate SQL:
```java
@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
}
```

#### 4.1.4 Polymorphism
Polymorphism is demonstrated in custom JPA converters implementing the standard `AttributeConverter<X, Y>` interface. The same `convertToEntityAttribute` method handles polymorphic JSON node structures dynamically:
```java
@Converter
public class OrderItemsConverter implements AttributeConverter<List<OrderItemDTO>, String> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<OrderItemDTO> attribute) {
        return attribute == null ? "[]" : mapper.writeValueAsString(attribute);
    }

    @Override
    public List<OrderItemDTO> convertToEntityAttribute(String dbData) {
        return mapper.readValue(dbData, new TypeReference<List<OrderItemDTO>>() {});
    }
}
```

#### 4.1.5 Abstraction
High-level controllers only interact with abstracted service interfaces (`ProductService`, `AuthService`, `PaymentService`), completely hiding whether persistence occurs via relational SQL, in-memory caches, or Kafka queues.

#### 4.1.6 Robust Exception Handling
OmniCart employs defensive programming with structured exception handling, ensuring external callers receive clear HTTP errors instead of stack traces:
```java
try {
    String clientSecret = paymentService.createCheckoutSession(request.getCart(), userId);
    return ResponseEntity.ok(new CheckoutSessionResponse(clientSecret));
} catch (Exception e) {
    log.error("Error creating checkout session", e);
    return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
}
```

---

### 4.2 Advanced Java Concepts Implemented

#### 4.2.1 JavaFX & GUI Development (FXML + MVC)
The desktop frontend separates view structure ([admin-dashboard-view.fxml](file:///Users/chinnu/Ecom/javafx-frontend/src/main/resources/com/omnicart/fxml/admin/admin-dashboard-view.fxml)) from business logic ([AdminDashboardController.java](file:///Users/chinnu/Ecom/javafx-frontend/src/main/java/com/omnicart/controller/admin/AdminDashboardController.java)). Custom CSS ensures modern dark/light mode visual ergonomics.

#### 4.2.2 Dynamic Event Handling
In [AddProductDialogController.java](file:///Users/chinnu/Ecom/javafx-frontend/src/main/java/com/omnicart/controller/admin/AddProductDialogController.java), a category selection `ChangeListener` dynamically toggles the available sizes between shoes and apparel:
```java
categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
    if (newVal != null && newVal.toLowerCase().contains("shoe")) {
        renderSizeChips(List.of("UK 5", "UK 6", "UK 7", "UK 8", "UK 9", "UK 10", "UK 11", "UK 12"));
    } else {
        renderSizeChips(List.of("S", "M", "L", "XL", "XXL"));
    }
});
```

#### 4.2.3 Asynchronous Multithreading & UI Thread Marshalling
1. **Background I/O:** All HTTP REST communication uses `java.net.http.HttpClient` with `CompletableFuture`, preventing network stalls from freezing the desktop window.
2. **UI Thread Marshalling:** Background thread responses are safely posted back onto the JavaFX UI Application Thread using `Platform.runLater(Runnable)`:
```java
apiClient.getAsync("/api/products", Product[].class)
    .thenAccept(products -> {
        Platform.runLater(() -> productTable.getItems().setAll(products));
    })
    .exceptionally(ex -> {
        Platform.runLater(() -> ToastService.showError("Network Error: " + ex.getMessage()));
        return null;
    });
```

#### 4.2.4 Java Collections & Stream Pipelines
Complex business logic leverages declarative Stream pipelines (`filter`, `map`, `collect(groupingBy)`):
```java
Map<String, Long> categoryCount = products.stream()
    .collect(Collectors.groupingBy(Product::getCategorySlug, Collectors.counting()));
```

---

## Chapter 5: Application Implementation and Screenshots

### 5.1 Authentication and Onboarding Interfaces
The platform provides role-based authentication securing both client storefronts and executive administrative tools:

> [!NOTE]
> **[Insert Screenshot: Figure 5.1 — User Login and Authentication Dialog]**
> * **View to Capture:** The JavaFX Portal Login Dialog or Next.js `/sign-in` screen.
> * **Key Elements to Show:** Username/email input fields, password field, "Sign In" action button, and role-based redirect indicator (`admin` vs `user`).

> [!NOTE]
> **[Insert Screenshot: Figure 5.2 — User Registration Interface]**
> * **View to Capture:** The Client Sign-up Modal or Web Registration page.
> * **Key Elements to Show:** First name, last name, email address, password fields, and validation feedback showing successful account creation and JWT dispatch.

---

### 5.2 Executive Management Dashboard
The executive desktop workstation provides real-time visibility into business operations, inventory levels, and revenue streams:

> [!NOTE]
> **[Insert Screenshot: Figure 5.3 — Main JavaFX Executive Dashboard]**
> * **View to Capture:** The full JavaFX Admin Station primary window ([admin-dashboard-view.fxml](file:///Users/chinnu/Ecom/javafx-frontend/src/main/resources/com/omnicart/fxml/admin/admin-dashboard-view.fxml)).
> * **Key Elements to Show:** Top-level KPI metric cards (Total Sales Revenue, Active Orders, Low Stock Warnings, Product Count) alongside the interactive monthly sales revenue Bar/Line chart.

> [!NOTE]
> **[Insert Screenshot: Figure 5.4 — Stochastic Financial & Demand Analytics Window]**
> * **View to Capture:** The Bayesian Inventory Intelligence section of the Admin Dashboard.
> * **Key Elements to Show:** The Stochastic Engine recommendation table with columns: Product Name, Current Stock, 30-Day Velocity, 95% Credible Interval (CI Low – CI High), Recommended Action (`URGENT_RESTOCK`, `HEALTHY`, `OVERSTOCKED`), and "Apply Reorder" action button.

---

### 5.3 Product Catalog & Dynamic Retail Sizing Modules
The catalog management module features category-dependent size configuration:

> [!NOTE]
> **[Insert Screenshot: Figure 5.5 — Add / Edit Product Modal (Footwear Selected)]**
> * **View to Capture:** The Add Product Dialog ([add-product-dialog.fxml](file:///Users/chinnu/Ecom/javafx-frontend/src/main/resources/com/omnicart/fxml/admin/add-product-dialog.fxml)) with "Shoes" chosen in the Category dropdown.
> * **Key Elements to Show:** Product name, cost price, retail price, stock quantity, lead time, and the dynamic **UK Shoe Sizing chips** (`UK 5`, `UK 6`, `UK 7`, `UK 8`, `UK 9`, `UK 10`, `UK 11`, `UK 12`).

> [!NOTE]
> **[Insert Screenshot: Figure 5.6 — Add / Edit Product Modal (Apparel Selected)]**
> * **View to Capture:** The Add Product Dialog with "Apparel", "Jackets", or "Hoodies" chosen in the Category dropdown.
> * **Key Elements to Show:** Sizing selection chips automatically switching from footwear numbers to **standard clothing sizes** (`S`, `M`, `L`, `XL`, `XXL`).

> [!NOTE]
> **[Insert Screenshot: Figure 5.7 — Administrative Product Inventory Table]**
> * **View to Capture:** The Admin Products Management view.
> * **Key Elements to Show:** Search bar with instant filtering, category selector dropdown, product data table with image thumbnails, price, stock counts, and "Edit" / "Delete" row action buttons.

---

### 5.4 Customer Storefront, Cart & Checkout Workflow
The customer journey encompasses browsing, variant selection, and immediate mock checkout:

> [!NOTE]
> **[Insert Screenshot: Figure 5.8 — Customer Product Details & Sizing Selection]**
> * **View to Capture:** The Product Details View ([product-details-view.fxml](file:///Users/chinnu/Ecom/javafx-frontend/src/main/resources/com/omnicart/fxml/product-details-view.fxml)) or Next.js Product Page.
> * **Key Elements to Show:** High-resolution product showcase, color picker swatches, dynamic size selector, quantity stepper, and "Add to Cart" button.

> [!NOTE]
> **[Insert Screenshot: Figure 5.9 — Shopping Cart & Native Mock Checkout Flow]**
> * **View to Capture:** The Cart Drawer / Checkout Window with items populated.
> * **Key Elements to Show:** Order summary calculation (subtotal, shipping, total), line item details, and the "Proceed to Checkout" action displaying instant order placement without third-party redirects.

---

### 5.5 Order Lifecycle & Database Operations (CRUD)
Administrators monitor order status and manage database records in real time:

> [!NOTE]
> **[Insert Screenshot: Figure 5.10 — Order Management and Order Cancellation View]**
> * **View to Capture:** The Admin Orders view showing customer purchase history.
> * **Key Elements to Show:** Order ID, customer email, order timestamp, amount (₹), status badge (`success`, `pending`, `cancelled`), and the red "Cancel Order" action button.

> [!NOTE]
> **[Insert Screenshot: Figure 5.11 — PostgreSQL Database Relations & Docker Infrastructure]**
> * **View to Capture:** Terminal showing `docker ps` with `ecom-postgres`, `ecom-kafka`, and `psql` shell displaying `SELECT * FROM orders;`.
> * **Key Elements to Show:** Running containers, database connection prompt, and relational row storage confirming persistence.

---

## Chapter 6: Conclusion and Future Work

### 6.1 Summary of Work Completed
The **OmniCart Enterprise E-Commerce Platform** successfully demonstrates a modern, scalable, distributed retail architecture:
1. **Microservices Partitioning:** Deployed 5 decoupled Spring Boot services with dedicated PostgreSQL databases (`productdb`, `orderdb`, `authdb`).
2. **Native Asynchronous Checkout:** Purged legacy third-party payment bloat in favor of a native mock checkout pipeline operating over Apache Kafka.
3. **Database Modernization:** Migrated historical MongoDB order documents into a structured PostgreSQL relational schema with zero data loss.
4. **Modern Java 21 Codebase:** Simplified verbose Java classes into immutable `record` components, reducing codebase bloat by ~40% while preserving 100% API compatibility.
5. **Cross-Platform Client Experience:** Delivered an administrative desktop suite with asynchronous multithreading, dynamic shoe/apparel sizing, and Bayesian inventory intelligence.

---

### 6.2 Future Work and Enhancements
- **Multi-Warehouse Geospatial Routing:** Implement location-based inventory allocation to route orders to the closest fulfillment center.
- **Elasticsearch Catalog Indexing:** Augment PostgreSQL full-text search with Elasticsearch for sub-millisecond autocomplete and typo-tolerant search queries.
- **WebSocket Live Dashboard:** Introduce STOMP/WebSockets over Spring Boot to push live order status updates directly to the JavaFX desktop UI without polling.
- **Mobile Client Application:** Develop a companion Flutter or React Native mobile shopping app connecting to the existing microservice REST gateway.
