# 🔔 Multi-Tenant Real-Time Notification System

A production-ready Spring Boot backend for a SaaS notification platform with strict multi-tenancy,
real-time WebSocket broadcasting, and full PostgreSQL persistence.

---

## 🏗️ Project Structure

```
src/main/java/com/saas/notification/
├── NotificationSystemApplication.java
├── config/
│   ├── ApiKeyFilter.java          ← Intercepts all requests, resolves tenant from X-API-KEY
│   ├── TenantContext.java         ← ThreadLocal tenant holder for strict isolation
│   ├── WebSocketConfig.java       ← STOMP over SockJS on /ws, broker on /topic
│   ├── CorsConfig.java            ← Allows http://localhost:5173 with credentials
│   └── SwaggerConfig.java         ← OpenAPI 3 with API key security scheme
├── controllers/
│   ├── NotificationController.java
│   ├── UserController.java
│   └── TenantController.java
├── domain/
│   ├── entity/
│   │   ├── Tenant.java
│   │   ├── User.java
│   │   └── Notification.java
│   └── enums/
│       ├── NotificationChannel.java   ← EMAIL | SMS | PUSH | INTERNAL
│       └── NotificationStatus.java    ← PENDING | SENT | FAILED
├── dto/
│   ├── NotificationDtos.java
│   ├── UserDtos.java
│   └── TenantDtos.java
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── TenantAccessDeniedException.java
│   └── GlobalExceptionHandler.java
├── repository/
│   ├── TenantRepository.java
│   ├── UserRepository.java
│   └── NotificationRepository.java
└── service/
    ├── NotificationService.java    ← Core business logic + WebSocket broadcast
    ├── UserService.java
    └── TenantService.java
```

---

## ⚙️ Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

---

## 🚀 Setup & Run

### 1. Create the PostgreSQL database

```sql
CREATE DATABASE notification_db;
```

### 2. Configure credentials

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/notification_db
spring.datasource.username=YOUR_PG_USER
spring.datasource.password=YOUR_PG_PASSWORD
```

### 3. Run the application

```bash
mvn spring-boot:run
```

Hibernate will **auto-create** the schema on first launch (`ddl-auto=update`).

### 4. Seed initial data

```bash
psql -U postgres -d notification_db -f seed.sql
```

---

## 🔑 Authentication

Every API request must include the `X-API-KEY` header:

| Tenant      | API Key                  |
|-------------|--------------------------|
| Acme Corp   | `acme-api-key-abc123`    |
| Globex Inc  | `globex-api-key-xyz789`  |

Missing or invalid key → **401 Unauthorized**  
Cross-tenant access attempt → **403 Forbidden**

---

## 📡 REST API

### Get all tenants
```
GET /api/tenants
X-API-KEY: acme-api-key-abc123
```

### Get users (scoped to tenant)
```
GET /api/users
X-API-KEY: acme-api-key-abc123
```

### Get notifications for a user
```
GET /api/notifications/user/1
X-API-KEY: acme-api-key-abc123
```

### Create a notification
```
POST /api/notifications?userId=1
X-API-KEY: acme-api-key-abc123
Content-Type: application/json

{
  "title": "Welcome!",
  "message": "Your account is ready.",
  "channel": "INTERNAL"
}
```

Response `201 Created`:
```json
{
  "id": 1,
  "title": "Welcome!",
  "message": "Your account is ready.",
  "channel": "INTERNAL",
  "status": "SENT",
  "createdAt": "2024-03-15T10:30:00",
  "userId": 1,
  "userName": "Alice Martin"
}
```

---

## ⚡ WebSocket / Real-Time

Connect with **SockJS + STOMP**:

```javascript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
  onConnect: () => {
    client.subscribe('/topic/notifications', (message) => {
      const notification = JSON.parse(message.body);
      console.log('New notification:', notification);
    });
  }
});

client.activate();
```

Every `POST /api/notifications` call broadcasts the new notification to all subscribers on `/topic/notifications`.

---

## 📘 Swagger UI

```
http://localhost:8080/swagger-ui.html
```

Click **Authorize**, enter your API key, and test all endpoints interactively.

---

## 🛡️ Security Model

| Layer              | Mechanism                                      |
|--------------------|------------------------------------------------|
| Authentication     | `X-API-KEY` header → resolves to Tenant        |
| Tenant Isolation   | `TenantContext` (ThreadLocal) on every request |
| Query Scoping      | All repository queries filter by `tenant_id`   |
| Cross-tenant guard | 403 if user.tenant ≠ authenticated tenant      |
| WebSocket bypass   | `/ws/**` excluded from API key filter          |

---

## 🧪 Quick Test (cURL)

```bash
# Create a notification
curl -X POST "http://localhost:8080/api/notifications?userId=1" \
  -H "X-API-KEY: acme-api-key-abc123" \
  -H "Content-Type: application/json" \
  -d '{"title":"Hello","message":"Test message","channel":"EMAIL"}'

# List notifications for user 1
curl "http://localhost:8080/api/notifications/user/1" \
  -H "X-API-KEY: acme-api-key-abc123"

# Attempt cross-tenant access (returns 403)
curl "http://localhost:8080/api/notifications/user/3" \
  -H "X-API-KEY: acme-api-key-abc123"
```
