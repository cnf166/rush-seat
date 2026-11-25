package com.rushseat.business.application.port.output;

import com.rushseat.business.domain.model.TicketType;

import java.util.List;
import java.util.Optional;

public interface TicketTypeRepository {

	TicketType save(TicketType ticketType);

	Optional<TicketType> findById(Long id);

	List<TicketType> findAllByEventId(Long eventId);

	boolean existsById(Long id);
}
