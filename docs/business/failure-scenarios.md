# Các tình huống lỗi và cách xử lý

## 1. Mục tiêu

Tài liệu liệt kê các tình huống lỗi có thể xảy ra trong những nghiệp vụ chính của RushSeat và xác định hành vi mà hệ thống cần bảo đảm.

Mục tiêu không phải là loại bỏ mọi lỗi, mà là bảo đảm lỗi không làm hỏng dữ liệu hoặc tạo ra trạng thái nghiệp vụ không hợp lệ.

## 2. Lỗi liên quan đến Event

### 2.1. Event không tồn tại

**Tình huống:** Customer hoặc Organizer gửi yêu cầu với `eventId` không hợp lệ.

**Yêu cầu xử lý:**
- Từ chối yêu cầu.
- Không tạo Reservation.
- Không thay đổi tồn kho.

### 2.2. Event chưa được công khai

**Tình huống:** Customer đặt vé cho Event ở trạng thái `DRAFT`.

**Yêu cầu xử lý:**
- Từ chối tạo Reservation.
- Không giữ vé.

### 2.3. Event đã bị hủy

**Tình huống:** Customer tạo hoặc xác nhận Reservation cho Event `CANCELLED`.

**Yêu cầu xử lý:**
- Từ chối thao tác.
- Không xác nhận Reservation.
- Không tạo Reservation mới.

### 2.4. Organizer không sở hữu Event

**Tình huống:** Organizer cố chỉnh sửa, công khai hoặc hủy Event của người khác.

**Yêu cầu xử lý:**
- Từ chối yêu cầu.
- Không thay đổi Event.
- Không dựa riêng vào role để quyết định quyền truy cập.

## 3. Lỗi liên quan đến tồn kho

### 3.1. Không đủ vé

**Tình huống:** Số lượng yêu cầu lớn hơn số vé khả dụng.

**Yêu cầu xử lý:**
- Từ chối giữ vé.
- Không tạo Reservation thành công.
- Không để lại thay đổi tồn kho một phần.

### 3.2. Hai yêu cầu cùng giữ vé cuối cùng

**Tình huống:** Nhiều yêu cầu đồng thời tranh cùng một TicketType.

**Yêu cầu xử lý:**
- Chỉ những yêu cầu thỏa điều kiện tồn kho tại thời điểm cập nhật mới được thành công.
- Không để `held_quantity + sold_quantity` vượt `total_quantity`.
- Xử lý xung đột hoặc transaction thất bại theo chính sách đã chọn.

### 3.3. Thất bại khi lưu ReservationItem

**Tình huống:** Tồn kho đã được cập nhật nhưng việc tạo ReservationItem thất bại.

**Yêu cầu xử lý:**
- Rollback các thay đổi thuộc cùng transaction.
- Không để tồn kho bị giữ mà không có Reservation hợp lệ.

## 4. Lỗi liên quan đến Reservation

### 4.1. Reservation đã hết hạn

**Tình huống:** Customer xác nhận sau `expiresAt`.

**Yêu cầu xử lý:**
- Không xác nhận Reservation.
- Reservation phải được xử lý theo quy tắc hết hạn.
- Không được tăng `sold_quantity` cho thao tác xác nhận thất bại.

### 4.2. Reservation đã được xác nhận

**Tình huống:** Customer gửi lại yêu cầu xác nhận.

**Yêu cầu xử lý:**
- Không thực hiện lại phép chuyển đổi tồn kho.
- Trả kết quả phù hợp với trạng thái hiện tại.

### 4.3. Hủy lặp lại

**Tình huống:** Hai yêu cầu cùng hủy một Reservation.

**Yêu cầu xử lý:**
- Chỉ một thao tác được phép thực hiện chuyển đổi trạng thái và giải phóng tồn kho.
- Không giảm tồn kho lần thứ hai.
- Xử lý yêu cầu còn lại theo trạng thái thực tế của Reservation.

### 4.4. Hết hạn đồng thời với xác nhận

**Tình huống:** Tác vụ nền hết hạn và Customer xác nhận gần như đồng thời.

**Yêu cầu xử lý:**
- Chỉ một chuyển đổi trạng thái hợp lệ được commit.
- Nếu Reservation đã hết hạn theo quy tắc thời gian, không được xác nhận thành công.
- Không vừa giảm `held_quantity` do hết hạn vừa giảm lần nữa do một thao tác khác.

