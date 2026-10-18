# Các tình huống lỗi và cách xử lý

## 1. Mục tiêu

Tài liệu liệt kê các tình huống lỗi có thể xảy ra trong các nghiệp vụ chính của RushSeat và xác định hành vi mà hệ thống cần bảo đảm.

Mục tiêu không phải loại bỏ mọi lỗi, mà là bảo đảm lỗi không làm hỏng dữ liệu hoặc tạo ra trạng thái nghiệp vụ không hợp lệ.

## 2. Lỗi liên quan đến Event

### 2.1. Event không tồn tại

**Tình huống:** Người dùng gửi yêu cầu với `eventId` không hợp lệ.

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
- Kiểm tra cả vai trò và quyền sở hữu tài nguyên.

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
- Chỉ yêu cầu thỏa điều kiện tồn kho tại thời điểm cập nhật mới được giữ vé thành công.
- Không để `held_quantity + sold_quantity` vượt `total_quantity`.
- Xử lý xung đột hoặc transaction thất bại theo cơ chế đã chọn.

### 3.3. Thất bại khi lưu ReservationItem

**Tình huống:** Việc giữ tồn kho đã được thực hiện nhưng tạo ReservationItem thất bại.

**Yêu cầu xử lý:**
- Rollback các thay đổi thuộc cùng transaction.
- Không để tồn kho bị giữ mà không có Reservation hợp lệ.

## 4. Lỗi liên quan đến Reservation

### 4.1. Reservation đã hết hạn

**Tình huống:** Customer xác nhận khi thời điểm hiện tại lớn hơn hoặc bằng `expiresAt`.

**Yêu cầu xử lý:**
- Không xác nhận Reservation.
- Xử lý hết hạn và giải phóng vé theo cơ chế an toàn.
- Không tăng `sold_quantity` cho thao tác xác nhận thất bại.

### 4.2. Reservation đã được xác nhận

**Tình huống:** Customer gửi lại yêu cầu xác nhận.

**Yêu cầu xử lý:**
- Không thực hiện lại phép chuyển đổi tồn kho.
- Trả kết quả phù hợp với trạng thái hiện tại.

### 4.3. Customer tự hủy Reservation đã xác nhận

**Tình huống:** Customer gửi yêu cầu hủy Reservation ở trạng thái `CONFIRMED`.

**Yêu cầu xử lý:**
- Từ chối yêu cầu.
- Không thay đổi trạng thái Reservation.
- Không giảm `sold_quantity`.

Reservation đã xác nhận chỉ được đưa vào quy trình hủy khi có nghiệp vụ hợp lệ, chẳng hạn Event bị hủy.

### 4.4. Hủy lặp lại

**Tình huống:** Hai yêu cầu cùng hủy một Reservation đang `HELD`.

**Yêu cầu xử lý:**
- Chỉ một thao tác được phép thực hiện chuyển trạng thái và giải phóng tồn kho.
- Không giảm tồn kho lần thứ hai.
- Yêu cầu còn lại phải xử lý theo trạng thái thực tế của Reservation.

### 4.5. Hết hạn đồng thời với xác nhận

**Tình huống:** Tác vụ nền hết hạn và Customer xác nhận gần như đồng thời.

**Yêu cầu xử lý:**
- Chỉ một chuyển đổi trạng thái hợp lệ được commit.
- Reservation đã hết hạn không được xác nhận thành công.
- Không vừa giảm `held_quantity` do hết hạn vừa giảm lần nữa do thao tác khác.
- Nếu tác vụ nền chưa chạy, use case xác nhận vẫn phải kiểm tra `expiresAt`.

## 5. Lỗi khi hủy Event

### 5.1. Hủy Event đồng thời với xác nhận Reservation

**Tình huống:** Yêu cầu hủy Event và yêu cầu xác nhận Reservation diễn ra đồng thời.

**Yêu cầu xử lý:**
- Phối hợp cập nhật trạng thái Event và Reservation bằng cơ chế đồng bộ hóa phù hợp.
- Không để Reservation được xác nhận thành công khi Event đã bị hủy theo thứ tự xử lý được hệ thống bảo đảm.
- Xác định rõ transaction boundary và cách xử lý khi một thao tác thất bại.

### 5.2. Chỉ xử lý được một phần Reservation

**Tình huống:** Event đã chuyển sang `CANCELLED`, nhưng một số Reservation chưa được xử lý.

**Yêu cầu xử lý:**
- Nếu sử dụng một transaction cho toàn bộ quy trình, các thay đổi thuộc transaction phải rollback khi thất bại.
- Nếu xử lý theo nhiều transaction, hệ thống phải ghi nhận các Reservation chưa hoàn tất.
- Cần cơ chế tiếp tục xử lý và retry an toàn.
- Chạy lại không được giải phóng tồn kho nhiều lần.

### 5.3. Hủy Reservation đã xác nhận chưa hoàn tất

