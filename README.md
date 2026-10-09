# Chain Store Platform

Backend của hệ thống chuỗi cửa hàng: 3 deployable (kiến trúc lai, xem `docs/adr`).

| Thư mục | Vai trò |
|---|---|
| `core-app/` | Modular Monolith (Spring Boot, Maven) |
| `sync-service/` | Change feed cho Store Agent (làm ở TASK-025, 028) |
| `order-ingest-service/` | Nhận ticket từ POS (làm ở TASK-012) |
| `contracts/` | OpenAPI, JSON Schema của sự kiện (thứ duy nhất dùng chung) |
| `infra/compose/` | Docker Compose: Postgres, Redis, Keycloak |
| `infra/scripts/` | Script tiện ích |

## Yêu cầu

Docker Desktop (Compose v2), JDK 21, Git.

## Dựng môi trường

```sh
cp infra/compose/.env.example infra/compose/.env   # lần đầu, rồi sửa mật khẩu và tag
infra/scripts/compose-local.sh up -d               # Postgres, Redis, Keycloak
infra/scripts/run-core.sh                          # chạy core-app
```

Dữ liệu Postgres chỉ khởi tạo ở lần đầu volume còn trống. Sửa script init thì phải
`infra/scripts/compose-local.sh down -v` (mất dữ liệu local) để chạy lại.

## Cổng

| Cổng | Dịch vụ |
|---|---|
| 5432 | Postgres |
| 6380 | Redis (map từ 6379) |
| 8180 | Keycloak (map từ 8080) |
| 8082 | core-app (API) |
| 8081 | core-app (health, metrics) |

## Kiểm tra

```sh
curl -s localhost:8081/health/liveness
curl -s localhost:8081/health/readiness
```

## Build và test

```sh
cd core-app && ./mvnw -B verify   # compile, test (Testcontainers), Spotless
./mvnw spotless:apply             # tự sửa định dạng khi Spotless báo lỗi
```

## Tài liệu

Quyết định kiến trúc: `docs/adr/`.
