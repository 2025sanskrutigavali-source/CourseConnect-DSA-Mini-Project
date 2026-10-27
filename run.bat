@echo off
setlocal enabledelayedexpansion

echo ====================================================================
echo        CourseConnect - Student Course Enrollment System
echo                   College DSA Mini-Project
echo ====================================================================
echo.

set TARGET_PORT=8082

:: 1. Verify Java compiler is installed
where javac >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] 'javac' compiler was not found in your system PATH.
    echo Please install JDK 17 or higher and ensure javac is in PATH.
    pause
    exit /b 1
)

:: 2. Verify Java runtime is installed
where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] 'java' runtime was not found in your system PATH.
    echo Please install JDK 17 or higher and ensure java is in PATH.
    pause
    exit /b 1
)

:: 3. Port Pre-flight Check: Verify Port 8082 is available
echo [*] Checking availability of port %TARGET_PORT%...
powershell -NoProfile -Command "$conn = Get-NetTCPConnection -LocalPort %TARGET_PORT% -ErrorAction SilentlyContinue; if ($conn) { exit 1 } else { exit 0 }" >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo.
    echo ====================================================================
    echo  [WARNING] Port %TARGET_PORT% is already in use by another application!
    echo ====================================================================
    echo  CourseConnect will NOT stop or terminate any existing processes.
    echo  (e.g., Jenkins on port 8080 or earlier projects on port 8081).
    echo.
    echo  Suggested action:
    echo    Please check port 8083 or specify another available port:
    echo    java -cp backend\bin Main --port 8083 --frontend frontend
    echo ====================================================================
    pause
    exit /b 1
)
echo [OK] Port %TARGET_PORT% is verified available!
echo.

:: 4. Create output directory
if not exist "backend\bin" (
    mkdir "backend\bin"
)

:: 5. Compile Java source files
echo [*] Compiling Java backend source code...
javac -d backend\bin backend\src\model\*.java backend\src\util\*.java backend\src\repository\*.java backend\src\service\*.java backend\src\server\*.java backend\src\Main.java
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed. Please review error messages above.
    pause
    exit /b 1
)
echo [OK] Compilation successful!
echo.

:: 6. Launch CourseConnect Server on Port 8082
echo [*] Starting CourseConnect HTTP Server on http://localhost:%TARGET_PORT%...
echo.
echo ====================================================================
echo  CourseConnect is accessible at:
echo    http://localhost:%TARGET_PORT%
echo.
echo  Demo Accounts:
echo    - Student Account:     student01 / student123
echo    - Student Account 2:   student02 / student123
echo    - Institution Account: admin01   / admin123
echo.
echo  Press Ctrl+C in this console window to gracefully stop the server.
echo ====================================================================
echo.

:: Launch default browser
start http://localhost:%TARGET_PORT%

java -cp backend\bin Main --port %TARGET_PORT% --frontend frontend

pause
