# ADR-012: Mỗi service tự validate JWT; quyền của Store Agent lấy từ claims trong token

- Trạng thái: accepted
- Ngày: 2026-10-08
- Giai đoạn: Phase 1

## Bối cảnh
ADR-007 để token chỉ chứa sub và tra quyền trong PostgreSQL của module identity-access (nằm trong core-app). Với sync-service và order-ingest-service độc lập, gọi core mỗi request vừa phá nguyên tắc 'không gọi REST đồng bộ giữa service', vừa làm mất khả năng sống khi core chết. Hai service này chỉ phục vụ Store Agent (service account), có quyền cố định theo vai trò POS_AGENT.

## Quyết định
(1) MỖI service là OAuth2 resource server riêng: tự xác minh chữ ký bằng JWKS cache (jwk-set-uri, không dùng issuer-uri), kiểm tra iss, exp và aud của CHÍNH nó (core: central-api, sync: sync-api, ingest: ingest-api; token Agent chứa aud sync-api và ingest-api). (2) Quyền của Store Agent lấy từ claims: store_id (mapper gắn cứng theo client agent-store-{code}) và quyền sync:pull / ticket:ingest (client role hoặc scope cố định) nên không cần gọi core. (3) Quyền người dùng dashboard (grants theo store, ADR-007) vẫn chỉ ở core-app qua identity-access. (4) Gateway chỉ route, rate limit, giới hạn body và có thể kiểm tra thô; không thay thế validate ở service. (5) sync-service và ingest không mở API cho người dùng dashboard ở Phase 1; xem lag qua metric/Grafana.

## Phương án đã cân nhắc
- Gateway validate JWT một lần rồi tin header nội bộ: đơn giản nhưng service phía sau thành 'tin mạng nội bộ', đi ngược mTLS/zero trust; loại.
- sync/ingest gọi identity-access của core để tra quyền (có cache): thêm phụ thuộc đồng bộ vào core; loại.
- Nhân bản bảng grants sang mỗi service qua Kafka: nặng cho nhu cầu hiện tại; để dành nếu sau này có API người dùng ở sync/ingest.

## Hệ quả
- Lợi: mỗi service chạy độc lập khi core hoặc Keycloak chết (token còn hạn, JWKS cache); không gọi core mỗi request.
- Đánh đổi: thu hồi một store (tắt client Keycloak) chỉ có hiệu lực khi token hết hạn (tối đa 15 phút theo ADR-005, giả định) thay vì 60 giây (giả định, cần đo ở TASK-023) của cache grants; giảm bằng cách đặt token lifespan ngắn riêng cho client agent (ví dụ 5 phút, giả định) hoặc thêm denylist store qua Kafka nếu cần. Việc chặn theo thiết bị (device) không còn ở ingest: core-app (module orders) kiểm tra device thuộc store (Q-014).
- Rủi ro: cấu hình mapper/aud sai ở Keycloak làm cả ba service từ chối token: kiểm thử bằng token thật ở từng service (TASK-006, 028, 012); cấu hình validate lặp ở 3 service (chấp nhận trùng lặp, không dùng thư viện entity chung).

## Liên quan
- ADR-005
- ADR-007
- TASK-006
