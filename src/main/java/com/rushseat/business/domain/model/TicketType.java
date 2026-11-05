package com.rushseat.business.domain.model;

public class TicketType {
	private final Long id;
	private final Long eventId;

	private String name;
	private String description;
	private long price;

	private int totalQuantity;
	private int heldQuantity;
	private int soldQuantity;

	// Creation & Information


	private TicketType(Long id, Long eventId, String name, String description, long price, int totalQuantity, int heldQuantity, int soldQuantity) {
		if (id != null && id <= 0) {
			throw new IllegalArgumentException("TicketType id is null or empty");
		}
		this.id = id;
		this.eventId = eventId;
		this.name = name;
		this.description = description;
		this.price = price;
		this.totalQuantity = totalQuantity;
		this.heldQuantity = heldQuantity;
		this.soldQuantity = soldQuantity;
	}
}
