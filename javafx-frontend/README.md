# OmniCart JavaFX Desktop Frontend

A high-performance, modern desktop frontend implementation for **OmniCart – Modern E-Commerce Platform**, built with **Java 21, JavaFX 21, FXML, and CSS**.

This frontend completely migrates and replaces the Next.js/React storefront and admin web frontends while **strictly preserving the original design, layout, color palette, typography, visual hierarchy, and user experience**.

---

## 1. Architecture & Design Principles

```
javafx-frontend/
│
├── pom.xml                                    # Maven 3.9+ / Java 21 configuration with JavaFX & Shade plugins
│
└── src/
    ├── main/
    │   ├── java/com/omnicart/
    │   │   ├── OmniCartApp.java              # JavaFX Application lifecycle entry point
    │   │   ├── Launcher.java                 # Uber-JAR standalone launcher (non-modular compatible)
    │   │   │
    │   │   ├── config/
    │   │   │   ├── AppConfig.java            # Service URLs, timeouts, API ports (:8000-:8003)
    │   │   │   └── SessionManager.java       # User authentication state, token, role-based session
    │   │   │
    │   │   ├── model/                        # Domain models matching backend Spring microservices
    │   │   │   ├── Product.java              # Catalog product with color images, sizes, pricing
    │   │   │   ├── Category.java             # Product category
    │   │   │   ├── CartItem.java             # Cart item with selected size, color, quantity
    │   │   │   ├── Order.java                # Order record from Order Service (MongoDB)
    │   │   │   ├── OrderItem.java            # Order item details
    │   │   │   ├── User.java                 # User account & role entity
    │   │   │   ├── ShippingAddress.java      # Checkout shipping address
    │   │   │   └── OrderChartData.java       # Analytics aggregates for dashboard charts
    │   │   │
    │   │   ├── api/                          # Non-blocking async REST API clients (Java HttpClient + Jackson)
    │   │   │   ├── ApiClient.java            # Base HTTP client with JSON serialization and JWT propagation
    │   │   │   ├── ProductApiService.java    # Product catalog, categories, search, filter, sort (:8000)
    │   │   │   ├── OrderApiService.java      # Customer orders, admin orders, order charts (:8001)
    │   │   │   ├── PaymentApiService.java    # Stripe sessions, test mock checkout (:8002)
    │   │   │   └── AuthApiService.java       # User directory, registration, authentication (:8003)
    │   │   │
    │   │   ├── service/                      # Client-side business logic
    │   │   │   ├── CartService.java          # Reactive shopping cart store with observable listeners
    │   │   │   ├── ToastService.java         # Animated toast popups mimicking React Toastify
    │   │   │   └── ImageLoaderService.java   # Local resource & remote image loader with LRU caching
    │   │   │
    │   │   ├── navigation/                   # View switching & scene routing
    │   │   │   ├── NavigationManager.java    # Central navigation controller
    │   │   │   └── ViewType.java             # Route view enumerations
    │   │   │
    │   │   ├── util/
    │   │   │   ├── CurrencyFormatter.java    # Formats INR currency (₹XX.XX)
    │   │   │   ├── SvgIcons.java             # Crisp vector Lucide SVG paths
    │   │   │   └── BrowserUtil.java          # Safe system browser launcher for Stripe
    │   │   │
    │   │   ├── ui/
    │   │   │   └── ProductCardView.java      # Reusable interactive product card component
    │   │   │
    │   │   └── controller/                   # FXML MVC Controllers
    │   │       ├── MainLayoutController.java # Storefront shell (Navbar, Search, Cart Badge, Footer)
    │   │       ├── HomeController.java       # Hero banner, category pills, product grid
    │   │       ├── ProductsController.java   # Filtered catalog, sort dropdown, search results
    │   │       ├── ProductDetailsController.java # Product gallery, swatch picker, Add to Cart
    │   │       ├── CartController.java       # 3-step checkout wizard (Cart -> Shipping -> Payment)
    │   │       ├── ReturnController.java     # Post-payment order success receipt screen
    │   │       ├── OrdersController.java     # Customer order history table
    │   │       ├── LoginDialogController.java# Authentication modal (Sign In / Register)
    │   │       │
    │   │       └── admin/                    # Admin Dashboard & Management Controllers
    │   │           ├── AdminLayoutController.java    # Admin sidebar navigation & top bar
    │   │           ├── AdminDashboardController.java # BarChart, PieChart, AreaChart, Todo, Cards
    │   │           ├── AdminProductsController.java  # Inventory data table with search, edit, delete
    │   │           ├── AdminOrdersController.java    # Orders data table with status pill badges
    │   │           ├── AdminUsersController.java     # User directory data table with avatar & role
    │   │           ├── AddProductDialogController.java
    │   │           ├── AddCategoryDialogController.java
    │   │           └── AddUserDialogController.java
    │   │
    │   └── resources/com/omnicart/
    │       ├── fxml/                         # Scene Builder-compatible FXML layouts
    │       │   ├── main-layout.fxml
    │       │   ├── home-view.fxml
    │       │   ├── products-view.fxml
    │       │   ├── product-details-view.fxml
    │       │   ├── cart-view.fxml
    │       │   ├── return-view.fxml
    │       │   ├── orders-view.fxml
    │       │   ├── login-dialog.fxml
    │       │   └── admin/
    │       │       ├── admin-layout.fxml
    │       │       ├── admin-dashboard-view.fxml
    │       │       ├── admin-products-view.fxml
    │       │       ├── admin-orders-view.fxml
    │       │       ├── admin-users-view.fxml
    │       │       ├── add-product-dialog.fxml
    │       │       ├── add-category-dialog.fxml
    │       │       └── add-user-dialog.fxml
    │       ├── css/                          # Modern CSS reproducing Tailwind/Shadcn styling
    │       │   ├── common.css
    │       │   ├── client.css
    │       │   └── admin.css
    │       └── images/                       # Bundled original image assets (logos, products, users)
    │           ├── products/
    │           └── users/
    │
    └── src/test/java/com/omnicart/
        └── SmokeTest.java                    # Unit and regression test suite
```

