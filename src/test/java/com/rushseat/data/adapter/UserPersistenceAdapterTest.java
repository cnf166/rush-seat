
package com.rushseat.data.adapter;

import com.rushseat.business.application.port.output.UserRepository;
import com.rushseat.business.domain.model.User;
import com.rushseat.business.domain.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
		replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import(UserPersistenceAdapter.class)
class UserPersistenceAdapterTest {

	@Autowired
	private UserRepository userRepository;

	@Test
	void shouldSaveAndRetrieveUser() {
		Instant createdAt = Instant.parse("2026-11-17T08:00:00Z");

		User user = User.register(
				"test@rushseat.com",
				"hashed-password",
				createdAt
		);

		User savedUser = userRepository.save(user);

		assertThat(savedUser.getId()).isNotNull();
		assertThat(savedUser.getEmail()).isEqualTo(user.getEmail());
		assertThat(savedUser.getPasswordHash())
				.isEqualTo(user.getPasswordHash());
		assertThat(savedUser.getRole()).isEqualTo(UserRole.CUSTOMER);
		assertThat(savedUser.getCreatedAt()).isEqualTo(createdAt);

		Optional<User> retrieved =
				userRepository.findById(savedUser.getId());

		assertThat(retrieved).isPresent();
		assertThat(retrieved.get().getId()).isEqualTo(savedUser.getId());
		assertThat(retrieved.get().getEmail())
				.isEqualTo("test@rushseat.com");
	}

	@Test
	void shouldFindUserByEmail() {
		User user = User.register(
				"find@rushseat.com",
				"hashed-password",
				Instant.parse("2026-11-17T08:00:00Z")
		);

		User savedUser = userRepository.save(user);

		Optional<User> retrieved =
				userRepository.findByEmail("find@rushseat.com");

		assertThat(retrieved).isPresent();
		assertThat(retrieved.get().getId())
				.isEqualTo(savedUser.getId());
	}

	@Test
	void shouldReturnTrueWhenEmailExists() {
		User user = User.register(
				"exists@rushseat.com",
				"hashed-password",
				Instant.parse("2026-11-17T08:00:00Z")
		);

		userRepository.save(user);

		assertThat(userRepository.existsByEmail("exists@rushseat.com"))
				.isTrue();

		assertThat(userRepository.existsByEmail("unknown@rushseat.com"))
				.isFalse();
	}

	@Test
	void shouldReturnEmptyWhenUserDoesNotExist() {
		assertThat(userRepository.findById(999999L)).isEmpty();
		assertThat(userRepository.findByEmail("missing@rushseat.com"))
				.isEmpty();
	}
}
