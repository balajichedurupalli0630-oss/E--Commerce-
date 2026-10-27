#!/usr/bin/env bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR"

echo "=== E-Commerce Java Microservices Platform ==="
echo "1. Starting Infrastructure (PostgreSQL, MongoDB, Kafka, Kafka UI)..."
docker compose up -d

echo ""
echo "Infrastructure containers running:"
docker compose ps

echo ""
echo "=== Ports & Services ==="
echo "PostgreSQL:      localhost:5432 (productdb)"
echo "MongoDB:         localhost:27017 (orderdb)"
echo "Kafka Broker:    localhost:9094"
echo "Kafka UI:        http://localhost:8080"
echo ""
echo "To run individual Java services:"
echo "  Product Service: (cd product-service && ../mvnw spring-boot:run)   -> http://localhost:8000"
echo "  Order Service:   (cd order-service   && ../mvnw spring-boot:run)   -> http://localhost:8001"
echo "  Payment Service: (cd payment-service && ../mvnw spring-boot:run)   -> http://localhost:8002"
echo "  Auth Service:    (cd auth-service    && ../mvnw spring-boot:run)   -> http://localhost:8003"
echo "  Email Service:   (cd email-service   && ../mvnw spring-boot:run)   -> Event Worker"
echo ""
echo "Or run jar files directly:"
echo "  java -jar product-service/target/product-service-1.0.0.jar"
echo "  java -jar order-service/target/order-service-1.0.0.jar"
echo "  java -jar payment-service/target/payment-service-1.0.0.jar"
echo "  java -jar auth-service/target/auth-service-1.0.0.jar"
echo "  java -jar email-service/target/email-service-1.0.0.jar"
