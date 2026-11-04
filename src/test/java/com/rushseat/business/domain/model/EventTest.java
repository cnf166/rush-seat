package com.rushseat.business.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

	private Long createdById;
	private String name;
	private String description;
	private Instant startsAt;
	private Instant endsAt;
	private Instant createdAt;

	@BeforeEach
	void setUp() {
		createdById = 1L;
		name = "RushSeat Concert";
		description = "Music event";

		startsAt = Instant.parse("2026-11-10T09:00:00Z");
		endsAt = Instant.parse("2026-11-10T17:00:00Z");
		createdAt = Instant.parse("2026-11-03T09:00:00Z");
	}

	
	// CREATE
	

	@Nested
	class CreateTests {

		@Test
		void create_shouldCreateDraftEvent() {
			Event event = Event.create(
					createdById, name, description,
					startsAt, endsAt, createdAt
			);

			assertAll(
					() -> assertNull(event.getId()),
					() -> assertEquals(createdById, event.getCreatedById()),
					() -> assertEquals(name, event.getName()),
					() -> assertEquals(description, event.getDescription()),
					() -> assertEquals(EventStatus.DRAFT, event.getStatus()),
					() -> assertEquals(startsAt, event.getStartsAt()),
					() -> assertEquals(endsAt, event.getEndsAt()),
					() -> assertEquals(createdAt, event.getCreatedAt())
			);
		}

		@Test
		void create_shouldRejectNullCreatedById() {
			assertThrows(
					IllegalArgumentException.class,
					() -> Event.create(
							null, name, description,
							startsAt, endsAt, createdAt
					)
			);
		}

		@Test
		void create_shouldRejectNonPositiveCreatedById() {
			assertThrows(
					IllegalArgumentException.class,
					() -> Event.create(
							0L, name, description,
							startsAt, endsAt, createdAt
					)
			);
		}

		@Test
		void create_shouldRejectNullName() {
			assertThrows(
					IllegalArgumentException.class,
					() -> Event.create(
							createdById, null, description,
							startsAt, endsAt, createdAt
					)
			);
		}

		@Test
		void create_shouldRejectBlankName() {
			assertThrows(
					IllegalArgumentException.class,
					() -> Event.create(
							createdById, "   ", description,
							startsAt, endsAt, createdAt
					)
			);
		}

		@Test
		void create_shouldRejectNameExceeding200Characters() {
			String longName = "a".repeat(201);

			assertThrows(
					IllegalArgumentException.class,
					() -> Event.create(
							createdById, longName, description,
							startsAt, endsAt, createdAt
					)
			);
		}

		@Test
		void create_shouldAcceptNameWith200Characters() {
			String maxLengthName = "a".repeat(200);

			Event event = Event.create(
					createdById, maxLengthName, description,
					startsAt, endsAt, createdAt
			);

			assertEquals(maxLengthName, event.getName());
		}

		@Test
		void create_shouldAcceptNullDescription() {
			Event event = Event.create(
					createdById, name, null,
					startsAt, endsAt, createdAt
			);

			assertNull(event.getDescription());
		}

		@Test
		void create_shouldRejectNullStartsAt() {
			assertThrows(
					NullPointerException.class,
					() -> Event.create(
							createdById, name, description,
							null, endsAt, createdAt
					)
			);
		}

		@Test
		void create_shouldRejectNullEndsAt() {
			assertThrows(
					NullPointerException.class,
					() -> Event.create(
							createdById, name, description,
							startsAt, null, createdAt
					)
			);
		}

		@Test
		void create_shouldRejectEndsAtBeforeStartsAt() {
			Instant invalidEndsAt =
					Instant.parse("2026-11-10T08:00:00Z");

			assertThrows(
					IllegalArgumentException.class,
					() -> Event.create(
							createdById, name, description,
							startsAt, invalidEndsAt, createdAt
					)
			);
		}

		@Test
		void create_shouldRejectEndsAtEqualToStartsAt() {
			assertThrows(
					IllegalArgumentException.class,
					() -> Event.create(
							createdById, name, description,
							startsAt, startsAt, createdAt
					)
			);
		}

		@Test
		void create_shouldRejectNullCreatedAt() {
			assertThrows(
					NullPointerException.class,
					() -> Event.create(
							createdById, name, description,
							startsAt, endsAt, null
					)
			);
		}
	}

	
	// RECONSTITUTE
	

	@Nested
	class ReconstituteTests {

		@Test
		void reconstitute_shouldRestoreExistingEvent() {
			Long id = 10L;

			Event event = Event.reconstitute(
					id, createdById, name, description,
					EventStatus.PUBLISHED,
					startsAt, endsAt, createdAt
			);

			assertAll(
					() -> assertEquals(id, event.getId()),
					() -> assertEquals(createdById, event.getCreatedById()),
					() -> assertEquals(name, event.getName()),
					() -> assertEquals(description, event.getDescription()),
					() -> assertEquals(EventStatus.PUBLISHED, event.getStatus()),
					() -> assertEquals(startsAt, event.getStartsAt()),
					() -> assertEquals(endsAt, event.getEndsAt()),
					() -> assertEquals(createdAt, event.getCreatedAt())
			);
		}

		@Test
		void reconstitute_shouldRejectNullId() {
			assertThrows(
					NullPointerException.class,
					() -> Event.reconstitute(
							null, createdById, name, description,
							EventStatus.DRAFT,
							startsAt, endsAt, createdAt
					)
			);
		}

		@Test
		void reconstitute_shouldRejectNonPositiveId() {
			assertThrows(
					IllegalArgumentException.class,
					() -> Event.reconstitute(
							0L, createdById, name, description,
							EventStatus.DRAFT,
							startsAt, endsAt, createdAt
					)
			);
		}
	}

	
	// UPDATE DETAILS
	

	@Nested
	class UpdateDetailsTests {

		@Test
		void updateDetails_shouldUpdateNameAndDescription() {
			Event event = createDraftEvent();

			event.updateDetails(
					"Updated Concert",
					"Updated description"
			);

			assertAll(
					() -> assertEquals("Updated Concert", event.getName()),
					() -> assertEquals(
							"Updated description",
							event.getDescription()
					)
			);
		}

		@Test
		void updateDetails_shouldAcceptNullDescription() {
			Event event = createDraftEvent();

			event.updateDetails("Updated Concert", null);

			assertNull(event.getDescription());
		}

		@Test
		void updateDetails_shouldRejectNullName() {
			Event event = createDraftEvent();

			assertThrows(
					IllegalArgumentException.class,
					() -> event.updateDetails(null, "New description")
			);

			assertEquals(name, event.getName());
			assertEquals(description, event.getDescription());
		}

		@Test
		void updateDetails_shouldRejectBlankName() {
			Event event = createDraftEvent();

			assertThrows(
					IllegalArgumentException.class,
					() -> event.updateDetails("   ", "New description")
			);

			assertEquals(name, event.getName());
			assertEquals(description, event.getDescription());
		}

		@Test
		void updateDetails_shouldRejectNameExceeding200Characters() {
			Event event = createDraftEvent();
			String longName = "a".repeat(201);

			assertThrows(
					IllegalArgumentException.class,
					() -> event.updateDetails(longName, "New description")
			);

			assertEquals(name, event.getName());
			assertEquals(description, event.getDescription());
		}

		@Test
		void updateDetails_shouldAcceptNameWith200Characters() {
			Event event = createDraftEvent();
			String maxLengthName = "a".repeat(200);

			event.updateDetails(maxLengthName, description);

			assertEquals(maxLengthName, event.getName());
		}

		@Test
		void updateDetails_shouldRejectPublishedEvent() {
			Event event = createPublishedEvent();

			assertThrows(
					IllegalStateException.class,
					() -> event.updateDetails(
							"Updated Concert",
							"Updated description"
					)
			);

			assertEquals(name, event.getName());
			assertEquals(description, event.getDescription());
		}

		@Test
		void updateDetails_shouldRejectCancelledEvent() {
			Event event = createCancelledEvent();

			assertThrows(
					IllegalStateException.class,
					() -> event.updateDetails(
							"Updated Concert",
							"Updated description"
					)
			);

			assertEquals(name, event.getName());
			assertEquals(description, event.getDescription());
		}
	}

	
	// UPDATE SCHEDULE
	

	@Nested
	class UpdateScheduleTests {

		@Test
		void updateSchedule_shouldUpdateSchedule() {
			Event event = createDraftEvent();

			Instant newStartsAt =
					Instant.parse("2026-11-12T09:00:00Z");
			Instant newEndsAt =
					Instant.parse("2026-11-12T17:00:00Z");

			event.updateSchedule(newStartsAt, newEndsAt);

			assertAll(
					() -> assertEquals(newStartsAt, event.getStartsAt()),
					() -> assertEquals(newEndsAt, event.getEndsAt())
			);
		}

		@Test
		void updateSchedule_shouldRejectNullStartsAt() {
			Event event = createDraftEvent();

			assertThrows(
					NullPointerException.class,
					() -> event.updateSchedule(null, endsAt)
			);

			assertOriginalSchedule(event);
		}

		@Test
		void updateSchedule_shouldRejectNullEndsAt() {
			Event event = createDraftEvent();

			assertThrows(
					NullPointerException.class,
					() -> event.updateSchedule(startsAt, null)
			);

			assertOriginalSchedule(event);
		}

		@Test
		void updateSchedule_shouldRejectEndsAtBeforeStartsAt() {
			Event event = createDraftEvent();

			Instant invalidEndsAt =
					Instant.parse("2026-11-10T08:00:00Z");

			assertThrows(
					IllegalArgumentException.class,
					() -> event.updateSchedule(startsAt, invalidEndsAt)
			);

			assertOriginalSchedule(event);
		}

		@Test
		void updateSchedule_shouldRejectEndsAtEqualToStartsAt() {
			Event event = createDraftEvent();

			assertThrows(
					IllegalArgumentException.class,
					() -> event.updateSchedule(startsAt, startsAt)
			);

			assertOriginalSchedule(event);
		}

		@Test
		void updateSchedule_shouldRejectPublishedEvent() {
			Event event = createPublishedEvent();

			Instant newStartsAt =
					Instant.parse("2026-11-12T09:00:00Z");
			Instant newEndsAt =
					Instant.parse("2026-11-12T17:00:00Z");

			assertThrows(
					IllegalStateException.class,
					() -> event.updateSchedule(newStartsAt, newEndsAt)
			);

			assertOriginalSchedule(event);
		}

		@Test
		void updateSchedule_shouldRejectCancelledEvent() {
			Event event = createCancelledEvent();

			Instant newStartsAt =
					Instant.parse("2026-11-12T09:00:00Z");
			Instant newEndsAt =
					Instant.parse("2026-11-12T17:00:00Z");

			assertThrows(
					IllegalStateException.class,
					() -> event.updateSchedule(newStartsAt, newEndsAt)
			);

			assertOriginalSchedule(event);
		}
	}

	
	// TEST FIXTURES
	

	private Event createDraftEvent() {
		return Event.create(
				createdById, name, description,
				startsAt, endsAt, createdAt
		);
	}

	private Event createPublishedEvent() {
		return Event.reconstitute(
				10L, createdById, name, description,
				EventStatus.PUBLISHED,
				startsAt, endsAt, createdAt
		);
	}

	private Event createCancelledEvent() {
		return Event.reconstitute(
				10L, createdById, name, description,
				EventStatus.CANCELLED,
				startsAt, endsAt, createdAt
		);
	}

	private void assertOriginalSchedule(Event event) {
		assertAll(
				() -> assertEquals(startsAt, event.getStartsAt()),
				() -> assertEquals(endsAt, event.getEndsAt())
		);
	}
}
