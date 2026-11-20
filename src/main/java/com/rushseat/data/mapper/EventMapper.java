package com.rushseat.data.mapper;

import com.rushseat.business.domain.model.Event;
import com.rushseat.data.entity.EventEntity;

public final class EventMapper {
	private EventMapper() {

	}

	// Domain -> Persistence
	public static EventEntity toEntity(Event event) {
		return new EventEntity(
				event.getId(),
				event.getCreatedById(),
				event.getName(),
				event.getDescription(),
				event.getStatus(),
				event.getStartsAt(),
				event.getEndsAt(),
				event.getCreatedAt()
		);
	}

	// Persistence -> Domain
	public static Event toDomain(EventEntity eventEntity) {
		return Event.reconstitute(
				eventEntity.getId(),
				eventEntity.getCreatedById(),
				eventEntity.getName(),
				eventEntity.getDescription(),
				eventEntity.getStatus(),
				eventEntity.getStartsAt(),
				eventEntity.getEndsAt(),
				eventEntity.getCreatedAt()
		);
	}
}
