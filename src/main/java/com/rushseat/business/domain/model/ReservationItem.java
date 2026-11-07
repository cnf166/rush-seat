package com.rushseat.business.domain.model;

import lombok.Getter;

import java.util.Objects;

@Getter
public class ReservationItem {
	private final Long id;
	private final Long ticketTypeId;

	private final int quantity;
	private final long unitPrice;

	private ReservationItem(Long id, Long ticketTypeId, int quantity, long unitPrice) {
		// validate identity, ticketTypeId, quantity, unitPrice
		if (id != null && id <= 0) {
			throw new IllegalArgumentException("ReservationItem id must be greater than 0");
		}

		Long validTicketTypeId = requirePositiveId(ticketTypeId, "ticketTypeId");
		if (quantity <= 0) {
			throw new IllegalArgumentException("ReservationItem quantity must be greater than 0");
		}

		if (unitPrice < 0) {
			throw new IllegalArgumentException("ReservationItem unitPrice must be greater or equal to 0");
		}
		this.id = id;
		this.ticketTypeId = validTicketTypeId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
	}

	public static ReservationItem create(Long ticketTypeId, int quantity, long unitPrice) {
		return new ReservationItem(null, ticketTypeId, quantity, unitPrice);
	}

	public static ReservationItem reconstitute(Long id, Long ticketTypeId, int quantity, long unitPrice) {
		Objects.requireNonNull(id, "ReservationItem id must not be null");
		return new ReservationItem(id, ticketTypeId, quantity, unitPrice);
	}

	public long getSubtotal() {
		return Math.multiplyExact((long) quantity, unitPrice); // throw ArithmeticException if overflow
	}

	// validation
	private static Long requirePositiveId(Long id, String fieldName) {
		if (id == null || id <= 0) {
			throw new IllegalArgumentException(fieldName + " must be greater than 0");
		}
		return id;
	}

}
