package com.rushseat.business.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.*;

@Getter
public class Reservation {
	private final Long id;
	private final Long userId;
	private final Long eventId;

	private final List<ReservationItem> items;

	private ReservationStatus status;
	private final Instant createdAt;
	private final Instant expiresAt;
	private Instant updatedAt;

	private Reservation(Long id, Long userId, Long eventId, List<ReservationItem> items, ReservationStatus status, Instant createdAt, Instant expiresAt, Instant updatedAt) {
		if (id != null && id <= 0) {
			throw new IllegalArgumentException("Reservation id must be greater than 0");
		}

		Long validUserId = requirePositiveId(userId, "userId");
		Long validEventId = requirePositiveId(eventId, "eventId");

		validateTimestamps(createdAt, expiresAt, updatedAt);

		this.id = id;
		this.userId = validUserId;
		this.eventId = validEventId;
		this.items = validateItems(items);
		this.status = Objects.requireNonNull(status, "ReservationStatus must not be null");
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
		this.updatedAt = updatedAt;
	}

	public static Reservation create(Long userId, Long eventId, List<ReservationItem> items, Instant createdAt, Instant expiresAt) {
		// validate id = null, status = HELD, updatedAt = createdAt
		return new Reservation(null, userId, eventId, items, ReservationStatus.HELD, createdAt, expiresAt, createdAt);
	}

	public static Reservation reconstitute(Long id, Long userId, Long eventId, List<ReservationItem> items, ReservationStatus status, Instant createdAt, Instant updatedAt, Instant expiresAt) {
		// restore persisted state
		Objects.requireNonNull(id, "Reservation id must not be null");
		return new Reservation(id, userId, eventId, items, status, createdAt, expiresAt, updatedAt);
	}

	public long getTotalAmount() {
		long totalAmount = 0L;
		for (ReservationItem item : items) {
			totalAmount = Math.addExact(totalAmount, item.getSubtotal());
		}
		return totalAmount;
	}

	public void confirm(Instant now) {
		// Validate now
		validateTransitionTime(now);
		// Validate current status
		// Check expiration (updatedAt  <= now < expireAt)
		requireHeldStatus();
		if (!now.isBefore(expiresAt)) {
			throw new IllegalStateException("Reservation has expired");
		}

		// Change status
		// Update updatedAt
		status = ReservationStatus.CONFIRMED;
		updatedAt = now;
	}

	public void cancel(Instant now) {
		validateTransitionTime(now);
		// Validate current status
		// Reject if now >= expiresAt
		requireHeldStatus();
		if (!now.isBefore(expiresAt)) {
			throw new IllegalStateException("Reservation has expired");
		}

		// Change status to CANCELLED
		// Update updatedAt
		status = ReservationStatus.CANCELLED;
		updatedAt = now;
	}

	public void expire(Instant now) {
		// Validate now
		validateTransitionTime(now);
		// Validate current status
		// Reject if now < expiresAt
		requireHeldStatus();
		if (now.isBefore(expiresAt)) {
			throw new IllegalStateException("Reservation is not expired yet");
		}
		// Change status to EXPIRED
		status = ReservationStatus.EXPIRED;
		updatedAt = now;
		// Update updatedAt
	}

	// override Getter annotation method
	public List<ReservationItem> getItems() {
		return Collections.unmodifiableList(items);
	}

	// validation
	private static Long requirePositiveId(Long id, String fieldName) {
		if (id == null || id <= 0) {
			throw new IllegalArgumentException(fieldName + " must be greater than 0");
		}
		return id;
	}

	private static List<ReservationItem> validateItems(List<ReservationItem> items) {
		// null / empty check
		Objects.requireNonNull(items, "items must not be null");

		// null item check
		if (items.isEmpty()) {
			throw new IllegalArgumentException("Reservation must contain at least one item");
		}
		// duplicate ticketTypeId check

		Set<Long> ticketTypeIds = new HashSet<>();

		for (ReservationItem item : items) {
			Objects.requireNonNull(item, "Reservation item must not be null");
			if (!ticketTypeIds.add(item.getTicketTypeId())) {
				throw new IllegalArgumentException("Duplicate ticketTypeId: " + item.getTicketTypeId());
			}
		}
		//  return defensive copy
		return new ArrayList<>(items);
	}

	private static void validateTimestamps(Instant createdAt, Instant expiresAt, Instant updatedAt) {
		Objects.requireNonNull(createdAt, "createdAt must not be null");
		Objects.requireNonNull(expiresAt, "expiresAt must not be null");
		Objects.requireNonNull(updatedAt, "updatedAt must not be null");
		if (!createdAt.isBefore(expiresAt)) {
			throw new IllegalArgumentException("createdAt must be before expiresAt");
		}

		if (updatedAt.isBefore(createdAt)) {
			throw new IllegalArgumentException("updatedAt must not be before createdAt");
		}
	}

	private void validateTransitionTime(Instant now) {
		// Validate now
		Objects.requireNonNull(now, "now must not be null");
		// Validate that now is not before updatedAt
		if (now.isBefore(updatedAt)) {
			throw new IllegalArgumentException("now must not be before updatedAt");
		}
	}

	// check ReservationStatus == HELD
	private void requireHeldStatus() {
		// Require current status to be HELD
		if (status != ReservationStatus.HELD) {
			throw new IllegalStateException("Reservation must be in HELD status");
		}
	}
}