## 5. Lỗi khi hủy Event

### 5.1. Hủy Event đồng thời với xác nhận Reservation

**Tình huống:** Một yêu cầu hủy Event và một yêu cầu xác nhận Reservation diễn ra đồng thời.

**Yêu cầu xử lý:**
- Phối hợp cập nhật trạng thái Event và Reservation bằng cơ chế đồng bộ hóa phù hợp.
- Không để Reservation được xác nhận sau khi Event đã bị hủy theo thứ tự xử lý được hệ thống bảo đảm.
- Xác định rõ transaction boundary và cách xử lý khi một thao tác thất bại.

### 5.2. Chỉ xử lý được một phần Reservation

**Tình huống:** Event đã chuyển sang `CANCELLED`, nhưng một số Reservation chưa được xử lý.

**Yêu cầu xử lý:**
- Nếu dùng một transaction, toàn bộ thay đổi thuộc transaction phải rollback khi thất bại.
- Nếu xử lý theo nhiều transaction, hệ thống phải ghi nhận phần việc chưa hoàn tất và có cách tiếp tục xử lý.
- Chạy lại không được giải phóng tồn kho nhiều lần.

### 5.3. Hủy Reservation đã xác nhận

**Tình huống:** Reservation chuyển sang `CANCELLATION_PENDING`, nhưng việc xử lý hủy chưa hoàn tất.

**Yêu cầu xử lý:**
- Không coi Reservation là đã hủy hoàn tất.
- Không tự động giả định hoàn tiền thành công.
- Chỉ chuyển sang `CANCELLED` khi điều kiện hoàn tất đã được đáp ứng.

## 6. Lỗi cơ sở dữ liệu

### 6.1. Transaction thất bại

**Yêu cầu xử lý:**
- Không trả về kết quả thành công nếu commit thất bại.
- Rollback các thay đổi thuộc transaction.
- Không giả định rằng thao tác đã thành công chỉ vì một câu lệnh trước đó trả về kết quả hợp lệ.

### 6.2. Deadlock hoặc xung đột đồng thời

**Yêu cầu xử lý:**
- Không bỏ qua lỗi.
- Chỉ retry các transaction phù hợp và bảo đảm thao tác có tính lặp an toàn.
- Giới hạn số lần retry.
- Ghi log đủ thông tin để điều tra mà không làm lộ thông tin nhạy cảm.

### 6.3. Mất kết nối sau khi gửi yêu cầu

**Tình huống:** Client không biết thao tác đã commit hay chưa vì kết nối bị ngắt.

**Yêu cầu xử lý:**
- Không mặc định rằng yêu cầu thất bại chỉ vì client không nhận được phản hồi.
- Khi cần bảo đảm retry an toàn cho thao tác tạo Reservation, phải thiết kế cơ chế nhận diện yêu cầu lặp.
- Cơ chế idempotency chưa thuộc phạm vi ban đầu và cần được thiết kế riêng trước khi tuyên bố xử lý được trường hợp này hoàn toàn.

## 7. Logging và quan sát

Các lỗi nghiệp vụ và kỹ thuật nên được phân biệt:

- Lỗi nghiệp vụ: hết vé, Event không hợp lệ, Reservation hết hạn, không đủ quyền.
- Lỗi kỹ thuật: lỗi cơ sở dữ liệu, deadlock, timeout, lỗi commit.

Log nên có các định danh cần thiết như `eventId`, `reservationId` và loại thao tác khi có sẵn. Không ghi mật khẩu, token hoặc dữ liệu xác thực nhạy cảm.

## 8. Ánh xạ package

- `api`: Chuyển lỗi nghiệp vụ và kỹ thuật thành phản hồi API phù hợp.
- `business`: Phát hiện vi phạm quy tắc và xác định hành vi nghiệp vụ.
- `data`: Xử lý lỗi persistence và cập nhật dữ liệu nhất quán.
- `infrastructure`: Cấu hình logging, transaction, retry và tác vụ nền.

## 9. Nguyên tắc chung

- Không trả thành công khi dữ liệu chưa được commit.
- Không để lỗi một phần tạo ra tồn kho không nhất quán.
- Không retry mù quáng các thao tác làm thay đổi trạng thái.
- Không dùng log thay cho cơ chế bảo đảm tính đúng đắn.
- Các tình huống race condition cần được kiểm thử bằng nhiều yêu cầu đồng thời, không chỉ bằng unit test thông thường.