# ADR-009: Quy tắc đồng bộ master data (bổ sung cho ADR-002)

- Trạng thái: accepted
- Ngày: 2026-10-08
- Giai đoạn: Phase 1

## Bối cảnh
Hai chiều dữ liệu, offline-first. Cần quy tắc để không phải giải quyết xung đột hai chiều. Chi tiết hóa cơ chế cursor của ADR-002.

## Quyết định
(1) Mỗi dữ liệu có đúng một bên được ghi: core-app (Master) sở hữu và ghi master data (catalog, assortment, giá), sync-service chỉ giữ bản chiếu đọc (ADR-011); store sở hữu ticket (ADR-003). Tồn Master do ticket tính ra (module orders), không đẩy xuống. (2) Mọi thực thể sync có version tăng đơn điệu; Agent bỏ qua version nhỏ hơn hoặc bằng cái đang có; sync-service cũng bỏ qua sự kiện có version cũ. (3) Xóa là tombstone (op = DELETE). (4) change_feed (sync-service) giữ 90 ngày; cursor cũ hơn thì trả 410 và Agent tải snapshot. (5) Ack chỉ tăng, idempotent, storeId lấy từ claim JWT (TASK-010, ADR-012). (6) Chu kỳ pull 30-60 giây (giả định, cần đo ở TASK-023), hasMore thì gọi tiếp ngay. (7) Thay đổi hàng loạt đi theo batch có giới hạn (xem kịch bản đổi Category). (8) Đọc an toàn change_feed (VIẾT LẠI): seq KHÔNG còn lấy từ sequence lúc INSERT trong nhiều transaction đồng thời. seq do MỘT writer duy nhất trong sync-service cấp, theo đúng thứ tự consume sự kiện MasterData; mỗi sự kiện được xử lý trong một transaction và commit xong mới xử lý sự kiện kế tiếp, nên thứ tự seq = thứ tự commit, feed đọc thẳng seq > since mà không bỏ sót và KHÔNG cần advisory lock. Bảo đảm single writer: sync-service chạy đúng 1 replica Phase 1, consumer concurrency = 1, writer cấp seq tường minh (lastSeq + 1, giữ trong bộ nhớ, khởi tạo từ max(seq) lúc khởi động) và PK trên seq nên hai writer vô tình chạy cùng lúc sẽ lỗi trùng khóa ngay (fail-fast) thay vì bỏ sót im lặng.

## Phương án đã cân nhắc
- Đồng bộ hai chiều với giải quyết xung đột (last-write-wins, CRDT)
- Đẩy tồn từ Master xuống store
- Không có tombstone mà so sánh toàn bộ

## Hệ quả
- Lợi: không có xung đột để giải; replay và phục hồi đơn giản; kiểm thử dễ.
- Rủi: store không sửa được master data tại chỗ (phải gửi yêu cầu lên Master); tồn cục bộ có thể lệch tạm thời.
- Kho: cần job purge change_feed và giám sát store có cursor sắp quá hạn; đối soát tồn Master với store là báo cáo, không tự động ghi đè.
- Đánh đổi của single writer: thông lượng ghi feed bị giới hạn bởi một luồng (đủ cho tần suất sửa master data thấp; chưa đo, đo ở TASK-023); sync-service là điểm đơn (RISK-015), nếu chết thì feed ngừng nhận thay đổi nhưng Kafka giữ sự kiện nên đuổi kịp khi lên lại; muốn scale ngang sau này phải shard theo store_id (mỗi shard một writer, seq theo shard), không thêm writer vào cùng một feed.
- Kho: mọi đường vào change_feed phải đi qua đúng một writer; ghi trực tiếp bằng SQL bỏ qua writer sẽ phá bảo đảm thứ tự.

## Liên quan
- ADR-002
- ADR-003
- ADR-011
- ADR-012
- RISK-015
- TASK-010
- TASK-023
