@echo off
echo ============================================================
echo   OmniCart Backend - Starting All Microservices
echo ============================================================
echo.

echo [1/5] Starting Product Service (Port 8000)...
start "Product Service" cmd /c "java -jar product-service\target\product-service-1.0.0.jar & pause"

timeout /t 3 /nobreak >nul

echo [2/5] Starting Order Service (Port 8001)...
start "Order Service" cmd /c "java -jar order-service\target\order-service-1.0.0.jar & pause"

timeout /t 3 /nobreak >nul

echo [3/5] Starting Payment Service (Port 8002)...
start "Payment Service" cmd /c "java -jar payment-service\target\payment-service-1.0.0.jar & pause"

timeout /t 3 /nobreak >nul

echo [4/5] Starting Auth Service (Port 8003)...
start "Auth Service" cmd /c "java -jar auth-service\target\auth-service-1.0.0.jar & pause"

timeout /t 3 /nobreak >nul

echo [5/5] Starting Email Service...
start "Email Service" cmd /c "java -jar email-service\target\email-service-1.0.0.jar & pause"

echo.
echo ============================================================
echo   All services started in separate windows!
echo   Product  : http://localhost:8000
echo   Order    : http://localhost:8001
echo   Payment  : http://localhost:8002
echo   Auth     : http://localhost:8003
echo   Kafka UI : http://localhost:8080
echo ============================================================
echo.
pause