---

## 2. Visual & Functional Parity with Next.js Frontends

### A. Customer Storefront (`apps/client`)
1. **Navigation Bar**:
   - Exact "OMNICART." bold branding with logo.
   - Search bar with instant keyboard search.
   - Interactive cart icon with live badge counter updating on `CartService` events.
   - User profile menu button with "See Orders" and "Sign Out".
   - "Admin Portal" shortcut pill button when signed in as an administrator.
2. **Home Page**:
   - Featured banner image (3:1 aspect ratio).
   - Category chips bar with vector icons (All, T-shirts, Shoes, Accessories, Bags, Dresses, Jackets, Gloves).
   - Responsive product cards grid with smooth hover zoom animations.
   - Size selection and interactive color swatches that switch images in real time.
   - "Add to Cart" button with shopping cart icon and toast notification.
   - "View all products" direct link.
3. **Products Page**:
   - Sort dropdown: *Newest, Oldest, Price: Low to High, Price: High to Low*.
   - Filter by active search keyword or category slug.
4. **Product Details Page**:
   - High-resolution product image responding to color selection.
   - Size selection square buttons.
   - Quantity counter stepper `[-] Qty [+]`.
   - "Add to Cart" & "Buy this Item" (instant checkout) buttons.
   - Klarna, Credit Cards, and Stripe badges.
   - Terms & refund disclaimer.
5. **Shopping Cart & Checkout Wizard**:
   - **Step 1 (Cart)**: Line items with thumbnail, size, color, quantity stepper, subtotal, and trash removal button. Right summary sidebar with Subtotal, Discount(10%), Shipping Fee, Total, and Continue button.
   - **Step 2 (Shipping)**: Form with Name, Email, Phone, Address, City validation.
   - **Step 3 (Payment)**: Delivery address confirmation, total payable amount, Test Mode Active badge, and "Place Order" button calling Payment Service `/sessions/mock-checkout`, clearing cart, and recording orders. Also supports "Pay via Stripe Hosted Checkout" opening the session in the default system browser.
6. **Payment Confirmation (Return)**:
   - Green checkmark circle, "Payment Successful!", Session ID, payment status, "View Your Orders" and "Continue Shopping" actions.
