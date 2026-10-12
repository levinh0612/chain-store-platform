# ADR-008: Cache: cache-aside, L1 Caffeine + L2 Redis

- Trạng thái: accepted
- Ngày: 2026-10-08
- Giai đoạn: Phase 1

## Bối cảnh
Một số dữ liệu đọc rất nhiều, ít đổi (grants của ADR-007, role-permission, JWKS của ADR-005, danh mục). Redis chỉ được dùng cho cache và rate limit.

## Quyết định
Cache-aside. L1 Caffeine trong tiến trình cho dữ liệu nhỏ, gần như tĩnh (role-permission, JWKS của từng service). L2 Redis cho dữ liệu dùng chung giữa các instance (grants của core-app, bộ lọc assortment, đếm rate limit ở gateway). TTL ngắn và xóa tường minh khi ghi. HTTP ETag cho danh sách lớn. Không cache kết quả ghi, tồn kho, hay bất cứ thứ gì mà sai là mất tiền. Không dùng Redis làm nơi lưu idempotency hay dữ liệu thật (đó là việc của ticket_inbox ở order-ingest-service, ADR-003). Mỗi service có cache riêng, không chia sẻ key space nghiệp vụ giữa service.

## Phương án đã cân nhắc
- Không cache
- Cache cấp 2 của Hibernate
- Dùng Redis làm nơi lưu idempotency/dữ liệu thật

## Hệ quả
- Lợi: giảm tải DB ở đường đọc nóng; Redis chết chỉ chậm hơn, không sai.
- Rủi: dữ liệu cũ tối đa bằng TTL; bài toán vô hiệu hóa cache; thêm một thành phần.
- Kho: đo hit ratio (cache_gets_total); mọi cache phải có cách chạy khi Redis chết; test 'Redis down' ở CI/STAGING.

## Liên quan
- ADR-003
- ADR-005
- ADR-007
