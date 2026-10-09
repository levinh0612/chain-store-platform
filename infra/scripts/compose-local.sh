#!/bin/sh
# Bọc lệnh compose dài. Ví dụ: infra/scripts/compose-local.sh up -d
cd "$(dirname "$0")/../compose" && docker compose -f compose.yml -f compose.local.yml "$@"
