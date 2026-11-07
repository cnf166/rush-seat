package com.rushseat.business.domain.model;

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
		// TODO
	}

	public static ReservationItem reconstitute(Long id, Long ticketTypeId, int quantity, long unitPrice) {
		// TODO
	}

	public long getSubtotal() {
		// TODO
	}
}
