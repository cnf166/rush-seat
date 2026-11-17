package com.rushseat.business.application.port.output;

import com.rushseat.business.domain.model.User;

import java.util.Optional;

public interface UserRepository {

	User save(User user);

	Optional<User> findById(Long id);

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);
}
