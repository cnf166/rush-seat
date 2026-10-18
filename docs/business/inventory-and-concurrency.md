# Quản lý tồn kho và xử lý đồng thời

## 1. Mục tiêu

Tài liệu mô tả cách RushSeat quản lý số lượng vé, bảo đảm không bán vượt số lượng và giữ dữ liệu nhất quán khi nhiều yêu cầu xảy ra đồng thời.

Đây là phần cốt lõi của hệ thống flash-sale.

## 2. Mô hình tồn kho

Mỗi `TicketType` có ba trường:

- `total_quantity`: Tổng số vé được tạo.
- `held_quantity`: Số vé đang được giữ.
- `sold_quantity`: Số vé đã được xác nhận.

Số lượng vé khả dụng:

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
| Customer hủy Reservation `HELD` | Giảm | Không đổi |
| Reservation `HELD` hết hạn | Giảm | Không đổi |
| Hủy Event với Reservation `HELD` | Giảm | Không đổi |
| Reservation `CONFIRMED` chuyển sang `CANCELLATION_PENDING` | Không đổi | Không đổi |
| Hoàn tất hủy Reservation đã xác nhận | Không đổi | Giảm theo chính sách |

Mỗi thay đổi tồn kho phải gắn với một chuyển đổi trạng thái hợp lệ và chỉ được thực hiện một lần.

## 4. Race condition khi giữ vé

Giả sử một `TicketType` chỉ còn một vé. Hai Customer gửi yêu cầu đặt vé gần như đồng thời.

Nếu cả hai yêu cầu cùng đọc `available_quantity = 1`, sau đó tự kiểm tra trong Java, cả hai có thể cùng cho rằng vẫn còn vé.

Kết quả là hệ thống có thể giữ hoặc bán vượt số lượng thực tế.

Do đó, phép kiểm tra và cập nhật tồn kho phải được bảo vệ bằng cơ chế đồng bộ hóa tại cơ sở dữ liệu.

## 5. Các hướng thiết kế

### 5.1. Khóa bản ghi

Khóa bản ghi `TicketType` trong lúc kiểm tra và cập nhật tồn kho.

Ưu điểm:
- Dễ hiểu về tính nhất quán.
- Phù hợp với PostgreSQL và transaction quan hệ.

Nhược điểm:
- Các yêu cầu cùng một TicketType phải chờ nhau.
- Có thể giảm throughput khi một loại vé nhận nhiều yêu cầu đồng thời.

### 5.2. Cập nhật có điều kiện

Thực hiện cập nhật chỉ khi điều kiện tồn kho vẫn thỏa mãn tại thời điểm cập nhật.

Ưu điểm:
- Có thể giảm khoảng thời gian giữa kiểm tra và cập nhật.
- Điều kiện tồn kho được kiểm tra tại thao tác cập nhật.

Nhược điểm:
- Cần xử lý trường hợp không có bản ghi nào được cập nhật.
- Khi một Reservation chứa nhiều TicketType, phải bảo đảm toàn bộ thao tác được thực hiện nhất quán.

### 5.3. Isolation level

Isolation level ảnh hưởng đến cách các transaction nhìn thấy và xử lý dữ liệu đồng thời.

Không nên cho rằng tăng isolation level tự động giải quyết mọi vấn đề. Thiết kế vẫn phải xác định các bất biến, cơ chế khóa hoặc cập nhật có điều kiện và cách xử lý lỗi khi xảy ra xung đột.

## 6. Ranh giới transaction

### 6.1. Tạo Reservation

Các thao tác sau phải nhất quán:

1. Kiểm tra và giữ tồn kho.
2. Tạo Reservation.
3. Tạo ReservationItem.
4. Commit dữ liệu.

Không được để tồn kho đã giữ nhưng không có Reservation hợp lệ, hoặc Reservation đã tạo thành công nhưng tồn kho chưa được giữ.

### 6.2. Xác nhận Reservation

Các thao tác sau phải nhất quán:

