
package com.rushseat.business.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

@Getter
public class TicketType {

	private static final int MAX_NAME_LENGTH = 100;
	private static final int MAX_PRICE_PRECISION = 15;

	private final Long id;
	private final Long eventId;
	private final Instant createdAt;

	private String name;
	private String description;
	private BigDecimal price;

	private int totalQuantity;
	private int heldQuantity;
	private int soldQuantity;

	private TicketType(
			Long id,
			Long eventId,
			String name,
			String description,
			BigDecimal price,
			int totalQuantity,
			int heldQuantity,
			int soldQuantity,
			Instant createdAt
	) {
		if (id != null && id <= 0) {
			throw new IllegalArgumentException(
					"TicketType id must be greater than 0"
			);
		}

		this.id = id;
		this.eventId = requirePositiveId(eventId, "eventId");
		this.name = validateName(name);
		this.description = description;
		this.price = validatePrice(price);

		validateInventory(totalQuantity, heldQuantity, soldQuantity);

		this.totalQuantity = totalQuantity;
		this.heldQuantity = heldQuantity;
		this.soldQuantity = soldQuantity;

		this.createdAt = Objects.requireNonNull(
				createdAt,
				"createdAt must not be null"
		);
	}

	// Creation & Reconstitution

	public static TicketType create(
			Long eventId,
			String name,
			String description,
			BigDecimal price,
			int totalQuantity,
			Instant createdAt
	) {
		return new TicketType(
				null,
				eventId,
				name,
				description,
				price,
				totalQuantity,
				0,
				0,
				createdAt
		);
	}

	public static TicketType reconstitute(
			Long id,
			Long eventId,
			String name,
			String description,
			BigDecimal price,
			int totalQuantity,
			int heldQuantity,
			int soldQuantity,
			Instant createdAt
	) {
		Objects.requireNonNull(id, "id must not be null");

		return new TicketType(
				id,
				eventId,
				name,
				description,
				price,
				totalQuantity,
				heldQuantity,
				soldQuantity,
				createdAt
		);
	}

	// Update

	public void updateDetails(String newName, String newDescription) {
		String validName = validateName(newName);

		this.name = validName;
		this.description = newDescription;
	}

	public void updatePrice(BigDecimal newPrice) {
		this.price = validatePrice(newPrice);
	}

	// Inventory

	public int getAvailableQuantity() {
		return totalQuantity - heldQuantity - soldQuantity;
	}

	public void hold(int quantity) {
		validatePositiveQuantity(quantity);

		if (getAvailableQuantity() < quantity) {
			throw new IllegalStateException(
					"Not enough available tickets"
			);
		}

		heldQuantity += quantity;
	}

	public void release(int quantity) {
		validatePositiveQuantity(quantity);

		if (heldQuantity < quantity) {
			throw new IllegalStateException(
					"Not enough held tickets to be released"
			);
		}

		heldQuantity -= quantity;
	}

	public void sell(int quantity) {
		validatePositiveQuantity(quantity);

		if (heldQuantity < quantity) {
			throw new IllegalStateException(
					"Not enough held tickets to be sold"
			);
		}

		heldQuantity -= quantity;
		soldQuantity += quantity;
	}

	// Validation

	private static String validateName(String name) {
		String validName = requireNonBlank(name, "name");

		if (validName.length() > MAX_NAME_LENGTH) {
			throw new IllegalArgumentException(
					"TicketType name must not exceed 100 characters"
			);
		}

		return validName;
	}

	private static BigDecimal validatePrice(BigDecimal price) {
		Objects.requireNonNull(price, "price must not be null");

		if (price.signum() < 0) {
			throw new IllegalArgumentException(
					"Price cannot be negative"
			);
		}

		BigDecimal normalizedPrice;

		try {
			normalizedPrice = price.setScale(
					0,
					RoundingMode.UNNECESSARY
			);
		} catch (ArithmeticException exception) {
			throw new IllegalArgumentException(
					"Price must not contain fractional digits",
					exception
			);
		}

		if (normalizedPrice.precision() > MAX_PRICE_PRECISION) {
			throw new IllegalArgumentException(
					"Price must not exceed NUMERIC(15, 0)"
			);
		}

		return normalizedPrice;
	}

	private static void validateInventory(
			int totalQuantity,
			int heldQuantity,
			int soldQuantity
	) {
		if (totalQuantity < 0) {
			throw new IllegalArgumentException(
					"Total quantity must not be negative"
			);
		}

		if (heldQuantity < 0) {
			throw new IllegalArgumentException(
					"Held quantity must not be negative"
			);
		}

		if (soldQuantity < 0) {
			throw new IllegalArgumentException(
					"Sold quantity must not be negative"
			);
		}

		if ((long) heldQuantity + soldQuantity > totalQuantity) {
			throw new IllegalArgumentException(
					"Held quantity and sold quantity cannot exceed total quantity"
			);
		}
	}

	private static void validatePositiveQuantity(int quantity) {
		if (quantity <= 0) {
			throw new IllegalArgumentException(
					"Quantity must be greater than 0"
			);
		}
	}

	private static String requireNonBlank(
			String value,
			String fieldName
	) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(
					fieldName + " cannot be blank"
			);
		}

		return value;
	}

	private static Long requirePositiveId(
			Long id,
			String fieldName
	) {
		if (id == null || id <= 0) {
			throw new IllegalArgumentException(
					fieldName + " must be greater than 0"
			);
		}

		return id;
	}
}
