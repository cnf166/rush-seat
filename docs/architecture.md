# RushSeat — Tổng quan kiến trúc hệ thống

## 1. Mục đích

RushSeat là hệ thống backend phục vụ việc bán vé trong các đợt mở bán có lưu lượng truy cập cao và quản lý quá trình đặt vé.

Hệ thống cần ngăn chặn tình trạng bán vượt số vé hiện có (overselling), duy trì tính nhất quán của inventory, xử lý các request trùng lặp và giải phóng reservation hết hạn một cách chính xác.

Phiên bản ban đầu sử dụng kiến trúc modular monolith và PostgreSQL làm nguồn dữ liệu chính.

## 2. Kiến trúc tổng thể

RushSeat sử dụng layered architecture, phân tách rõ ràng giữa xử lý HTTP, nghiệp vụ, lưu trữ dữ liệu và tích hợp hệ thống bên ngoài.

```text
Client
  |
  v
API Layer
  |
  v
Business Layer
  |
  v
Data / Infrastructure Adapters
  |
  v
PostgreSQL
```

Business layer chịu trách nhiệm về các use case và quy tắc nghiệp vụ. Data và infrastructure adapters triển khai các interface cần thiết để business layer sử dụng mà không phụ thuộc trực tiếp vào framework lưu trữ hoặc messaging.

## 3. Trách nhiệm của từng layer

### API

Trách nhiệm:
- Tiếp nhận và xử lý HTTP request/response.
- Validate request DTO.
- Chuyển đổi request thành dữ liệu đầu vào cho business layer.
- Chuyển đổi lỗi ứng dụng thành HTTP response phù hợp.

API layer không chứa logic quản lý inventory và không thực hiện truy vấn database trực tiếp.

### Business

Trách nhiệm:
- Triển khai các use case của ứng dụng.
- Thực thi quy tắc vòng đời reservation.
- Định nghĩa domain model và các abstraction phục vụ nghiệp vụ.
- Điều phối các thao tác cần thành công hoặc thất bại cùng nhau.

Business layer không phụ thuộc trực tiếp vào Spring Data JPA, Hibernate, Redis client hoặc Kafka client.

### Data

Trách nhiệm:
- Định nghĩa persistence entity.
- Triển khai repository adapter cho database.
- Chuyển đổi giữa persistence entity và business model.
- Hỗ trợ các thao tác lưu trữ cần thực hiện trong transaction.

Database constraints và transaction phải bảo vệ các bất biến quan trọng của inventory.

### Infrastructure

Trách nhiệm:
- Tích hợp các hệ thống bên ngoài.
- Cung cấp adapter cho Redis, Kafka và payment khi có yêu cầu.
- Xử lý cấu hình và các tình huống lỗi đặc thù của hạ tầng.

Các thành phần infrastructure chỉ được bổ sung khi có yêu cầu kỹ thuật cụ thể chứng minh sự cần thiết.

### Security

Trách nhiệm:
- Xác thực người dùng.
- Thực thi phân quyền dựa trên vai trò.
- Bảo vệ tài nguyên theo quyền sở hữu và quyền truy cập.

Không được sử dụng resource ID như một cơ chế xác thực hoặc phân quyền.

### Config

Trách nhiệm:
- Khai báo cấu hình ứng dụng.
- Cấu hình bean và các điểm tích hợp hạ tầng.
- Tách biệt cấu hình theo môi trường khỏi business logic.

## 4. Công nghệ ban đầu

- Java 21
- Spring Boot 4.x
- Gradle Kotlin DSL
- Spring MVC
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway để quản lý database migration
- JUnit 5 và Mockito

Redis, Kafka, triển khai bằng Docker và hạ tầng cloud sẽ được bổ sung ở các giai đoạn sau.

## 5. Reservation và inventory

MVP sử dụng vé không gắn với ghế cụ thể (general admission).

Mỗi ticket type quản lý ba giá trị:
- `total_quantity`: tổng số vé được phép bán.
- `held_quantity`: số vé đang được giữ.
- `sold_quantity`: số vé đã bán thành công.

Số vé còn khả dụng được tính theo công thức:

`available_quantity = total_quantity - held_quantity - sold_quantity`

Bất biến sau phải luôn được đảm bảo:

`0 <= held_quantity + sold_quantity <= total_quantity`

Các thao tác chuyển trạng thái reservation và cập nhật inventory tương ứng phải có tính nguyên tử.

PostgreSQL là nguồn dữ liệu có thẩm quyền đối với trạng thái reservation và inventory được lưu trữ bền vững.

## 6. Tính đúng đắn và xử lý đồng thời

Hệ thống cần hỗ trợ:
- Cập nhật inventory an toàn bằng transaction.
- Ngăn chặn overselling.
- Tạo reservation có khả năng xử lý request trùng lặp.
- Xử lý chính xác việc confirm, cancel và expiration.
- Kiểm thử tích hợp các thao tác đồng thời.
- Benchmark hiệu năng có thể tái hiện.

Tính đúng đắn phải được xác lập trước khi đưa caching hoặc xử lý bất đồng bộ vào hệ thống.

## 7. Lộ trình phát triển kiến trúc

1. Triển khai REST API cốt lõi và vòng đời reservation.
2. Bổ sung PostgreSQL persistence và concurrency control.
3. Đánh giá việc sử dụng Redis để tối ưu reservation và hiệu năng.
4. Bổ sung payment simulator, idempotency và Kafka khi có lý do phù hợp.
5. Hoàn thiện integration tests, load tests, CI/CD, triển khai và tài liệu.

Mỗi thành phần hạ tầng mới phải có trách nhiệm rõ ràng, chiến lược xử lý lỗi và lợi ích có thể đo lường.
