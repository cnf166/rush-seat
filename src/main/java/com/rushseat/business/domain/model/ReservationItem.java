package com.rushseat.business.domain.model;

import java.util.Objects;

public class ReservationItem {
	private final Long id;
	private final Long ticketTypeId;

	private final int quantity;
	private final long unitPrice;

	private ReservationItem(Long id, Long ticketTypeId, int quantity, long unitPrice) {
		// TODO: validate identity
		// TODO: validate ticketTypeId
		// TODO: validate quantity
		// TODO: validate unitPrice
		this.id = id;
		this.ticketTypeId = ticketTypeId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
	}

	public static ReservationItem create(Long ticketTypeId, int quantity, long unitPrice) {
		return new ReservationItem(null, ticketTypeId, quantity, unitPrice);
	}

	public static ReservationItem reconstitute(Long id, Long ticketTypeId, int quantity, long unitPrice) {
		Objects.requireNonNull(id, "Id must not be null");
		return new ReservationItem(id, ticketTypeId, quantity, unitPrice);
	}

	public long getSubtotal() {
		return Math.multiplyExact((long) quantity, unitPrice);
	}

	// validation
}
