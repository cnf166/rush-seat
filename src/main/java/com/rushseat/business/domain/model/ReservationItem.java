
package com.rushseat.business.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Getter
public class ReservationItem {

	private static final int MAX_PRICE_PRECISION = 15;

	private final Long id;
	private final Long ticketTypeId;

	private final int quantity;
	private final BigDecimal unitPrice;

	private ReservationItem(
			Long id,
			Long ticketTypeId,
			int quantity,
			BigDecimal unitPrice
	) {
		if (id != null && id <= 0) {
			throw new IllegalArgumentException(
					"ReservationItem id must be greater than 0"
			);
		}

		this.id = id;
		this.ticketTypeId = requirePositiveId(
				ticketTypeId,
				"ticketTypeId"
		);

		if (quantity <= 0) {
			throw new IllegalArgumentException(
					"ReservationItem quantity must be greater than 0"
			);
		}

		this.quantity = quantity;
		this.unitPrice = validateUnitPrice(unitPrice);
	}

	// Creation & Reconstitution

	public static ReservationItem create(
			Long ticketTypeId,
			int quantity,
			BigDecimal unitPrice
	) {
		return new ReservationItem(
				null,
				ticketTypeId,
				quantity,
				unitPrice
		);
	}

	public static ReservationItem reconstitute(
			Long id,
			Long ticketTypeId,
			int quantity,
			BigDecimal unitPrice
	) {
		Objects.requireNonNull(
				id,
				"ReservationItem id must not be null"
		);

		return new ReservationItem(
				id,
				ticketTypeId,
				quantity,
				unitPrice
		);
	}

	// Calculation

	public BigDecimal getSubtotal() {
		return unitPrice.multiply(BigDecimal.valueOf(quantity));
	}

	// Validation

	private static BigDecimal validateUnitPrice(BigDecimal unitPrice) {
		Objects.requireNonNull(
				unitPrice,
				"unitPrice must not be null"
		);

		if (unitPrice.signum() < 0) {
			throw new IllegalArgumentException(
					"Unit price cannot be negative"
			);
		}

		BigDecimal normalizedPrice;

		try {
			normalizedPrice = unitPrice.setScale(
					0,
					RoundingMode.UNNECESSARY
			);
		} catch (ArithmeticException exception) {
			throw new IllegalArgumentException(
					"Unit price must not contain fractional digits",
					exception
			);
		}

		if (normalizedPrice.precision() > MAX_PRICE_PRECISION) {
			throw new IllegalArgumentException(
					"Unit price must not exceed NUMERIC(15, 0)"
			);
		}

		return normalizedPrice;
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
