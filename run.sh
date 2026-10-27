#!/usr/bin/env bash
# ====================================================================
# CourseConnect - Student Course Enrollment System
# Linux & macOS Launcher Script (Port 8082)
# ====================================================================

set -e

TARGET_PORT=8082

echo "===================================================================="
echo "       CourseConnect - Student Course Enrollment System"
echo "                  College DSA Mini-Project"
echo "===================================================================="
echo ""

# 1. Check for javac
if ! command -v javac &> /dev/null; then
    echo "[ERROR] 'javac' compiler was not found. Please install JDK 17 or higher."
    exit 1
fi

# 2. Check for java
if ! command -v java &> /dev/null; then
    echo "[ERROR] 'java' runtime was not found. Please install JDK 17 or higher."
    exit 1
fi

# 3. Port Pre-flight Check: Verify Port 8082 is available
echo "[*] Checking availability of port ${TARGET_PORT}..."
PORT_OCCUPIED=0
if command -v lsof &> /dev/null; then
    if lsof -Pi :${TARGET_PORT} -sTCP:LISTEN -t >/dev/null ; then
        PORT_OCCUPIED=1
    fi
elif command -v nc &> /dev/null; then
    if nc -z localhost ${TARGET_PORT} 2>/dev/null; then
        PORT_OCCUPIED=1
    fi
fi

if [ $PORT_OCCUPIED -eq 1 ]; then
    echo ""
    echo "===================================================================="
    echo " [WARNING] Port ${TARGET_PORT} is currently in use by another process!"
    echo "===================================================================="
    echo " CourseConnect will NOT stop or terminate any existing processes."
    echo ""
    echo " Suggested action:"
    echo "   Please run CourseConnect on another available port, for example:"
    echo "   java -cp backend/bin Main --port 8083 --frontend frontend"
    echo "===================================================================="
    exit 1
fi
echo "[OK] Port ${TARGET_PORT} is verified available!"
echo ""

# 4. Create build directory
mkdir -p backend/bin

# 5. Compile Java sources
echo "[*] Compiling backend Java sources..."
javac -d backend/bin \
    backend/src/model/*.java \
    backend/src/util/*.java \
    backend/src/repository/*.java \
    backend/src/service/*.java \
    backend/src/server/*.java \
    backend/src/Main.java

echo "[OK] Compilation successful!"
echo ""

# 6. Launch application on port 8082
echo "[*] Starting CourseConnect on http://localhost:${TARGET_PORT}..."
echo "--------------------------------------------------------------------"
echo " Open your browser at: http://localhost:${TARGET_PORT}"
echo " Demo Accounts:"
echo "   - Student:     student01 / student123"
echo "   - Student 2:   student02 / student123"
echo "   - Institution: admin01   / admin123"
echo " Press Ctrl+C to stop the server."
echo "--------------------------------------------------------------------"
echo ""

java -cp backend/bin Main --port ${TARGET_PORT} --frontend frontend
