# ADR-004: Kafka là hạ tầng Phase 1: master data core -> sync, ingest, analytics, audit

- Trạng thái: accepted
- Ngày: 2026-10-08
- Giai đoạn: Phase 1

## Bối cảnh
Kiến trúc lai (ADR-001) cần kênh bất đồng bộ giữa các deployable: core -> sync-service (master data, ADR-011) và order-ingest-service -> core (PosOrder). Ingest tới core (Flow B) cần order per store, có DLQ, lag tăng thì scale. Đường Agent <-> Master (pull cursor) không được phụ thuộc Kafka: store offline vẫn bán, Kafka chết thì Agent vẫn kéo được phần đã có.

## Quyết định
Kafka (KRaft) là hạ tầng PHASE 1 (TASK-016 làm sớm, trước các task sync/ingest). Topic: MasterData (core -> sync, key = entityType:entityId, ~6 partition, giả định), PosOrder (ingest -> core, key = storeId, ~24 partition (giả định, cần đo ở TASK-023), giả định), Analytics, Audit (+ DLT). Số partition chọn theo năng lực consumer, KHÔNG theo số store; cần đo lại bằng load test. Agent không giao tiếp với Kafka. Kafka chết: sync feed không nhận thay đổi mới nhưng store vẫn kéo phần đã có; outbox của core tích lũy; ingest vẫn nhận ticket (outbox của ingest tích lũy); tất cả phát lại khi Kafka lên.

## Phương án đã cân nhắc
- Kafka cho tất cả (rủi: store offline không bán)
- Chỉ REST sync, không Kafka (analytics khó)
- Event sourcing = Kafka (quá phức tạp)

## Hệ quả
- Lợi: các service độc lập về runtime; analytics có event; DLQ + monitoring tốt.
- Rủi: Kafka là thành phần Phase 1 bắt buộc cho luồng master data tới sync-service; phải quản lý 2 dòng dữ liệu (cursor pull cho Agent, Kafka giữa service).
- Kho: phải monitor lag (consumer, outbox age), DLQ alert, replay event khi cần, contract test cho mọi topic.

## Liên quan
- ADR-001
- ADR-011
- TASK-016
