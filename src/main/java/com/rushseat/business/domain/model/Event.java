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

		// độ dài tên > 200
		String validName = requireNonBlank(name, "name");
		if (validName.length() > 200) {
			throw new IllegalArgumentException("Event name must be less than 200 characters");
		}

		// endtime diễn ra trước starttime
		Instant validStartsAt = Objects.requireNonNull(startsAt,  "startsAt must not be null");
		Instant validEndsAt = Objects.requireNonNull(endsAt, "endsAt must not be null");
		if (!validEndsAt.isAfter(validStartsAt)) {
			throw new IllegalArgumentException("endsAt must be after startsAt ");
		}

		this.id = id;
		this.createdById = requirePositiveId(createdById, "createdById");
		this.name = validName;
		this.description = description;
		this.status = Objects.requireNonNull(status, "Status must not be null");
		this.startsAt = validStartsAt;
		this.endsAt = validEndsAt;
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
	}

	public static Event create() {
		// TODO
	}

	public static Event reconstitute() {
		// TODO
	}

	public void updateDetails() {
		// TODO
	}

	public void updateSchedule() {
		// TODO
	}

	public void publish() {
		// TODO
	}

	public void cancel() {
		// TODO
	}

	public boolean canAcceptReservations(Instant now) {
		// TODO
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

}