1. Kiểm tra trạng thái và thời hạn Reservation.
2. Kiểm tra Event vẫn cho phép xác nhận.
3. Chuyển Reservation sang `CONFIRMED`.
4. Giảm `held_quantity`.
5. Tăng `sold_quantity`.
6. Commit dữ liệu.

### 6.3. Hủy hoặc hết hạn Reservation

Đối với Reservation `HELD`:

1. Xác nhận Reservation vẫn ở `HELD`.
2. Kiểm tra điều kiện hủy hoặc hết hạn.
3. Chuyển sang `CANCELLED` hoặc `EXPIRED`.
4. Giảm `held_quantity`.
5. Commit dữ liệu.

### 6.4. Hủy Reservation đã xác nhận

Khi Event bị hủy, Reservation `CONFIRMED` chuyển sang `CANCELLATION_PENDING`.

Khi quy trình hủy hoàn tất, hệ thống cập nhật dữ liệu liên quan và giảm `sold_quantity` theo chính sách trước khi hoặc đồng thời với việc chuyển Reservation sang `CANCELLED`.

Các thay đổi phải được thực hiện sao cho không thể giải phóng tồn kho hai lần.

## 7. Reservation chứa nhiều TicketType

Một Reservation có thể chứa nhiều loại vé, nhưng tất cả TicketType phải thuộc cùng một Event.

Khi giữ nhiều loại vé, hệ thống cần tránh việc giữ thành công một phần rồi thất bại ở phần còn lại.

Nếu cập nhật nhiều TicketType trong cùng transaction, nên thống nhất thứ tự xử lý bản ghi để giảm nguy cơ deadlock.

## 8. Xử lý hết hạn

RushSeat sử dụng hai cơ chế phối hợp:

- Tác vụ nền định kỳ tìm và giải phóng vé của Reservation đã hết hạn.
- Use case xác nhận tự kiểm tra thời hạn và từ chối Reservation đã hết hạn.

Tác vụ nền phải kiểm tra trạng thái hiện tại trước khi giải phóng vé. Nếu Reservation đã được xác nhận hoặc hủy, tác vụ không được xử lý nó như một Reservation đang giữ vé.

Nếu tác vụ nền và yêu cầu xác nhận chạy đồng thời, chỉ một chuyển đổi trạng thái hợp lệ được phép commit.

## 9. Hủy Event

Khi Event bị hủy:

- Reservation `HELD` chuyển sang `CANCELLED` và giải phóng `held_quantity`.
- Reservation `CONFIRMED` chuyển sang `CANCELLATION_PENDING`.
- Khi hoàn tất cập nhật dữ liệu nội bộ, Reservation chuyển sang `CANCELLED` và `sold_quantity` được giảm theo chính sách.

Vé được giải phóng không được phép quay lại trạng thái có thể bán trong Event đã hủy.

## 10. Đo lường

Khi kiểm thử tải, nên theo dõi:

- Số yêu cầu tạo Reservation thành công và thất bại.
- Số trường hợp bán vượt tồn kho — mục tiêu là không có.
- Tỷ lệ lỗi khi có tải đồng thời.
- Độ trễ P95/P99.
- Thời gian chờ khóa.
- Số transaction thất bại do xung đột.
- Số Reservation đã hết hạn nhưng chưa được xử lý.

Không đưa ra số liệu throughput hoặc latency như kết quả thực tế trước khi có kiểm thử và phép đo.

## 11. Công nghệ trong MVP

PostgreSQL là nguồn dữ liệu có thẩm quyền về tồn kho.

Không cần đưa Redis hoặc Kafka vào chỉ để bảo đảm tính đúng đắn của việc giữ vé. Các công nghệ này chỉ nên được bổ sung khi có yêu cầu và lý do kỹ thuật cụ thể.

## 12. Ánh xạ package

- `api`: Nhận yêu cầu và chuyển đến use case.
- `business`: Định nghĩa quy tắc tồn kho và điều phối thay đổi trạng thái.
- `data`: Triển khai truy vấn, khóa, cập nhật có điều kiện và persistence.
- `infrastructure`: Cấu hình cơ sở dữ liệu, transaction và các thành phần hỗ trợ thực thi.
