# RushSeat — Tổng quan nghiệp vụ

## 1. Giới thiệu

RushSeat là hệ thống đặt vé trực tuyến hướng đến các sự kiện có nhu cầu đặt vé tập trung trong thời gian ngắn, chẳng hạn như chương trình biểu diễn, hội thảo hoặc sự kiện giải trí.

Hệ thống tập trung giải quyết bài toán quản lý số lượng vé và reservation trong điều kiện nhiều người dùng có thể gửi yêu cầu đồng thời. Mục tiêu là ngăn việc bán vượt số lượng vé, quản lý thời gian giữ chỗ và bảo đảm các thao tác xác nhận hoặc hủy reservation không làm sai lệch dữ liệu.

Trong MVP, RushSeat sử dụng mô hình vé không gán ghế cụ thể (*general admission*). Mỗi loại vé thuộc một sự kiện và có số lượng giới hạn.

## 2. Các tác nhân

### 2.1. Customer

Customer là người sử dụng hệ thống để tìm kiếm và đặt vé.

Các nghiệp vụ chính:

- Xem thông tin sự kiện đã công khai.
- Xem các loại vé và số lượng vé còn khả dụng.
- Tạo reservation khi sự kiện đang mở bán.
- Xác nhận reservation đang giữ chỗ.
- Hủy reservation theo các điều kiện nghiệp vụ được cho phép.
- Theo dõi trạng thái reservation của bản thân.

Customer chỉ được quản lý reservation thuộc quyền sở hữu của mình.

### 2.2. Organizer

Organizer là người quản lý các sự kiện do mình sở hữu.

Các nghiệp vụ chính:

- Tạo sự kiện ở trạng thái `DRAFT`.
- Chỉnh sửa thông tin sự kiện theo các điều kiện nghiệp vụ.
- Tạo và quản lý các loại vé.
- Công khai sự kiện bằng cách chuyển trạng thái từ `DRAFT` sang `PUBLISHED`.
- Hủy sự kiện thuộc quyền sở hữu của mình.

Organizer không được chỉnh sửa hoặc hủy sự kiện thuộc sở hữu của Organizer khác.

### 2.3. Admin

Admin là người quản trị hệ thống.

Các nghiệp vụ chính:

- Quản lý sự kiện trên phạm vi toàn hệ thống.
- Hủy sự kiện theo quyền quản trị.
- Thực hiện các tác vụ quản trị khác khi được bổ sung vào phạm vi sản phẩm.

Quyền quản trị toàn hệ thống không loại bỏ yêu cầu kiểm tra tính hợp lệ của các thao tác nghiệp vụ.

## 3. Phạm vi MVP

### 3.1. Chức năng thuộc phạm vi

- Quản lý người dùng và vai trò.
- Tạo, chỉnh sửa, công khai và hủy sự kiện.
- Quản lý loại vé và số lượng vé.
- Tạo reservation để giữ vé trong một khoảng thời gian.
- Xác nhận reservation.
- Hủy reservation.
- Xử lý reservation hết hạn.
- Bảo đảm tính nhất quán của tồn kho khi có nhiều yêu cầu đồng thời.
- Quản lý trạng thái reservation khi sự kiện bị hủy.

### 3.2. Chức năng chưa thuộc phạm vi

- Tích hợp cổng thanh toán thực tế.
- Quy trình hoàn tiền tự động.
- Gán ghế cụ thể và sơ đồ chỗ ngồi.
- Hệ thống phân tán dựa trên Kafka hoặc Redis ngay từ đầu.
- Các cơ chế chống gian lận hoặc hệ thống xếp hàng nâng cao.

Những chức năng này có thể được xem xét sau khi các nghiệp vụ cốt lõi hoạt động đúng và có kiểm thử phù hợp.

## 4. Các khái niệm nghiệp vụ chính

### 4.1. Event

Event đại diện cho một sự kiện được tổ chức và có thể mở bán vé.

Một Event có thông tin như tên, mô tả, thời gian bắt đầu, thời gian kết thúc, người sở hữu và trạng thái.

Trong MVP, `starts_at` được sử dụng làm mốc bắt đầu cho phép đặt vé. Hệ thống chỉ chấp nhận reservation khi Event ở trạng thái `PUBLISHED`, thời điểm hiện tại không sớm hơn `starts_at` và chưa đạt `ends_at`.

Nếu sau này thời điểm mở bán khác thời điểm bắt đầu sự kiện, cần bổ sung trường thời gian mở bán riêng.

### 4.2. TicketType

TicketType đại diện cho một loại vé của Event, ví dụ vé tiêu chuẩn hoặc vé ưu tiên.

Mỗi TicketType có giá và số lượng giới hạn. Hệ thống theo dõi số lượng vé đang được giữ, đã bán và còn khả dụng.

### 4.3. Reservation

Reservation đại diện cho yêu cầu giữ hoặc xác nhận vé của một Customer.

Reservation có thể chứa nhiều loại vé, nhưng trong MVP tất cả các loại vé trong cùng một reservation phải thuộc cùng một Event.

Reservation có thời hạn giữ chỗ. Thời hạn mặc định là 5 phút, tính từ lúc reservation được tạo.

### 4.4. ReservationItem

ReservationItem biểu diễn một dòng vé trong Reservation, bao gồm loại vé, số lượng và giá tại thời điểm giữ vé.

