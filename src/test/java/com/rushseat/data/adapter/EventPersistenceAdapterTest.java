package com.rushseat.data.adapter;

import com.rushseat.business.application.port.output.EventRepository;
import com.rushseat.business.application.port.output.UserRepository;
import com.rushseat.business.domain.enums.EventStatus;
import com.rushseat.business.domain.model.Event;
import com.rushseat.business.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
		replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import({
		UserPersistenceAdapter.class,
		EventPersistenceAdapter.class
})
class EventPersistenceAdapterTest {

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private UserRepository userRepository;

	private User createOrganizer(String email) {
		User organizer = User.register(
				email,
				"hashed-password",
				Instant.parse("2026-11-20T08:00:00Z")
		);

		return userRepository.save(organizer);
	}

	private Event createEvent(Long organizerId, String name) {
		return Event.create(
				organizerId,
				name,
				"Test event description",
				Instant.parse("2026-12-20T08:00:00Z"),
				Instant.parse("2026-12-20T18:00:00Z"),
				Instant.parse("2026-11-20T08:00:00Z")
		);
	}

	@Test
	void shouldSaveAndRetrieveEvent() {
		// Arrange
		User organizer = createOrganizer("organizer1@rushseat.com");

		Event event = createEvent(
				organizer.getId(),
				"RushSeat Test Event"
		);

		// Act
		Event savedEvent = eventRepository.save(event);

		Optional<Event> retrieved =
				eventRepository.findById(savedEvent.getId());

		// Assert
		assertThat(savedEvent.getId()).isNotNull();

		assertThat(retrieved).isPresent();

		Event actual = retrieved.orElseThrow();

		assertThat(actual.getId()).isEqualTo(savedEvent.getId());
		assertThat(actual.getCreatedById()).isEqualTo(organizer.getId());
		assertThat(actual.getName()).isEqualTo("RushSeat Test Event");
		assertThat(actual.getDescription())
				.isEqualTo("Test event description");
		assertThat(actual.getStatus()).isEqualTo(EventStatus.DRAFT);
		assertThat(actual.getStartsAt())
				.isEqualTo(event.getStartsAt());
		assertThat(actual.getEndsAt())
				.isEqualTo(event.getEndsAt());
		assertThat(actual.getCreatedAt())
				.isEqualTo(event.getCreatedAt());
	}

	@Test
	void shouldFindEventsByCreatedById() {
		// Arrange
		User organizer1 = createOrganizer("organizer2@rushseat.com");
		User organizer2 = createOrganizer("organizer3@rushseat.com");

		eventRepository.save(
				createEvent(organizer1.getId(), "Event A")
		);

		eventRepository.save(
				createEvent(organizer1.getId(), "Event B")
		);

		eventRepository.save(
				createEvent(organizer2.getId(), "Event C")
		);

		// Act
		List<Event> events =
				eventRepository.findAllByCreatedById(organizer1.getId());

		// Assert
		assertThat(events).hasSize(2);
		assertThat(events)
				.extracting(Event::getName)
				.containsExactlyInAnyOrder("Event A", "Event B");

		assertThat(events)
				.allMatch(event ->
						event.getCreatedById().equals(organizer1.getId()));
	}

	@Test
	void shouldFindOnlyPublishedEvents() {
		// Arrange
		User organizer = createOrganizer("organizer4@rushseat.com");

		Event draftEvent = createEvent(
				organizer.getId(),
				"Draft Event"
		);

		Event publishedEvent = createEvent(
				organizer.getId(),
				"Published Event"
		);
		publishedEvent.publish();

		Event cancelledEvent = createEvent(
				organizer.getId(),
				"Cancelled Event"
		);
		cancelledEvent.cancel();

		eventRepository.save(draftEvent);
		eventRepository.save(publishedEvent);
		eventRepository.save(cancelledEvent);

		// Act
		List<Event> publishedEvents =
				eventRepository.findAllPublished();

		// Assert
		assertThat(publishedEvents).hasSize(1);

		assertThat(publishedEvents.getFirst().getName())
				.isEqualTo("Published Event");

		assertThat(publishedEvents.getFirst().getStatus())
				.isEqualTo(EventStatus.PUBLISHED);
	}

	@Test
	void shouldReturnEmptyWhenEventDoesNotExist() {
		assertThat(eventRepository.findById(999999L)).isEmpty();
	}

	@Test
	void shouldReturnTrueWhenEventExists() {
		// Arrange
		User organizer = createOrganizer("organizer5@rushseat.com");

		Event savedEvent = eventRepository.save(
				createEvent(organizer.getId(), "Existing Event")
		);

		// Act & Assert
		assertThat(eventRepository.existsById(savedEvent.getId()))
				.isTrue();

		assertThat(eventRepository.existsById(999999L))
				.isFalse();
	}
}
