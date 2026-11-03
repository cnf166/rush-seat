package com.rushseat.business.domain.model;


import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
	// Patern AAA
	// Arrange: prepare the input
	// Act: call the method that need to be tested
	// Assert: handle if the output matches the expectation.

	@Test
	void register_shouldCreateCustomerUser() {
		// Arrange
		String email = "email@mail.com";
		String passwordHash = "passwordHash";
		Instant createdAt = Instant.parse("2018-01-01T00:00:00.00Z");

		// Act
		User user = User.register(email, passwordHash, createdAt);

		// Assert
		assertEquals(email, user.getEmail());
		assertEquals(passwordHash, user.getPasswordHash());
		assertEquals(createdAt, user.getCreatedAt());
		assertEquals(UserRole.CUSTOMER, user.getRole());
		assertNull(user.getId());
	}

	@Test
	void register_shouldRejectNullEmail() {
		// Arrange
		String email = null;
		String passwordHash = "passwordHash";
		Instant createdAt = Instant.parse("2018-01-01T00:00:00.00Z");

		// Act & Assert
		assertThrows(IllegalArgumentException.class,
				() -> User.register(email, passwordHash, createdAt), "email should be not null");
	}

	@Test
	void register_shouldRejectBlankEmail() {
		// Arrange
		String email = " ";
		String passwordHash = "passwordHash";
		Instant createdAt = Instant.parse("2018-01-01T00:00:00.00Z");

		// Act & Assert
		assertThrows(IllegalArgumentException.class,
				() -> User.register(email, passwordHash, createdAt), "email should be not blank");
	}

	@Test
	void register_shouldRejectNullPasswordHash() {
		// Arrange
		String email = "email@mail.com";
		String passwordHash = null;
		Instant createdAt = Instant.parse("2018-01-01T00:00:00Z");

		// Act & Assert
		assertThrows(IllegalArgumentException.class,
				() -> User.register(email, passwordHash, createdAt), "password should be not null");
	}

	@Test
	void register_shouldRejectBlankPasswordHash() {
		// Arrange
		String email = "email@mail.com";
		String passwordHash = "    ";
		Instant createdAt = Instant.parse("2018-01-01T00:00:00Z");

		// Act & Assert
		assertThrows(IllegalArgumentException.class,
				() -> User.register(email, passwordHash, createdAt), "password should be not blank");
	}

	@Test
	void register_shouldRejectNullCreatedAt() {
		// Arrange
		String email = "email@mail.com";
		String passwordHash = "passwordHash";
		Instant createdAt = null;

		// Act & Assert
		assertThrows(NullPointerException.class,
				() -> User.register(email, passwordHash, createdAt), "createdAt should be not null");
	}

	@Test
	void reconstitute_shouldRestoreExistingUser() {
		// Arrange
		Long id = 1L;
		String email = "email@mail.com";
		String passwordHash = "passwordHash";
		UserRole role = UserRole.ORGANIZER;
		Instant createdAt = Instant.parse("2018-01-01T00:00:00.00Z");

		// Act
		User user = User.reconstitute(id, email, passwordHash, role, createdAt);

		// Assert
		assertEquals(id, user.getId());
		assertEquals(email, user.getEmail());
		assertEquals(passwordHash, user.getPasswordHash());
		assertEquals(role, user.getRole());
		assertEquals(createdAt, user.getCreatedAt());
	}

	@Test
	void changeRole_shouldUpdateUserRole() {
		// Arrange
		User user = User.register(
				"email@mail.com",
				"passwordHash",
				Instant.parse("2018-01-01T00:00:00Z")
		);

		UserRole newRole = UserRole.ORGANIZER;

		// Act & Assert
		user.changeRole(newRole);
		assertEquals(newRole, user.getRole());
	}

	@Test
	void changeRole_shouldRejectNullRole() {
		// Arrange
		User user = User.register(
				"email@mail.com",
				"passwordHash",
				Instant.parse("2018-01-01T00:00:00Z")
		);

		// Act & Assert
		UserRole role = null;
		assertThrows(NullPointerException.class,
				() -> user.changeRole(role));
	}
}
