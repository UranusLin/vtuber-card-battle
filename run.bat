@echo off
REM ═══════════════════════════════════════
REM VTuber Card Battle — 編譯 & 執行腳本 (Windows)
REM ═══════════════════════════════════════

REM 建立輸出目錄
if not exist out mkdir out
if not exist lib mkdir lib

REM 檢查 PostgreSQL JDBC Driver
if not exist lib\postgresql-42.7.3.jar (
    echo 請手動下載 PostgreSQL JDBC Driver:
    echo https://jdbc.postgresql.org/download/postgresql-42.7.3.jar
    echo 放到 lib\ 資料夾中
    pause
    exit /b
)

REM 編譯
echo 🔨 編譯中...
javac -cp "lib\*" -d out -encoding UTF-8 ^
    src\main\java\com\vcb\model\enums\*.java ^
    src\main\java\com\vcb\model\*.java ^
    src\main\java\com\vcb\exception\*.java ^
    src\main\java\com\vcb\config\*.java ^
    src\main\java\com\vcb\dao\*.java ^
    src\main\java\com\vcb\service\*.java ^
    src\main\java\com\vcb\view\*.java ^
    src\main\java\com\vcb\Main.java

if %ERRORLEVEL% EQU 0 (
    echo ✅ 編譯成功！
    echo.
    echo 🎮 啟動遊戲...
    java -cp "out;lib\*" com.vcb.Main
) else (
    echo ❌ 編譯失敗！請檢查錯誤訊息。
)
pause
