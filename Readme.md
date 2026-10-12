# RushSeat

**High-Concurrency Flash-Sale & Seat Reservation System**

RushSeat là dự án backend Java tập trung vào việc xây dựng hệ thống đặt vé có khả năng xử lý nhiều request đồng thời, đảm bảo tính nhất quán của inventory, xử lý request trùng lặp và giải phóng reservation hết hạn chính xác.

Dự án nghiên cứu cách thiết kế và kiểm thử một hệ thống đặt vé có thể xử lý các request cạnh tranh mà không bán vượt số vé hiện có hoặc thực hiện cùng một nghiệp vụ nhiều lần.

## Mục tiêu

- Ngăn chặn overselling khi có nhiều request đồng thời.
- Triển khai vòng đời reservation với cơ chế expiration đáng tin cậy.
- Xử lý request trùng lặp bằng idempotency.
- Duy trì tính nhất quán của inventory thông qua PostgreSQL transaction.
- Đo lường hiệu năng bằng các bài load test có thể tái hiện.
- Ghi lại các quyết định kỹ thuật, trade-off và tình huống lỗi.

## Công nghệ triển khai

- **Language:** Java 21
- **Backend Framework:** Spring Boot 4.1.1, Spring MVC
- **Persistence:** Spring Data JPA, Hibernate
- **Database:** PostgreSQL
- **Testing:** JUnit 5, Mockito, Testcontainers
- **Performance Testing:** k6
- **Infrastructure:** Docker, Docker Compose
- **CI/CD:** GitHub Actions

Redis, Kafka, và AWS sẽ được triển khai ở giai đoạn sau. 

## Phạm vi MVP

- Authentication và authorization.
- Quản lý event và ticket type.
- Tra cứu số lượng vé còn khả dụng.
- Giữ vé tạm thời có thời hạn.
- Xác nhận và hủy reservation.
- Duy trì tính nhất quán của inventory và xử lý concurrency.
- Xử lý request tạo reservation trùng lặp.
- Kiểm thử tự động các bất biến nghiệp vụ quan trọng.

MVP ban đầu sử dụng vé không gắn với ghế cụ thể (general admission).

Thanh toán sẽ được mô phỏng và chỉ được bổ sung sau khi vòng đời reservation cốt lõi hoạt động ổn định.

## Kiến trúc

RushSeat bắt đầu bằng kiến trúc modular monolith, phân tách rõ trách nhiệm giữa các thành phần:

- `api`: REST controller, request/response DTO và xử lý lỗi HTTP.
- `business`: use case, domain model, business rules và abstraction.
- `data`: persistence entity, repository implementation và mapping.
- `infrastructure`: tích hợp Redis, Kafka và payment provider.
- `security`: authentication và authorization.
- `config`: cấu hình ứng dụng.

PostgreSQL là nguồn dữ liệu chính thức cho inventory và trạng thái reservation được lưu trữ bền vững.

Xem [Architecture Overview](docs/architecture.md) để biết chi tiết về kiến trúc và trách nhiệm của từng layer.

## Các bất biến nghiệp vụ cốt lõi

1. Số lượng vé khả dụng không được âm.
2. Tổng số vé đang giữ và đã bán không được vượt quá inventory.
3. Chỉ reservation hợp lệ, chưa hết hạn mới được confirm.
4. Request trùng lặp không được tạo ra tác động nghiệp vụ trùng lặp.
5. Trạng thái reservation và inventory phải nhất quán trong transaction.
6. Các tuyên bố về hiệu năng phải dựa trên benchmark có thể tái hiện.

## Lộ trình phát triển

1. Khởi tạo project, viết tài liệu và thiết kế REST API cốt lõi.
2. Thiết kế database schema và triển khai vòng đời reservation.
3. Triển khai transaction, locking và concurrency tests.
4. Đánh giá Redis để tối ưu reservation và xử lý tính nhất quán.
5. Bổ sung payment simulator, idempotency, Kafka events và xử lý lỗi.
6. Hoàn thiện integration tests, load tests, CI/CD, triển khai và tài liệu.

## Chạy project ở môi trường local

Yêu cầu:
- JDK 21.
- Gradle Wrapper được cung cấp trong repository.

Chạy test:

```bash
./gradlew clean test
```

Chạy ứng dụng:

```bash
./gradlew bootRun
```

## Trạng thái dự án

Đã hoàn thành khởi tạo project và tài liệu kiến trúc ban đầu.

Các chức năng nghiệp vụ sẽ được triển khai theo từng giai đoạn trong lộ trình phát triển.
