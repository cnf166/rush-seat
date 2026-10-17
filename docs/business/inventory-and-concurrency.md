# Quản lý tồn kho và xử lý đồng thời

## 1. Mục tiêu

Tài liệu xác định cách RushSeat quản lý số lượng vé, bảo đảm không bán vượt số lượng và giữ dữ liệu nhất quán khi nhiều yêu cầu xảy ra đồng thời.

Đây là phần cốt lõi của hệ thống flash-sale.

## 2. Mô hình tồn kho

Mỗi `TicketType` có ba trường:

- `total_quantity`: Tổng số vé.
- `held_quantity`: Số vé đang được giữ.
- `sold_quantity`: Số vé đã xác nhận.

Công thức:

`available_quantity = total_quantity - held_quantity - sold_quantity`

Các bất biến:

- `total_quantity >= 0`
- `held_quantity >= 0`
- `sold_quantity >= 0`
- `held_quantity + sold_quantity <= total_quantity`

## 3. Tác động của các nghiệp vụ

| Nghiệp vụ | `held_quantity` | `sold_quantity` |
|---|---:|---:|
| Tạo Reservation | Tăng | Không đổi |
| Xác nhận Reservation | Giảm | Tăng |
| Hủy Reservation `HELD` | Giảm | Không đổi |
| Reservation `HELD` hết hạn | Giảm | Không đổi |
| Hoàn tất hủy Reservation đã xác nhận | Không đổi | Giảm theo chính sách |
| Hủy Event khi Reservation đang `CONFIRMED` | Chưa tự động giảm | Chưa tự động giảm |

Việc cập nhật phải dựa trên số lượng trong ReservationItem, không dựa trên số liệu do client gửi lên khi hủy hoặc xác nhận.

## 4. Vấn đề race condition

Giả sử còn một vé và hai Customer cùng gửi yêu cầu đặt vé.

Nếu cả hai yêu cầu đều đọc `available_quantity = 1`, rồi cùng kiểm tra trong Java, cả hai có thể cùng quyết định rằng còn vé.

Kết quả có thể là tổng số vé đang giữ vượt quá số lượng thực tế.

Do đó, phép kiểm tra và cập nhật tồn kho phải được bảo vệ bằng cơ chế đồng bộ hóa ở tầng cơ sở dữ liệu.

## 5. Các hướng thiết kế

### 5.1. Khóa bản ghi

Khóa bản ghi TicketType trong lúc kiểm tra và cập nhật tồn kho.

Ưu điểm:
- Dễ hiểu về tính nhất quán.
- Phù hợp với PostgreSQL và giao dịch quan hệ.

Nhược điểm:
- Các yêu cầu cùng TicketType phải chờ nhau.
- Có thể giảm throughput khi một loại vé nhận lượng lớn yêu cầu.

### 5.2. Cập nhật có điều kiện

Thực hiện phép cập nhật chỉ khi điều kiện tồn kho vẫn thỏa mãn tại thời điểm cập nhật.

Ưu điểm:
- Có thể giảm khoảng thời gian giữa kiểm tra và cập nhật.
- Điều kiện hợp lệ được kiểm tra ngay tại thao tác cập nhật.

Nhược điểm:
- Cần xử lý trường hợp không có bản ghi nào được cập nhật.
- Luồng cập nhật nhiều TicketType cần được thiết kế cẩn thận.

### 5.3. Isolation level

Isolation level ảnh hưởng đến cách các transaction nhìn thấy và xử lý dữ liệu đồng thời.

Không nên cho rằng tăng isolation level tự động giải quyết mọi vấn đề. Thiết kế vẫn phải xác định các bất biến, cơ chế khóa hoặc cập nhật có điều kiện và cách xử lý lỗi khi xung đột.

## 6. Ranh giới transaction

Đối với tạo Reservation, các thao tác sau phải nhất quán:

1. Kiểm tra và giữ tồn kho.
2. Tạo Reservation.
3. Tạo ReservationItem.
4. Commit dữ liệu.

Đối với xác nhận Reservation:

1. Kiểm tra trạng thái và thời hạn.
2. Chuyển trạng thái Reservation.
3. Giảm `held_quantity`.
4. Tăng `sold_quantity`.
5. Commit dữ liệu.

Nếu một bước bắt buộc thất bại, các thay đổi thuộc cùng giao dịch phải được rollback.

## 7. Xử lý nhiều loại vé

Một Reservation có thể chứa nhiều TicketType nhưng tất cả phải thuộc cùng Event.

Khi giữ nhiều loại vé, hệ thống cần tránh tình huống giữ thành công một phần số lượng nhưng thất bại ở phần còn lại.

Nếu cập nhật nhiều TicketType trong cùng transaction, nên thống nhất thứ tự xử lý các bản ghi để giảm nguy cơ deadlock.

## 8. Hủy và hết hạn

Các thao tác giải phóng tồn kho phải gắn với chuyển trạng thái hợp lệ.

Không được giảm `held_quantity` chỉ vì một yêu cầu hủy đến. Hệ thống phải xác định rằng Reservation thực sự chuyển từ `HELD` sang trạng thái không còn giữ vé.

Tương tự, `sold_quantity` chỉ được giảm khi reservation đã xác nhận được xử lý hủy theo chính sách.

## 9. Đo lường

Khi triển khai kiểm thử tải, nên theo dõi:

- Số yêu cầu tạo Reservation thành công và thất bại.
- Số lượng vé bị bán vượt nếu có — mục tiêu là không có.
- Tỷ lệ lỗi khi có tải đồng thời.
- Độ trễ P95/P99.
- Thời gian chờ khóa và số lần transaction thất bại do xung đột.
- Số Reservation hết hạn nhưng chưa được xử lý.

Không nên đưa ra con số throughput hoặc latency mục tiêu như kết quả đã đạt được trước khi có phép đo thực tế.

## 10. Ánh xạ package

- `api`: Nhận yêu cầu và chuyển đến use case.
- `business`: Định nghĩa quy tắc tồn kho và điều phối thay đổi trạng thái.
- `data`: Triển khai khóa, cập nhật có điều kiện, truy vấn và transaction persistence.
- `infrastructure`: Cấu hình cơ sở dữ liệu và các thành phần hỗ trợ thực thi.

Redis và Kafka chưa cần thiết để bảo đảm tính đúng đắn của MVP. PostgreSQL phải tiếp tục là nguồn dữ liệu có thẩm quyền về tồn kho.