Giá được lưu lại nhằm bảo đảm reservation giữ nguyên mức giá đã áp dụng tại thời điểm tạo, ngay cả khi giá vé thay đổi sau đó.

## 5. Luồng nghiệp vụ tổng quát

### 5.1. Tổ chức sự kiện

1. Organizer tạo Event ở trạng thái `DRAFT`.
2. Organizer thiết lập thông tin sự kiện và các loại vé.
3. Organizer công khai Event, chuyển trạng thái sang `PUBLISHED`.
4. Khi đến thời điểm mở bán, Customer có thể tạo reservation.

### 5.2. Đặt và xác nhận vé

1. Customer chọn loại vé và số lượng cần đặt.
2. Hệ thống kiểm tra Event có cho phép đặt vé hay không.
3. Hệ thống kiểm tra số lượng vé khả dụng.
4. Nếu hợp lệ, hệ thống giữ vé và tạo Reservation cùng các ReservationItem.
5. Reservation bắt đầu ở trạng thái `HELD`.
6. Customer xác nhận trong thời hạn giữ chỗ.
7. Nếu xác nhận thành công, Reservation chuyển sang `CONFIRMED`; số vé tương ứng được chuyển từ lượng đang giữ sang lượng đã bán.
8. Nếu hết thời hạn mà chưa xác nhận, Reservation được xử lý hết hạn và số vé đang giữ được giải phóng.

### 5.3. Hủy reservation

Reservation đang `HELD` có thể chuyển sang `CANCELLED` theo quy tắc hủy tương ứng và phải giải phóng số vé đã giữ.

Reservation đã `CONFIRMED` được xử lý theo chính sách hủy được phép. Khi sự kiện bị hủy, reservation đã xác nhận chuyển sang `CANCELLATION_PENDING`, sau đó chuyển sang `CANCELLED` khi quy trình xử lý hủy hoàn tất.

### 5.4. Hủy sự kiện

1. Organizer hoặc Admin gửi yêu cầu hủy Event.
2. Hệ thống kiểm tra quyền và tính hợp lệ của yêu cầu.
3. Event chuyển sang `CANCELLED`.
4. Reservation ở trạng thái `HELD` được chuyển sang `CANCELLED` và giải phóng lượng vé đang giữ.
5. Reservation ở trạng thái `CONFIRMED` chuyển sang `CANCELLATION_PENDING`.
6. Sau khi xử lý hủy hoàn tất, reservation chuyển sang `CANCELLED`.

MVP chưa tích hợp cổng thanh toán. Vì vậy, trạng thái `CANCELLED` không đồng nghĩa với việc tiền đã được hoàn.

## 6. Nguyên tắc nghiệp vụ quan trọng

### 6.1. Không bán vượt số lượng vé

Số vé khả dụng được xác định bằng:

`available_quantity = total_quantity - held_quantity - sold_quantity`

Tại mọi thời điểm, hệ thống phải bảo đảm:

- `held_quantity >= 0`
- `sold_quantity >= 0`
- `held_quantity + sold_quantity <= total_quantity`

Việc kiểm tra tồn kho phải an toàn trước các yêu cầu đồng thời. Chỉ kiểm tra số lượng trong bộ nhớ của ứng dụng là chưa đủ.

### 6.2. Giá vé được xác định ở phía máy chủ

Customer chỉ gửi loại vé và số lượng cần đặt. Hệ thống tự xác định giá từ dữ liệu hiện có và lưu giá vào ReservationItem.

Không tin tưởng giá tiền do client gửi lên.

### 6.3. Trạng thái phải chuyển theo quy tắc

Reservation không được chuyển tùy ý giữa các trạng thái. Mỗi chuyển đổi cần có điều kiện hợp lệ và các thay đổi tồn kho tương ứng.

### 6.4. Quyền truy cập gắn với tài nguyên

Việc xác thực người dùng và kiểm tra vai trò không thay thế việc kiểm tra quyền sở hữu Event hoặc Reservation.

### 6.5. Hủy reservation không đồng nghĩa với hoàn tiền

Trạng thái Reservation mô tả vòng đời đặt vé. Nếu bổ sung thanh toán, vòng đời giao dịch và hoàn tiền phải được quản lý riêng.

## 7. Liên kết tài liệu

- `domain-model.md`: Mô hình miền, quan hệ giữa các đối tượng và các bất biến nghiệp vụ.
- `reservation-lifecycle.md`: Vòng đời Reservation và các chuyển đổi trạng thái.
- `use-cases/create-reservation.md`: Nghiệp vụ tạo Reservation.
- `use-cases/confirm-reservation.md`: Nghiệp vụ xác nhận Reservation.
- `use-cases/cancel-reservation.md`: Nghiệp vụ hủy Reservation.
- `use-cases/expire-reservation.md`: Nghiệp vụ xử lý Reservation hết hạn.
- `use-cases/cancel-event.md`: Nghiệp vụ hủy Event và xử lý các Reservation liên quan.
- `use-cases/publish-event.md`: Nghiệp vụ công khai sự kiện.
- `inventory-and-concurrency.md`: Quản lý tồn kho và xử lý đồng thời.
- `failure-scenarios.md`: Các tình huống lỗi và cách hệ thống cần phản ứng.
