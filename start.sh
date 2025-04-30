#!/bin/bash

# Exit on any error
set -e

echo "Building services..."

# Build API Gateway
echo "Building API Gateway..."
cd services/api-gateway
mvn clean package -DskipTests
cd ../..

# Build Auth Service
echo "Building Auth Service..."
cd services/auth-service
mvn clean package -DskipTests
cd ../..

# Build Inventory Service
echo "Building Inventory Service..."
cd services/inventory-service
mvn clean package -DskipTests
cd ../..

# Start all services with Docker Compose
echo "Starting services with Docker Compose..."
docker-compose up -d

echo "Waiting for services to start..."
sleep 10

echo "Services are starting. You can check their status with 'docker-compose ps'"
echo "Access the services at:"
echo "- API Gateway: http://localhost:8080"
echo "- Auth Service: http://localhost:8081"
echo "- Inventory Service: http://localhost:8082"
echo "- Eureka Service Registry: http://localhost:8761"
echo "- Prometheus: http://localhost:9090"
echo "- Grafana: http://localhost:3000 (admin/admin)"

echo "To view logs: docker-compose logs -f [service_name]"
echo "To stop all services: docker-compose down"