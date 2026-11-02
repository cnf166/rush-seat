package com.rushseat.business.domain.model;

import lombok.Getter;

import java.time.Instant;

@Getter
public class Event {
	private final Long id;
	private final Long createdById;
	private final String name;
	private final String description;
	private EventStatus status;
	private final Instant startsAt;
	private final Instant endsAt;
	private final Instant createdAt;

	public Event(Long id, Long createdById, String name, String description, Instant startsAt, Instant endsAt, Instant createdAt, EventStatus status) {
		this.id = id;
		this.createdById = createdById;
		this.name = name;
		this.description = description;
		this.startsAt = startsAt;
		this.endsAt = endsAt;
		this.createdAt = createdAt;
		this.status = status;
	}

	public static Event create() {
		// TODO: Create event with DRAFT status
	}

	public static Event reconstitute() {
		// TODO: Restore existing event
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

}
