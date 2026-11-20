package com.rushseat.business.application.port.output;

import com.rushseat.business.domain.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository {

	Event save(Event event);

	Optional<Event> findById(Long id);

	List<Event> findAllByCreatedById(Long userId);

	List<Event> findAllPublished();

	boolean existsById(Long id);

}
