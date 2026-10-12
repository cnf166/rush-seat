# RushSeat — Thiết kế cơ sở dữ liệu

## 1. Mục đích

Tài liệu này mô tả thiết kế cơ sở dữ liệu ban đầu cho RushSeat, hệ thống đặt vé có khả năng xử lý nhiều yêu cầu đồng thời.

Schema hỗ trợ quản lý người dùng, sự kiện, loại vé, số lượng vé và vòng đời reservation.

PostgreSQL là nguồn dữ liệu chính thức của hệ thống.

## 2. Các quyết định thiết kế

- Sử dụng `BIGINT GENERATED ALWAYS AS IDENTITY` làm khóa chính.
- Sử dụng `BIGINT` cho các khóa ngoại.
- Sử dụng `TIMESTAMPTZ` để lưu thời điểm.
- Sử dụng `NUMERIC(15, 0)` để biểu diễn tiền VND.
- MVP sử dụng vé không gắn với ghế cụ thể (general admission).
- Lưu các bộ đếm inventory trong bảng `ticket_types`.
- Thay đổi trạng thái reservation và inventory phải được thực hiện trong cùng một transaction.
- Chưa triển khai Redis, Kafka, orders, payments hoặc idempotency trong schema ban đầu.
- Chưa bổ sung các index phục vụ tối ưu hiệu năng khi chưa có workload cụ thể.

## 3. Quan hệ giữa các bảng

- Một người dùng có thể tạo nhiều sự kiện.
- Một người dùng có thể sở hữu nhiều reservation.
- Một sự kiện có thể có nhiều loại vé.
- Một reservation phải chứa ít nhất một reservation item.
- Mỗi reservation item tham chiếu đến một loại vé.

**Quy tắc nghiệp vụ:** Tất cả loại vé trong cùng một reservation phải thuộc cùng một sự kiện. Quy tắc này cần được kiểm tra trong business layer khi tạo reservation, bên trong transaction.

## 4. Thiết kế các bảng

### 4.1. Bảng `users`

Lưu thông tin tài khoản và quyền của người dùng.

| Cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `id` | BIGINT | Khóa chính, tự sinh |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE |
| `password_hash` | VARCHAR(255) | NOT NULL |
| `role` | VARCHAR(20) | NOT NULL, CHECK |
| `created_at` | TIMESTAMPTZ | NOT NULL |

Các giá trị hợp lệ của `role`:

- `CUSTOMER`
- `ADMIN`

Mật khẩu phải được lưu dưới dạng hash an toàn, không lưu plaintext.

### 4.2. Bảng `events`

Lưu thông tin sự kiện và thời gian tổ chức.

| Cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `id` | BIGINT | Khóa chính, tự sinh |
| `created_by` | BIGINT | NOT NULL, FK đến `users.id` |
| `name` | VARCHAR(200) | NOT NULL |
| `description` | TEXT | Có thể NULL |
| `status` | VARCHAR(20) | NOT NULL, CHECK |
| `starts_at` | TIMESTAMPTZ | NOT NULL |
| `ends_at` | TIMESTAMPTZ | NOT NULL |
| `created_at` | TIMESTAMPTZ | NOT NULL |

Các giá trị hợp lệ của `status`:

- `DRAFT`: Sự kiện đang được chuẩn bị.
- `PUBLISHED`: Sự kiện đã được công bố.
- `CANCELLED`: Sự kiện đã bị hủy.

Ràng buộc thời gian: `ends_at > starts_at`.

Chỉ sự kiện ở trạng thái `PUBLISHED`, chưa bắt đầu và chưa kết thúc mới được phép nhận reservation mới.

### 4.3. Bảng `ticket_types`

Lưu loại vé, giá vé và số lượng vé.

| Cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `id` | BIGINT | Khóa chính, tự sinh |
| `event_id` | BIGINT | NOT NULL, FK đến `events.id` |
| `name` | VARCHAR(100) | NOT NULL |
| `price` | NUMERIC(15, 0) | NOT NULL, CHECK |
| `total_quantity` | INTEGER | NOT NULL, CHECK |
| `held_quantity` | INTEGER | NOT NULL, mặc định 0, CHECK |
| `sold_quantity` | INTEGER | NOT NULL, mặc định 0, CHECK |

Các ràng buộc:

- `price >= 0`
- `total_quantity >= 0`
- `held_quantity >= 0`
- `sold_quantity >= 0`
- `held_quantity + sold_quantity <= total_quantity`
- `UNIQUE (event_id, name)`

Số vé còn khả dụng được tính theo công thức:

