#!/usr/bin/env bash
# ============================================================
# deploy-backend.sh — outvoice-backend (Spring Boot) deploy
# Run this FROM YOUR LAPTOP, from inside the outvoice-backend repo root.
# ============================================================
set -euo pipefail

# ---------- CONFIG (edit these once) ----------
SERVER_USER="root"                          # SSH user on E2E server
SERVER_HOST="outvoice"
REMOTE_APP_DIR="/opt/outvoice-backend"      # where the jar + service live on server
SERVICE_NAME="outvoice-backend"             # systemd service name (see outvoice-backend.service)
BUILD_TOOL="maven"                          # "maven" or "gradle" — CHANGE if needed
JAR_GLOB="target/*.jar"                     # gradle users: change to build/libs/*.jar
# ------------------------------------------------

echo "==> Building outvoice-backend locally..."
if [[ "$BUILD_TOOL" == "maven" ]]; then
  ./mvnw clean package -DskipTests
elif [[ "$BUILD_TOOL" == "gradle" ]]; then
  ./gradlew clean bootJar
  JAR_GLOB="build/libs/*.jar"
else
  echo "Unknown BUILD_TOOL: $BUILD_TOOL"; exit 1
fi

JAR_FILE=$(ls $JAR_GLOB | grep -v plain | head -n1)
if [[ -z "$JAR_FILE" ]]; then
  echo "No jar found matching $JAR_GLOB"; exit 1
fi
echo "==> Built: $JAR_FILE"

echo "==> Uploading jar to server..."
scp -O "$JAR_FILE" "$SERVER_USER@$SERVER_HOST:$REMOTE_APP_DIR/outvoice-backend-new.jar"

echo "==> Swapping jar and restarting service..."
ssh "$SERVER_USER@$SERVER_HOST" bash -s <<EOF
set -e
cd "$REMOTE_APP_DIR"
# keep one rollback copy
if [[ -f outvoice-backend.jar ]]; then
  mv outvoice-backend.jar outvoice-backend.jar.bak
fi
mv outvoice-backend-new.jar outvoice-backend.jar
sudo systemctl restart "$SERVICE_NAME"
sleep 3
sudo systemctl status "$SERVICE_NAME" --no-pager -l | head -n 15
EOF

echo "==> Done. Tail logs with:"
echo "    ssh $SERVER_USER@$SERVER_HOST 'sudo journalctl -u $SERVICE_NAME -f'"