package com.rushseat.business.domain.model;

import java.time.Instant;
import java.util.Objects;

public class User {
	private final Long id;
	private final String email;
	private final String passwordHash;
	private UserRole role;
	private final Instant createdAt;

	/**
	 * Core Domain Entity đại diện cho người dùng trong hệ sinh thái RushSeat.
	 *
	 * <p> Quy tắc bất biến (Invariants):
	 * <ul>
	 *   <li> Luôn ở trạng thái hợp lệ ngay khi được khởi tạo trong bộ nhớ (Always-Valid Entity).</li>
	 *   <li> Không thể khởi tạo trực tiếp bằng constructor; phải dùng Factory Methods để biểu đạt rõ ngữ cảnh.</li>
	 *   <li> Tách biệt hoàn toàn khỏi JPA/Hibernate annotations để giữ Domain thuần khiết.</li>
	 * </ul>
	 */


	// Constructor được đóng gói private để bắt buộc việc tạo thực thể phải đi qua
	// các Factory Method rõ nghĩa (register hoặc reconstitute), ngăn chặn việc
	// tùy tiện tạo đối tượng với dữ liệu nửa vời ở các layer bên ngoài.
	private User(Long id, String email, String passwordHash, UserRole role, Instant createdAt) {
		this.id = id;
		this.email = requireNonBlank(email, "Email");
		this.passwordHash = requireNonBlank(passwordHash, "Password Hash");
		this.role = Objects.requireNonNull(role, "Role must not be null");
		this.createdAt = Objects.requireNonNull(createdAt, "Created at must not be mull");
	}

	/**
	 * Dành riêng cho Use Case đăng ký tài khoản mới:
	 * - id = null (chờ Database tự sinh sequence/auto-increment).
	 * - role mặc định luôn là CUSTOMER.
	 */
	public static User register(String email, String passwordHash, Instant createdAt) {
		return new User(null, email, passwordHash, UserRole.CUSTOMER, createdAt);
	}

	/**
	 * Dành riêng cho Persistence Adapter / Data Mapper:
	 * Dùng để tái tạo (hydrate) lại Domain Entity từ Database lên bộ nhớ RAM.
	 * Bắt buộc phải có ID và mang đúng trạng thái/role đã lưu dưới DB.
	 */
	public static User reconstitute(Long id, String email, String passwordHash, UserRole role, Instant createdAt) {
		Objects.requireNonNull(id, "Id must not be null");

		return new User(id, email, passwordHash, role, createdAt);
	}

	// quản lý trạng thái đối với UserRole
	public void changeRole(UserRole newRole) {
		this.role = Objects.requireNonNull(newRole, "New role cannot be null");
	}

	private static String requireNonBlank(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " cannot be blank");
		}
		return value;
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public UserRole getRole() {
		return role;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
