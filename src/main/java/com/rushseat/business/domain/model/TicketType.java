package com.rushseat.business.domain.model;

import java.time.Instant;
import java.util.Objects;

public class TicketType {
	private final Long id;
	private final Long eventId;

	private String name;
	private String description;
	private long price;

	private int totalQuantity;
	private int heldQuantity;
	private int soldQuantity;

	private final Instant createdAt;

	// Creation & Information

	private TicketType(Long id, Long eventId, String name, String description, long price, int totalQuantity, int heldQuantity, int soldQuantity, Instant createdAt) {
		// validate identity
		if (id != null && id <= 0) {
			throw new IllegalArgumentException("TicketType id must be greater than 0");
		}

		Long validEventId = requirePositiveId(eventId, "eventId");

		// validate info
		String validName = validateName(name);
		long validPrice = validatePrice(price);

		// validate inventory
		validateInventory(totalQuantity, heldQuantity, soldQuantity);

		Instant validateCreatedAt = Objects.requireNonNull(createdAt, "createdAt must not be null");

		this.id = id;
		this.eventId = validEventId;
		this.name = validName;
		this.description = description;
		this.price = validPrice;
		this.totalQuantity = totalQuantity;
		this.heldQuantity = heldQuantity;
		this.soldQuantity = soldQuantity;
		this.createdAt = validateCreatedAt;
	}

	public static TicketType create(Long eventId, String name, String description, long price, int totalQuantity, Instant createdAt) {
		// TODO
	}

	public static TicketType reconstitute(Long id, Long eventId, String name, String description, long price, int totalQuantity, int heldQuantity, int soldQuantity, Instant createdAt) {
		// TODO
	}

	public void updateDetails(String newName, String newDescription) {
		// TODO
	}

	public void updatePrice(long newPrice) {
		// TODO
	}

	// validation

	private static String validateName(String name) {
		String validName = requireNonBlank(name, "name");

		if (validName.length() > 100) {
			throw new IllegalArgumentException("TicketType name must not exceed 100 characters");
		}

		return validName;
	}

	private static void validateInventory(int totalQuantity, int heldQuantity, int soldQuantity) {
		if (totalQuantity < 0) {
			throw new IllegalArgumentException("Total quantity must be greater than 0");
		}

		if (heldQuantity < 0) {
			throw new IllegalArgumentException("Held quantity must be greater than 0");
		}

		if (soldQuantity < 0) {
			throw new IllegalArgumentException("Sold quantity must be greater than 0");
		}

		if ((long) soldQuantity + heldQuantity > totalQuantity) {
			throw new IllegalArgumentException("Sum of sold quantity and held quantity cannot exceed total quantity");
		}

	}

	private static long validatePrice(long price) {
		if (price < 0) {
			throw new IllegalArgumentException("Price cannot be negative");
		}
		return price;
	}

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
