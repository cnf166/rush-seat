package com.rushseat.data.adapter;

import com.rushseat.business.application.port.output.UserRepository;
import com.rushseat.business.domain.model.User;
import com.rushseat.data.entity.UserEntity;
import com.rushseat.data.mapper.UserMapper;
import com.rushseat.data.repository.UserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserPersistenceAdapter implements UserRepository {

	private final UserJpaRepository userJpaRepository;

	public UserPersistenceAdapter(UserJpaRepository userJpaRepository) {
		this.userJpaRepository = userJpaRepository;
	}

	@Override
	public User save(User user) {
		UserEntity userEntity = UserMapper.toEntity(user);

		UserEntity savedUserEntity = userJpaRepository.save(userEntity);
		return UserMapper.toDomain(savedUserEntity);
	}

	@Override
	public Optional<User> findById(Long id) {
		return userJpaRepository.findById(id)
				.map(UserMapper::toDomain);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return userJpaRepository.findByEmail(email)
				.map(UserMapper::toDomain);
	}

	@Override
	public boolean existsByEmail(String email) {
		return userJpaRepository.existsByEmail(email);
	}
}
