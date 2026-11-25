package com.rushseat.data.mapper;

import com.rushseat.business.domain.model.TicketType;
import com.rushseat.data.entity.TicketTypeEntity;

public final class TicketTypeMapper {

	private TicketTypeMapper() {

	}

	// Domain --> Persistence
	public static TicketTypeEntity toEntity(TicketType ticketType) {
		return new TicketTypeEntity(
				ticketType.getId(),
				ticketType.getEventId(),
				ticketType.getName(),
				ticketType.getDescription(),
				ticketType.getPrice(),
				ticketType.getTotalQuantity(),
				ticketType.getHeldQuantity(),
				ticketType.getSoldQuantity(),
				ticketType.getCreatedAt()
		);
	}

	// Persistence --> Domain
	public static TicketType toDomain(TicketTypeEntity ticketTypeEntity) {
		return TicketType.reconstitute(
				ticketTypeEntity.getId(),
				ticketTypeEntity.getEventId(),
				ticketTypeEntity.getName(),
				ticketTypeEntity.getDescription(),
				ticketTypeEntity.getPrice(),
				ticketTypeEntity.getTotalQuantity(),
				ticketTypeEntity.getHeldQuantity(),
				ticketTypeEntity.getSoldQuantity(),
				ticketTypeEntity.getCreatedAt()
		);
	}
}
