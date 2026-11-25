package com.rushseat.data.repository;

import com.rushseat.data.entity.TicketTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketTypeJpaRepository extends JpaRepository<TicketTypeEntity, Long> {
	List<TicketTypeEntity> findAllByEventId(Long eventId);
}
