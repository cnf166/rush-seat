package com.rushseat.data.mapper;

import com.rushseat.business.domain.model.User;
import com.rushseat.data.entity.UserEntity;

public final class UserMapper {
	private UserMapper() {

	}


	// down to DB
	public static UserEntity toEntity(User user) {
		return new UserEntity(
				user.getId(),
				user.getEmail(),
				user.getPasswordHash(),
				user.getRole(),
				user.getCreatedAt()
		);
	}

	// go up to business layer
	public static User toDomain(UserEntity userEntity) {
		return User.reconstitute(
				userEntity.getId(),
				userEntity.getEmail(),
				userEntity.getPasswordHash(),
				userEntity.getRole(),
				userEntity.getCreatedAt()
		);
	}
}
