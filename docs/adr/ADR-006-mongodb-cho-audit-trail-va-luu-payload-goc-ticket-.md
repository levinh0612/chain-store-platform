# ADR-006: MongoDB cho audit trail và lưu payload gốc ticket (Phase 2)

- Trạng thái: accepted
- Ngày: 2026-10-08
- Giai đoạn: Phase 2

## Bối cảnh
Hai nhu cầu hợp document model: (1) audit trail (ai sửa gì, khi nào), cấu trúc khác nhau theo loại entity, ghi nhiều đọc ít; (2) lưu payload gốc ticket từ POS để đối soát và replay, mỗi phiên bản POS có thể có trường khác nhau. Đây cũng là chủ đích học MongoDB của project. PostgreSQL vẫn là nguồn sự thật cho đơn, tồn, giá.

## Quyết định
Thêm MongoDB làm kho phụ (không phải source of truth) từ Phase 2. Ghi vào MongoDB chỉ qua Kafka consumer (ADR-004), không ghi đồng bộ trong transaction nghiệp vụ. Consumer ghi MongoDB (AuditWriter, RawTicketWriter) đặt trong core-app (chọn, giả định chờ xác nhận ở Q-015): core là nơi có sales_order để đối soát và API reconciliation/replay, nên chỉ cần một chỗ biết MongoDB; order-ingest-service không phụ thuộc MongoDB. Payload gốc ticket lấy từ sự kiện TicketReceived trên PosOrder (ADR-003). Collection: audit_events (index entityType+entityId+at, TTL/partition theo tháng nếu cần), raw_tickets (unique storeId+ticketId để ghi idempotent, kèm posVersion, receivedAt, payload). MongoDB chết: bán hàng và đồng bộ vẫn chạy, audit/đối soát trễ, consumer giữ offset và ghi bù khi MongoDB lên lại. Production dùng replica set (tối thiểu 3 node), MVP có thể 1 node. Điều kiện xem lại/bỏ: vận hành MongoDB tốn hơn lợi ích, hoặc JSONB trong PostgreSQL đáp ứng đủ thì chuyển về PostgreSQL.

## Phương án đã cân nhắc
- JSONB trong PostgreSQL (ít thành phần vận hành hơn, có transaction chung; nhược: bảng audit/payload lớn làm phình DB nguồn sự thật, cần partition và vacuum. Đây là phương án quay về nếu bỏ MongoDB)
- Elasticsearch/OpenSearch (mạnh tìm kiếm full-text, nhưng không hợp làm kho lưu payload gốc, vận hành nặng hơn)
- File/S3 (rẻ, bền, nhưng khó truy vấn theo entity/thời gian, đối soát và replay phải tự dựng công cụ)

## Hệ quả
- Lợi: schema linh hoạt theo loại entity và phiên bản POS, ghi nhiều không đè tải PostgreSQL, hợp mục tiêu học.
- Rủi: thêm 1 hệ lưu trữ phải vận hành (backup, replica set, monitoring), nguy cơ lệch nguồn sự thật nếu dùng sai (xem RISK-011).
- Trigger xem lại: bỏ MongoDB, chuyển sang JSONB trong PostgreSQL nếu chi phí vận hành vượt lợi ích hoặc lệch dữ liệu so với PostgreSQL lặp lại (RISK-011).
- Kho: consumer ghi MongoDB phải idempotent (unique key), theo dõi lag riêng, có job đối chiếu số lượng với PostgreSQL.

## Liên quan
- ADR-003
- ADR-004
- RISK-011
