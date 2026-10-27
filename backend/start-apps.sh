#!/usr/bin/env bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR"

mkdir -p logs

echo "=== Starting E-Commerce Java Microservices ==="

start_service() {
  local name=$1
  local jar="$name/target/$name-1.0.0.jar"
  local log="logs/$name.log"

  if [ ! -f "$jar" ]; then
    echo "[-] $name JAR not found at $jar. Please build first with ./mvnw clean package -DskipTests"
    return 1
  fi

  echo "[+] Starting $name (logs -> $log)..."
  nohup java -jar "$jar" > "$log" 2>&1 &
  echo $! > "logs/$name.pid"
}

start_service "product-service"
start_service "order-service"
start_service "payment-service"
start_service "auth-service"
start_service "email-service"

echo ""
echo "=== All Java microservices started in background ==="
echo "Logs are available in backend/logs/<service>.log"
echo "To view live logs: tail -f backend/logs/product-service.log"
echo "To stop all services: ./stop-apps.sh"