**Tình huống:** Reservation đã chuyển sang `CANCELLATION_PENDING`, nhưng cập nhật dữ liệu nội bộ chưa hoàn tất.

**Yêu cầu xử lý:**
- Không coi Reservation là đã hủy hoàn tất.
- Không giảm `sold_quantity` nhiều lần.
- Chỉ chuyển sang `CANCELLED` khi các cập nhật dữ liệu nội bộ cần thiết thành công.
- Không giả định rằng tiền đã được hoàn.

## 6. Lỗi khi xử lý Reservation hết hạn

### 6.1. Tác vụ nền chạy chậm

**Tình huống:** Reservation đã hết hạn nhưng tác vụ nền chưa chạy.

**Yêu cầu xử lý:**
- Use case xác nhận phải từ chối Reservation đã hết hạn.
- Tác vụ nền giải phóng vé khi xử lý Reservation đó.
- Không cho phép Reservation được xác nhận chỉ vì tác vụ nền chạy chậm.

### 6.2. Hai worker cùng xử lý một Reservation

**Tình huống:** Hai worker cùng tìm thấy một Reservation hết hạn.

**Yêu cầu xử lý:**
- Chỉ một worker được phép thực hiện chuyển đổi trạng thái và giải phóng vé.
- Worker còn lại không được giảm `held_quantity` lần nữa.
- Việc cập nhật cần có điều kiện hoặc cơ chế khóa phù hợp.

### 6.3. Transaction hết hạn thất bại

**Tình huống:** Cập nhật trạng thái hoặc tồn kho thất bại.

**Yêu cầu xử lý:**
- Rollback các thay đổi thuộc transaction.
- Không để Reservation và tồn kho mất nhất quán.
- Chỉ retry khi thao tác có tính lặp an toàn.
- Ghi nhận lỗi để có thể theo dõi và xử lý tiếp.

## 7. Lỗi cơ sở dữ liệu

### 7.1. Transaction thất bại

**Yêu cầu xử lý:**
- Không trả về kết quả thành công nếu commit thất bại.
- Rollback các thay đổi thuộc transaction.
- Không giả định rằng thao tác đã thành công chỉ vì một câu lệnh trước đó trả về kết quả hợp lệ.

### 7.2. Deadlock hoặc xung đột đồng thời

**Yêu cầu xử lý:**
- Không bỏ qua lỗi.
- Chỉ retry các transaction phù hợp và có cơ chế bảo đảm tính lặp an toàn.
- Giới hạn số lần retry.
- Ghi log đủ thông tin để điều tra mà không làm lộ dữ liệu nhạy cảm.

### 7.3. Mất kết nối sau khi gửi yêu cầu

**Tình huống:** Client không biết thao tác đã commit hay chưa vì kết nối bị ngắt.

**Yêu cầu xử lý:**
- Không mặc định rằng yêu cầu thất bại chỉ vì client không nhận được phản hồi.
- Với thao tác tạo Reservation, cần thiết kế cơ chế nhận diện yêu cầu lặp nếu muốn hỗ trợ retry an toàn.
- Idempotency chưa thuộc phạm vi ban đầu và cần được thiết kế riêng trước khi tuyên bố xử lý được hoàn toàn tình huống này.

## 8. Logging và quan sát

Các lỗi cần được phân biệt thành hai nhóm.

### Lỗi nghiệp vụ

Ví dụ:
- Không đủ vé.
- Event không hợp lệ.
- Reservation hết hạn.
- Không đủ quyền.
- Reservation không thể chuyển trạng thái.

### Lỗi kỹ thuật

Ví dụ:
- Lỗi cơ sở dữ liệu.
- Deadlock.
- Timeout.
- Lỗi commit.
- Lỗi khi tác vụ nền xử lý Reservation.

Log nên có các định danh cần thiết như `eventId`, `reservationId` và loại thao tác khi có sẵn.

Không ghi mật khẩu, token hoặc dữ liệu xác thực nhạy cảm.

## 9. Ánh xạ package

- `api`: Chuyển lỗi nghiệp vụ và kỹ thuật thành phản hồi API phù hợp.
- `business`: Phát hiện vi phạm quy tắc và xác định hành vi nghiệp vụ.
- `data`: Xử lý lỗi persistence và cập nhật dữ liệu nhất quán.
- `infrastructure`: Cấu hình logging, transaction, retry và tác vụ nền.

## 10. Nguyên tắc chung

- Không trả thành công khi dữ liệu chưa được commit.
- Không để lỗi một phần tạo ra tồn kho không nhất quán.
- Không retry mù quáng các thao tác làm thay đổi trạng thái.
- Không dùng log thay cho cơ chế bảo đảm tính đúng đắn.
- Các tình huống race condition cần được kiểm thử bằng nhiều yêu cầu đồng thời, không chỉ bằng unit test thông thường.
