package com.rushseat.business.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public class Event {
	private final Long id;
	private final Long createdById;
	private String name;
	private String description;
	private EventStatus status;
	private Instant startsAt;
	private Instant endsAt;
	private final Instant createdAt;

	private Event(Long id, Long createdById, String name, String description, EventStatus status, Instant startsAt, Instant endsAt, Instant createdAt) {
		if (id != null && id <= 0) {
			throw new IllegalArgumentException("Event id must be a positive integer");
		}

		Long validCreatedById = requirePositiveId(createdById, "createdById");

		// assign
		this.id = id;
		this.createdById = validCreatedById;
		this.name = validateName(name);
		this.description = description;
		this.status = Objects.requireNonNull(status, "Status must not be null");
		validateSchedule(startsAt, endsAt);
		this.startsAt = startsAt;
		this.endsAt = endsAt;
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
	}

	public static Event create(Long createdById, String name, String description, Instant startsAt, Instant endsAt, Instant createdAt) {
		return new Event(null, createdById, name, description, EventStatus.DRAFT, startsAt, endsAt, createdAt);
	}

	public static Event reconstitute(Long id, Long createdById, String name, String description, EventStatus status, Instant startsAt, Instant endsAt, Instant createdAt) {
		Objects.requireNonNull(id, "id must not be null");
		return new Event(id, createdById, name, description, status, startsAt, endsAt, createdAt);
	}

	public void updateDetails(String newName, String newDescription) {
		if (status != EventStatus.DRAFT) {
			throw new IllegalStateException("Only DRAFT events can be updated");
		}

		this.name = validateName(newName);
		this.description = newDescription;
	}

	public void updateSchedule(Instant newStartsAt, Instant newEndsAt) {
		if (status != EventStatus.DRAFT) {
			throw new IllegalStateException("Only DRAFT events can be updated");
		}

		validateSchedule(newStartsAt, newEndsAt);

		// assign
		this.startsAt = newStartsAt;
		this.endsAt = newEndsAt;
	}

	public void publish() {
		if (status != EventStatus.DRAFT) {
			throw new IllegalStateException("Only DRAFT events can be published");
		}
		this.status = EventStatus.PUBLISHED;
	}

	public void cancel() {
		if (status == EventStatus.CANCELLED) {
			throw new IllegalStateException("Only CANCELLED events can be cancelled");
		}
		this.status = EventStatus.CANCELLED;
	}

	public boolean canAcceptReservations(Instant now) {
		Objects.requireNonNull(now, "now must not be null");
		// check status event == Publish & startsAt <= now < endsAt
		// !now biểu thức có dấu bằng
		return status == EventStatus.PUBLISHED && !now.isBefore(startsAt) && now.isBefore(endsAt);
	}

	// helper methods
	private static String requireNonBlank(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " cannot be blank");
		}
		return value;
	}

	private static Long requirePositiveId(Long id, String fieldName) {
		if (id == null || id <= 0) {
			throw new IllegalArgumentException(fieldName + " must be greater than 0");
		}
		return id;
	}

	private static void validateSchedule(Instant startsAt, Instant endsAt) {
		// endtime diễn ra trước starttime
		Objects.requireNonNull(startsAt,  "startsAt must not be null");
		Objects.requireNonNull(endsAt, "endsAt must not be null");
		if (!endsAt.isAfter(startsAt)) {
			throw new IllegalArgumentException("endsAt must be after startsAt ");
		}
	}

	// check if name exceed 200 characters
	private static String validateName(String name) {
		String validName = requireNonBlank(name, "name");

		if (validName.length() > 200) {
			throw new IllegalArgumentException("Event name must not exceed 200 characters");
		}

		return validName;
	}

}
