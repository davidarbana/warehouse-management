# Warehouse Management System

A Spring Boot REST API for managing warehouse inventory, orders, and deliveries.
Built as a home assignment for **Lufthansa Industry Solutions**.

---

## Tech Stack

- **Java 17**
- **Spring Boot 3.2**
- **Spring Security + JWT**
- **Spring Data JPA / Hibernate**
- **H2 In-Memory Database**
- **Log4j2**
- **Swagger / OpenAPI 3**
- **JUnit 5 + Mockito**
- **Maven**
- **Lombok**

---

## How to Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Steps

```bash
# Clone the repository
git clone <repo-url>
cd warehouse

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application starts on **http://localhost:8080**

---

## Default Users (seeded on startup)

| Username  | Password     | Role              |
|-----------|--------------|-------------------|
| admin     | admin123     | SYSTEM_ADMIN      |
| manager   | manager123   | WAREHOUSE_MANAGER |
| client    | client123    | CLIENT            |

---

## API Documentation

Swagger UI is available at:
**http://localhost:8080/swagger-ui.html**

H2 Console is available at:
**http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:warehousedb`
- Username: `sa`
- Password: *(empty)*

---

## Authentication

All protected endpoints require a JWT Bearer token.

1. Call `POST /api/v1/auth/login` with your credentials
2. Copy the token from the response
3. Add it to the `Authorization` header: `Bearer <token>`

In Swagger UI, click **Authorize** and paste your token.

---

## Endpoints Overview

### Auth
| Method | Path | Description | Access |
|--------|------|-------------|--------|
| POST | `/api/v1/auth/login` | Login and get JWT token | Public |
| POST | `/api/v1/auth/register` | Register a new user | Public |

### Orders
| Method | Path | Description | Access |
|--------|------|-------------|--------|
| POST | `/api/v1/orders` | Create a new order | CLIENT |
| GET | `/api/v1/orders/my` | Get my orders (filter by status) | CLIENT |
| POST | `/api/v1/orders/{id}/items` | Add item to order | CLIENT |
| DELETE | `/api/v1/orders/{id}/items/{itemId}` | Remove item from order | CLIENT |
| PATCH | `/api/v1/orders/{id}/items/{itemId}` | Update item quantity | CLIENT |
| POST | `/api/v1/orders/{id}/submit` | Submit order for approval | CLIENT |
| POST | `/api/v1/orders/{id}/cancel` | Cancel order | CLIENT |
| GET | `/api/v1/orders` | Get all orders (filter by status) | WAREHOUSE_MANAGER |
| GET | `/api/v1/orders/{id}` | Get order details | WAREHOUSE_MANAGER |
| POST | `/api/v1/orders/{id}/approve` | Approve an order | WAREHOUSE_MANAGER |
| POST | `/api/v1/orders/{id}/decline` | Decline an order with reason | WAREHOUSE_MANAGER |
| POST | `/api/v1/orders/{id}/schedule-delivery` | Schedule delivery | WAREHOUSE_MANAGER |
| GET | `/api/v1/orders/{id}/available-dates` | Get available delivery dates | WAREHOUSE_MANAGER |

### Inventory
| Method | Path | Description | Access |
|--------|------|-------------|--------|
| GET | `/api/v1/inventory` | Get all items | CLIENT, WAREHOUSE_MANAGER |
| GET | `/api/v1/inventory/{id}` | Get item by ID | CLIENT, WAREHOUSE_MANAGER |
| POST | `/api/v1/inventory` | Create item | WAREHOUSE_MANAGER |
| PUT | `/api/v1/inventory/{id}` | Update item | WAREHOUSE_MANAGER |
| DELETE | `/api/v1/inventory/{id}` | Delete item | WAREHOUSE_MANAGER |

### Trucks
| Method | Path | Description | Access |
|--------|------|-------------|--------|
| GET | `/api/v1/trucks` | Get all trucks | WAREHOUSE_MANAGER |
| GET | `/api/v1/trucks/{id}` | Get truck by ID | WAREHOUSE_MANAGER |
| POST | `/api/v1/trucks` | Create truck | WAREHOUSE_MANAGER |
| PUT | `/api/v1/trucks/{id}` | Update truck | WAREHOUSE_MANAGER |
| DELETE | `/api/v1/trucks/{id}` | Delete truck | WAREHOUSE_MANAGER |

### Users (SYSTEM_ADMIN only)
| Method | Path | Description | Access |
|--------|------|-------------|--------|
| GET | `/api/v1/users` | Get all users | SYSTEM_ADMIN |
| GET | `/api/v1/users/{id}` | Get user by ID | SYSTEM_ADMIN |
| POST | `/api/v1/users` | Create user | SYSTEM_ADMIN |
| PUT | `/api/v1/users/{id}` | Update user | SYSTEM_ADMIN |
| DELETE | `/api/v1/users/{id}` | Delete user | SYSTEM_ADMIN |

---

## Order Lifecycle

```
CREATED → AWAITING_APPROVAL → APPROVED → UNDER_DELIVERY → FULFILLED
                           ↓
                        DECLINED → (edit) → AWAITING_APPROVAL
CREATED/AWAITING_APPROVAL/APPROVED/DECLINED → CANCELED
```

- Orders can be edited (add/remove/update items) only in **CREATED** or **DECLINED** status
- Submitting moves status from CREATED/DECLINED → **AWAITING_APPROVAL**
- A daily cronjob runs at midnight and marks UNDER_DELIVERY orders as **FULFILLED** when the delivery date is reached

---

## Delivery Scheduling Rules

- Deliveries happen within a single day (weekdays only)
- A truck can only complete **one delivery per day**
- Selected trucks must have enough **combined volume** to carry the order
- Available delivery dates can be queried for up to **30 days** ahead
- When a delivery is scheduled, **inventory quantities are deducted** automatically

---

## Project Structure

```
src/main/java/com/davidarbana/warehouse/
├── config/          # SwaggerConfig, DataSeeder
├── controller/      # REST controllers
├── dto/
│   ├── request/     # Incoming request DTOs
│   └── response/    # Outgoing response DTOs
├── entity/          # JPA entities
├── enums/           # Role, OrderStatus
├── exception/       # Custom exceptions, GlobalExceptionHandler
├── repository/      # Spring Data JPA repositories
├── scheduled/       # Cronjob for delivery fulfillment
├── security/        # JwtService, JwtAuthFilter, SecurityConfig
└── service/
    ├── interfaces   # Service interfaces
    └── impl/        # Service implementations
```

---

## Assumptions

- H2 in-memory database is used for simplicity — data resets on each restart
- All deliveries happen within a single day as specified
- Volume is measured in cubic meters
- The delivery scheduling period defaults to 30 days max (configurable via `delivery.scheduling.max-period`)
