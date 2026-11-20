package com.rushseat.data.repository;

import com.rushseat.business.domain.enums.EventStatus;
import com.rushseat.business.domain.model.Event;
import com.rushseat.data.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventJpaRepository extends JpaRepository<EventEntity, Long> {
	List<EventEntity> findAllByCreatedById(Long userId);

	List<EventEntity> findAllByStatus(EventStatus status);
}
