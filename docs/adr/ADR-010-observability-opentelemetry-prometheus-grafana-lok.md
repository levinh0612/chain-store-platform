# ADR-010: Observability: OpenTelemetry + Prometheus + Grafana + Loki

- Trạng thái: accepted
- Ngày: 2026-10-08
- Giai đoạn: Phase 1

## Bối cảnh
Luồng đi qua nhiều thành phần và nhiều service (Agent, gateway, core-app, sync-service, order-ingest-service, PostgreSQL, Kafka, consumer). Một người vận hành, cần tìm nguyên nhân nhanh và biết hỏng trước khi store báo.

## Quyết định
OpenTelemetry cho trace/metric/log context; Prometheus + Alertmanager + Grafana; Loki cho log JSON; Tempo hoặc Jaeger cho trace. Mỗi service có health (/health/liveness, /health/readiness) và /metrics riêng trên cổng quản lý. Cảnh báo dựa trên SLO (burn rate) và metric nghiệp vụ (sync lag, master_event_lag, ingest freshness, outbox age, DLT). Trace xuyên service: truyền traceparent và correlationId qua HTTP, outbox và Kafka headers (ADR-003, ADR-004), mỗi service đặt lại MDC từ header. Triển khai ở TASK-022 và TASK-029.

## Phương án đã cân nhắc
- ELK
- Dịch vụ SaaS (Datadog...) tốn tiền
- Chỉ dùng log

## Hệ quả
- Lợi: mã nguồn mở, chạy được bằng Compose, chuẩn mở dễ đổi backend.
- Rủi: thêm 4-5 container cần tài nguyên; phải kiểm soát cardinality và chi phí lưu trữ.
- Kho: profile Compose observability, dashboard và rule nằm trong git; mọi module phải tạo metric nghiệp vụ của mình (đưa vào Definition of Done).

## Liên quan
- ADR-003
- ADR-004
- TASK-022
- TASK-029
