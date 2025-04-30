# How to Start and Test the Project

## Prerequisites

Ensure you have the following installed:
- Java 21
- Maven
- Docker and Docker Compose
- Kubernetes cluster (optional, for full deployment)
- Postman or similar API testing tool

## Option 1: Running Locally with Docker Compose

This is the quickest way to get started for development and testing.

### Step 1: Build and Start the Services

You can use the provided scripts to build and start all services automatically:

**For Windows:**
```
start.bat
```

**For Linux/Mac:**
```bash
chmod +x start.sh
./start.sh
```

Alternatively, you can build each service manually:

```bash
# Navigate to each service directory and build
cd services/api-gateway
mvn clean package -DskipTests

cd ../auth-service
mvn clean package -DskipTests

cd ../inventory-service
mvn clean package -DskipTests
```

### Step 2: Start the Services with Docker Compose

A `docker-compose.yml` file has been created in the project root. It includes all the necessary services:

- PostgreSQL database with automatic creation of auth_db and inventory_db
- Redis cache for distributed caching
- Eureka service registry for service discovery
- Auth Service for authentication and authorization
- Inventory Service for inventory management
- API Gateway for routing requests
- Prometheus for metrics collection
- Grafana for metrics visualization

The PostgreSQL container uses a custom initialization script (`database/postgres/init-multiple-databases.sh`) to automatically create the required databases when the container starts.

```yaml
version: '3'
services:
  postgres:
    image: postgres:13
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgrespassword
      POSTGRES_MULTIPLE_DATABASES: auth_db,inventory_db
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:6.2-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  service-registry:
    image: springcloud/eureka
    ports:
      - "8761:8761"

  auth-service:
    build: ./services/auth-service
    ports:
      - "8081:8081"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/auth_db
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgrespassword
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-registry:8761/eureka/
    depends_on:
      postgres:
        condition: service_healthy
      service-registry:
        condition: service_started

  inventory-service:
    build: ./services/inventory-service
    ports:
      - "8082:8082"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/inventory_db
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgrespassword
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-registry:8761/eureka/
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
      service-registry:
        condition: service_started

  api-gateway:
    build: ./services/api-gateway
    ports:
      - "8080:8080"
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-registry:8761/eureka/
    depends_on:
      - service-registry
      - auth-service
      - inventory-service

  prometheus:
    image: prom/prometheus:v2.30.3
    ports:
      - "9090:9090"
    volumes:
      - ./kubernetes/base/prometheus-config.yaml:/etc/prometheus/prometheus.yml
    depends_on:
      - api-gateway
      - auth-service
      - inventory-service

  grafana:
    image: grafana/grafana:8.2.2
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - ./monitoring/grafana/dashboards:/var/lib/grafana/dashboards
    depends_on:
      - prometheus

volumes:
  postgres-data:
  redis-data:
```

Then run:

```bash
docker-compose up -d
```

### Step 3: Stopping the Services

To stop all services, you can use the provided scripts:

**For Windows:**
```
stop.bat
```

**For Linux/Mac:**
```bash
chmod +x stop.sh
./stop.sh
```

Alternatively, you can stop the services manually:

```bash
docker-compose down
```

## Option 2: Deploying to Kubernetes

For a more production-like environment, you can deploy to Kubernetes.

### Step 1: Build Docker Images

```bash
# Build and tag images
docker build -t inventory-system/api-gateway:dev ./services/api-gateway
docker build -t inventory-system/auth-service:dev ./services/auth-service
docker build -t inventory-system/inventory-service:dev ./services/inventory-service
```

### Step 2: Apply Kubernetes Configurations

```bash
# Apply the dev overlay
kubectl apply -k kubernetes/overlays/dev
```

## Testing the Application

### 1. Register a User

```
POST http://localhost:8080/auth/api/auth/signup
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "roles": ["ROLE_USER"]
}
```

### 2. Login to Get JWT Token

```
POST http://localhost:8080/auth/api/auth/signin
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123"
}
```

Save the JWT token from the response.

### 3. Create a Category

```
POST http://localhost:8080/inventory/api/categories
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "name": "Test Category",
  "description": "Test category description"
}
```

### 4. Create a Location

```
POST http://localhost:8080/inventory/api/locations
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "name": "Test Location",
  "address": "123 Test St",
  "city": "Test City",
  "state": "TS",
  "zipCode": "12345",
  "country": "Test Country"
}
```

### 5. Create an Inventory Item

```
POST http://localhost:8080/inventory/api/inventory
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "name": "Test Item",
  "description": "Test item description",
  "sku": "TEST-001",
  "quantity": 100,
  "threshold": 20,
  "price": 19.99,
  "category": {
    "id": 1
  },
  "location": {
    "id": 1
  }
}
```

### 6. Update Inventory Quantity

```
PATCH http://localhost:8080/inventory/api/inventory/1/quantity
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "quantityChange": -10
}
```

### 7. Check Low Stock Items

```
GET http://localhost:8080/inventory/api/inventory/low-stock
Authorization: Bearer <your_jwt_token>
```

## Monitoring

If you've deployed with Docker Compose or Kubernetes, you can access:

- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000

Default Grafana credentials: admin/admin

For Kubernetes port-forwarding:

```bash
kubectl port-forward -n inventory-system svc/prometheus 9090:9090
kubectl port-forward -n inventory-system svc/grafana 3000:3000
```

## Troubleshooting

1. **Services not starting**: Check logs with `docker-compose logs <service-name>` or `kubectl logs <pod-name> -n inventory-system`
2. **Database connection issues**: Ensure PostgreSQL is running and accessible
3. **Authentication failures**: Verify JWT token is valid and not expired
4. **API Gateway routing issues**: Check Eureka service registry at http://localhost:8761 to ensure services are registered

## Additional Resources

- API Documentation: Available at http://localhost:8082/inventory/swagger-ui.html when the inventory service is running
- Monitoring Dashboard: Import the dashboard JSON from monitoring/grafana/dashboards into Grafana