7. **Order History**:
   - Customer orders loaded via `/user-orders` with Order ID, total in ₹, colored status badges (Success, Pending, Failed), formatted date, and items list.

### B. Admin Portal (`apps/admin`)
1. **Collapsible Navigation Sidebar**:
   - Application (Dashboard, Inbox, Calendar, Settings).
   - Products (See All Products, + Add Product modal, + Add Category modal).
   - Users (See All Users, + Add User modal).
   - Orders/Transactions (See All Transactions).
   - Administrator profile card with quick switch back to Storefront.
2. **Dashboard Overview**:
   - **Total Revenue Bar Chart**: Monthly bars for Total and Successful orders from `/order-chart`.
   - **Latest Transactions**: Mini cards with customer email, status badge, and formatted amount.
   - **Browser Usage Pie Chart**: Donut chart with browser percentages.
   - **Operational Tasks (Todo List)**: Calendar date picker with task checklist.
   - **Total Traffic Area Chart**: Stacked area curves for Desktop vs. Mobile visitors.
   - **Popular Products**: Mini cards with product thumbnail, category, and price.
3. **Product Inventory Management**:
   - DataTable with search filter, price sorting, circular thumbnails, and actions menu (Copy ID, View Details, Delete Product via `DELETE /products/{id}`).
4. **Order / Payment Transactions**:
   - DataTable with search, status pills (pending/yellow, success/green, failed/red), amount, and timestamp.
5. **User Management**:
   - DataTable with user avatar images, full name, email, role badge (`ADMIN`/`USER`), and active status.
6. **Role-Based Authorization**:
   - Only users with role `admin` can enter the admin portal. If an unauthorized user attempts access, access is denied.

---

## 3. Microservice Backend Integration

The JavaFX application connects directly to the 4 Spring Boot microservices over standard REST APIs:

| Microservice | Port | Endpoints Used |
|---|---|---|
| **Product Service** | `http://localhost:8000` | `GET /products`, `GET /products/{id}`, `POST /products`, `PUT /products/{id}`, `DELETE /products/{id}`, `GET /categories`, `POST /categories` |
| **Order Service** | `http://localhost:8001` | `GET /user-orders`, `GET /orders?limit={n}`, `GET /order-chart` |
| **Payment Service** | `http://localhost:8002` | `POST /sessions/create-checkout-session`, `POST /sessions/mock-checkout`, `GET /sessions/{id}` |
| **Auth Service** | `http://localhost:8003` | `GET /users`, `GET /users/{id}`, `POST /users`, `DELETE /users/{id}` |

### Non-blocking Async Architecture
All API calls utilize Java 21 `java.net.http.HttpClient` with `CompletableFuture`. UI thread safety is strictly enforced via `Platform.runLater()`, preventing UI freezing during network operations.

### Offline & Development Fallback
If the backend microservices or Docker containers are offline during local UI development or testing, the API service layer gracefully falls back to local mock stores and pre-seeded catalogs, ensuring the application remains testable and functional at all times.

---

## 4. How to Run the Application

### Option 1: Running with Maven
From `javafx-frontend` directory:
```bash
# Compile and run the desktop application
.\mvnw.cmd clean javafx:run
```

### Option 2: Running the Standalone Executable JAR
```bash
# 1. Build the shaded fat JAR
.\mvnw.cmd clean package -DskipTests

# 2. Run the generated fat JAR directly
java -jar target\javafx-frontend-1.0.0-all.jar
```

### Option 3: Double-click Launcher (Windows)
Double-click `run-client.cmd` in the `javafx-frontend` folder.

---

## 5. Testing Credentials

| Persona | Username | Password | Role | Access |
|---|---|---|---|---|
| **Administrator** | `admin` | `admin123` | `admin` | Full access to Customer Storefront & Admin Portal |
| **Customer** | `johndoe` | `password123` | `user` | Customer Storefront, Cart, Checkout, Order History |

*Tip: In the Sign-In modal dialog, click **"🔑 Admin Portal Login"** or **"👤 Customer Login"** for one-click demo login.*