`available_quantity = total_quantity - held_quantity - sold_quantity`

Các bộ đếm inventory không được cập nhật độc lập với thao tác thay đổi trạng thái reservation tương ứng.

### 4.4. Bảng `reservations`

Lưu thông tin về một lần giữ vé của người dùng.

| Cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `id` | BIGINT | Khóa chính, tự sinh |
| `user_id` | BIGINT | NOT NULL, FK đến `users.id` |
| `status` | VARCHAR(20) | NOT NULL, CHECK |
| `expires_at` | TIMESTAMPTZ | NOT NULL |
| `created_at` | TIMESTAMPTZ | NOT NULL |
| `updated_at` | TIMESTAMPTZ | NOT NULL |

Các trạng thái hợp lệ:

- `HELD`: Reservation đang giữ vé.
- `CONFIRMED`: Reservation đã được xác nhận.
- `CANCELLED`: Reservation đã bị hủy.
- `EXPIRED`: Reservation đã hết hạn.

Thời gian hết hạn được xác định bởi server.

Chỉ reservation còn ở trạng thái `HELD` và chưa hết hạn mới được phép chuyển sang `CONFIRMED`. Khi reservation hết hạn, hệ thống phải giải phóng số vé đang giữ đúng một lần.

### 4.5. Bảng `reservation_items`

Lưu các loại vé và số lượng thuộc một reservation.

| Cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `id` | BIGINT | Khóa chính, tự sinh |
| `reservation_id` | BIGINT | NOT NULL, FK đến `reservations.id` |
| `ticket_type_id` | BIGINT | NOT NULL, FK đến `ticket_types.id` |
| `quantity` | INTEGER | NOT NULL, CHECK |
| `unit_price` | NUMERIC(15, 0) | NOT NULL, CHECK |

Các ràng buộc:

- `quantity > 0`
- `unit_price >= 0`
- `UNIQUE (reservation_id, ticket_type_id)`

`unit_price` lưu giá vé tại thời điểm tạo reservation. Giá này không thay đổi khi giá niêm yết của loại vé được cập nhật về sau.

Một reservation có thể chứa nhiều loại vé, nhưng tất cả phải thuộc cùng một sự kiện.

## 5. Các bất biến nghiệp vụ

Hệ thống phải luôn đảm bảo các điều kiện sau:

1. `held_quantity >= 0`.
2. `sold_quantity >= 0`.
3. `held_quantity + sold_quantity <= total_quantity`.
4. Mỗi reservation phải có ít nhất một item.
5. Mỗi reservation item phải tham chiếu đến một ticket type tồn tại.
6. Tất cả item trong cùng reservation phải thuộc cùng một sự kiện.
7. Thay đổi trạng thái reservation và inventory tương ứng phải được commit hoặc rollback cùng nhau.
8. Chỉ reservation hợp lệ, chưa hết hạn mới được confirm.

Các `CHECK` constraint bảo vệ những điều kiện nằm trong phạm vi một dòng dữ liệu. Các quy tắc liên quan đến nhiều dòng hoặc nhiều bảng cần được xử lý bằng transaction, locking và logic nghiệp vụ phù hợp.

## 6. Thay đổi inventory theo vòng đời reservation

| Thao tác | `held_quantity` | `sold_quantity` |
|---|---:|---:|
| Tạo reservation | Tăng | Không đổi |
| Confirm reservation | Giảm | Tăng |
| Hủy reservation đang giữ | Giảm | Không đổi |
| Reservation hết hạn | Giảm | Không đổi |

Mỗi thao tác chỉ được thực hiện khi reservation đang ở trạng thái phù hợp.

Việc gửi lại yêu cầu hủy hoặc xử lý expiration nhiều lần không được giải phóng vé nhiều hơn một lần.

## 7. Những nội dung chưa thuộc phạm vi

Schema ban đầu chưa bao gồm:

- Ghế được đánh số và sơ đồ chỗ ngồi.
- Bảng orders và payments.
- Idempotency key.
- Cấu trúc inventory trong Redis.
- Kafka events hoặc transactional outbox.
- Các index tối ưu hiệu năng ngoài những index tự sinh từ primary key và unique constraint.

Các index bổ sung sẽ được xem xét khi đã xác định truy vấn thực tế và workload.

## 8. Thứ tự triển khai

1. Review và chốt schema.
2. Tạo Flyway migration đầu tiên.
3. Cấu hình PostgreSQL và kết nối database.
4. Tạo persistence entities và repository adapters.
5. Triển khai transaction cho reservation và kiểm thử concurrency.
