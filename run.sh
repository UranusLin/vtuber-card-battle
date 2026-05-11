#!/bin/bash
# ═══════════════════════════════════════
# VTuber Card Battle — 編譯 & 執行腳本
# ═══════════════════════════════════════

# 建立輸出目錄
mkdir -p out lib

# 檢查 PostgreSQL JDBC Driver
if [ ! -f lib/postgresql-42.7.3.jar ]; then
    echo "📥 下載 PostgreSQL JDBC Driver..."
    curl -L -o lib/postgresql-42.7.3.jar \
        https://jdbc.postgresql.org/download/postgresql-42.7.3.jar
    echo "✅ 下載完成！"
fi

# 編譯
echo "🔨 編譯中..."
javac -cp "lib/*" -d out -encoding UTF-8 \
    src/main/java/com/vcb/model/enums/*.java \
    src/main/java/com/vcb/model/*.java \
    src/main/java/com/vcb/exception/*.java \
    src/main/java/com/vcb/config/*.java \
    src/main/java/com/vcb/dao/*.java \
    src/main/java/com/vcb/service/*.java \
    src/main/java/com/vcb/view/*.java \
    src/main/java/com/vcb/Main.java

if [ $? -eq 0 ]; then
    echo "✅ 編譯成功！"
    echo ""
    echo "🎮 啟動遊戲..."
    java -cp "out:lib/*" com.vcb.Main
else
    echo "❌ 編譯失敗！請檢查錯誤訊息。"
fi
