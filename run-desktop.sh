#!/bin/bash
# ==========================================================
# OmniCart JavaFX Desktop Runner Script
# Launches the Outstanding Tier JavaFX E-Commerce Frontend
# ==========================================================

set -e

# Ensure Java runtime is found
if ! command -v java &>/dev/null; then
    if [ -d "/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home"
        export PATH="$JAVA_HOME/bin:$PATH"
    elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        export PATH="$JAVA_HOME/bin:$PATH"
    fi
fi

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ -d "$PROJECT_DIR/javafx-frontend" ]; then
    APP_DIR="$PROJECT_DIR/javafx-frontend"
else
    APP_DIR="$PROJECT_DIR/ecommerce-javafx"
fi

echo "=========================================================="
echo " Starting OmniCart JavaFX Desktop Platform...             "
echo "=========================================================="

cd "$APP_DIR"

# Launch using the JavaFX Maven plugin or pre-packaged JAR
if [ -f "target/javafx-frontend-1.0.0-all.jar" ]; then
    echo "Launching standalone application JAR..."
    java --enable-native-access=ALL-UNNAMED -jar target/javafx-frontend-1.0.0-all.jar "$@"
elif [ -f "target/javafx-frontend-1.0.0.jar" ]; then
    echo "Launching standalone application JAR..."
    java --enable-native-access=ALL-UNNAMED -jar target/javafx-frontend-1.0.0.jar "$@"
elif [ -f "target/ecommerce-javafx-1.0.0.jar" ]; then
    echo "Launching standalone application JAR..."
    java --enable-native-access=ALL-UNNAMED -jar target/ecommerce-javafx-1.0.0.jar "$@"
else
    echo "Running with JavaFX Maven plugin..."
    mvn javafx:run
fi
