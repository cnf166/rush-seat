package com.rushseat.data.entity;

import com.rushseat.business.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
@Table(name = "users") // anh xa toi bang users
public class UserEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY )
	private Long id;

	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserRole role;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected UserEntity() {
		// init non-constructor - required by JPA (Hibernate using Select query, using Java reflection method
		// to create an empty object --> field all the value into column

		// using PROTECTED in non-constructor --> Best practice.
	}

	public UserEntity(Long id, String email, String passwordHash, UserRole role, Instant createdAt) {
		this.id = id;
		this.email = email;
		this.passwordHash = passwordHash;
		this.role = role;
		this.createdAt = createdAt;
	}
}
