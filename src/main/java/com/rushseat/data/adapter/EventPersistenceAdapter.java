package com.rushseat.data.adapter;

import com.rushseat.business.application.port.output.EventRepository;
import com.rushseat.business.domain.enums.EventStatus;
import com.rushseat.business.domain.model.Event;
import com.rushseat.data.entity.EventEntity;
import com.rushseat.data.mapper.EventMapper;
import com.rushseat.data.repository.EventJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EventPersistenceAdapter implements EventRepository {
   private final EventJpaRepository eventJpaRepository;

	public EventPersistenceAdapter(EventJpaRepository eventJpaRepository) {
		this.eventJpaRepository = eventJpaRepository;
	}

	@Override
	public Event save(Event event) {
		EventEntity eventEntity = EventMapper.toEntity(event);

		EventEntity savedEventEntity = eventJpaRepository.save(eventEntity);
		return EventMapper.toDomain(savedEventEntity);
	}

	@Override
	public Optional<Event> findById(Long id) {
		return eventJpaRepository.findById(id)
				.map(EventMapper::toDomain);
	}

	@Override
	public List<Event> findAllByCreatedById(Long userId) {
		return eventJpaRepository.findAllByCreatedById(userId)
				.stream()
				.map(EventMapper::toDomain)
				.toList();
	}

	@Override
	public List<Event> findAllPublished() {
		return eventJpaRepository.findAllByStatus(EventStatus.PUBLISHED)
				.stream()
				.map(EventMapper::toDomain)
				.toList();
	}

	@Override
	public boolean existsById(Long id) {
		return eventJpaRepository.existsById(id);
	}
}
