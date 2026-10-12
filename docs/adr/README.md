# Architecture Decision Records

Mỗi ADR ghi một quyết định kiến trúc: bối cảnh, quyết định, phương án đã cân nhắc, hệ quả và trigger xem lại. Mẫu: [_template.md](_template.md).

| ID | Tiêu đề | Trạng thái | Ngày |
|---|---|---|---|
| ADR-001 | [Kiến trúc lai: core Modular Monolith + 2 service tách sớm (sync-service, order-ingest-service)](ADR-001-kien-truc-lai-core-modular-monolith-2-service-tach.md) | accepted | 2026-10-08 |
| ADR-002 | [Cursor-based sync cho master data (pull model)](ADR-002-cursor-based-sync-cho-master-data-pull-model.md) | accepted | 2026-10-08 |
| ADR-003 | [Inbox + Outbox pattern cho order ingestion](ADR-003-inbox-outbox-pattern-cho-order-ingestion.md) | accepted | 2026-10-08 |
| ADR-004 | [Kafka là hạ tầng Phase 1: master data core -> sync, ingest, analytics, audit](ADR-004-kafka-la-ha-tang-phase-1-master-data-core-sync-ing.md) | accepted | 2026-10-08 |
| ADR-005 | [Keycloak + OIDC + local credential cache cho Agent](ADR-005-keycloak-oidc-local-credential-cache-cho-agent.md) | accepted | 2026-10-08 |
| ADR-006 | [MongoDB cho audit trail và lưu payload gốc ticket (Phase 2)](ADR-006-mongodb-cho-audit-trail-va-luu-payload-goc-ticket-.md) | accepted | 2026-10-08 |
| ADR-007 | [Phân quyền (Authorization): RBAC có phạm vi, quyền lưu ở PostgreSQL](ADR-007-phan-quyen-authorization-rbac-co-pham-vi-quyen-luu.md) | accepted | 2026-10-08 |
| ADR-008 | [Cache: cache-aside, L1 Caffeine + L2 Redis](ADR-008-cache-cache-aside-l1-caffeine-l2-redis.md) | accepted | 2026-10-08 |
| ADR-009 | [Quy tắc đồng bộ master data (bổ sung cho ADR-002)](ADR-009-quy-tac-dong-bo-master-data-bo-sung-cho-adr-002.md) | accepted | 2026-10-08 |
| ADR-010 | [Observability: OpenTelemetry + Prometheus + Grafana + Loki](ADR-010-observability-opentelemetry-prometheus-grafana-lok.md) | accepted | 2026-10-08 |
| ADR-011 | [Event-carried state transfer từ core sang sync-service (thay vì sync-service đọc DB của core)](ADR-011-event-carried-state-transfer-tu-core-sang-sync-ser.md) | accepted | 2026-10-08 |
| ADR-012 | [Mỗi service tự validate JWT; quyền của Store Agent lấy từ claims trong token](ADR-012-moi-service-tu-validate-jwt-quyen-cua-store-agent-.md) | accepted | 2026-10-08 |

## Quy ước

- ADR đã accepted không sửa nội dung quyết định. Muốn đổi thì viết ADR mới, ghi "Supersedes ADR-0NN" và đổi trạng thái ADR cũ thành superseded.
- Số liệu ghi "giả định" là chưa đo; đo bằng k6 ở TASK-023 rồi cập nhật bằng ADR mới nếu kết quả đổi quyết định.
- Sơ đồ dùng Mermaid (GitHub tự hiển thị).
