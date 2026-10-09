#!/bin/sh
# Chạy core-app ở máy local: đọc mật khẩu từ infra/compose/.env rồi chạy Spring Boot.
set -e
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
set -a
. "$ROOT/infra/compose/.env"
set +a
export DB_APP_PASSWORD="$CORE_APP_PASSWORD"
export DB_MIGRATE_PASSWORD="$CORE_OWNER_PASSWORD"
cd "$ROOT/core-app" && exec ./mvnw -q spring-boot:run
