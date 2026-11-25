
package com.rushseat.business.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketTypeTest {

	private static final Long EVENT_ID = 1L;
	private static final Instant CREATED_AT =
			Instant.parse("2026-11-25T10:00:00Z");

	private TicketType ticketType;

	@BeforeEach
	void setUp() {
		ticketType = TicketType.create(
				EVENT_ID,
				"VIP",
				"VIP ticket",
				new BigDecimal("500000"),
				100,
				CREATED_AT
		);
	}

	// Creation

	@Test
	void create_shouldInitializeTicketTypeWithEmptyInventory() {
		assertThat(ticketType.getId()).isNull();
		assertThat(ticketType.getEventId()).isEqualTo(EVENT_ID);
		assertThat(ticketType.getName()).isEqualTo("VIP");
		assertThat(ticketType.getDescription()).isEqualTo("VIP ticket");
		assertThat(ticketType.getPrice())
				.isEqualByComparingTo("500000");

		assertThat(ticketType.getTotalQuantity()).isEqualTo(100);
		assertThat(ticketType.getHeldQuantity()).isZero();
		assertThat(ticketType.getSoldQuantity()).isZero();
		assertThat(ticketType.getAvailableQuantity()).isEqualTo(100);
		assertThat(ticketType.getCreatedAt()).isEqualTo(CREATED_AT);
	}

	@Test
	void reconstitute_shouldRestoreExistingTicketType() {
		TicketType restored = TicketType.reconstitute(
				10L,
				EVENT_ID,
				"VIP",
				"VIP ticket",
				new BigDecimal("500000"),
				100,
				20,
				30,
				CREATED_AT
		);

		assertThat(restored.getId()).isEqualTo(10L);
		assertThat(restored.getEventId()).isEqualTo(EVENT_ID);
		assertThat(restored.getHeldQuantity()).isEqualTo(20);
		assertThat(restored.getSoldQuantity()).isEqualTo(30);
		assertThat(restored.getAvailableQuantity()).isEqualTo(50);
	}

	// Identity validation

	@Test
	void create_shouldRejectInvalidEventId() {
		assertThatThrownBy(() -> TicketType.create(
				null,
				"VIP",
				null,
				new BigDecimal("500000"),
				100,
				CREATED_AT
		)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void reconstitute_shouldRejectNullId() {
		assertThatThrownBy(() -> TicketType.reconstitute(
				null,
				EVENT_ID,
				"VIP",
				null,
				new BigDecimal("500000"),
				100,
				0,
				0,
				CREATED_AT
		)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void reconstitute_shouldRejectNonPositiveId() {
		assertThatThrownBy(() -> TicketType.reconstitute(
				0L,
				EVENT_ID,
				"VIP",
				null,
				new BigDecimal("500000"),
				100,
				0,
				0,
				CREATED_AT
		)).isInstanceOf(IllegalArgumentException.class);
	}

	// Name validation

	@Test
	void create_shouldRejectBlankName() {
		assertThatThrownBy(() -> TicketType.create(
				EVENT_ID,
				"   ",
				null,
				new BigDecimal("500000"),
				100,
				CREATED_AT
		)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void create_shouldRejectNameLongerThan100Characters() {
		String longName = "A".repeat(101);

		assertThatThrownBy(() -> TicketType.create(
				EVENT_ID,
				longName,
				null,
				new BigDecimal("500000"),
				100,
				CREATED_AT
		)).isInstanceOf(IllegalArgumentException.class);
	}

	// Price validation

	@Test
	void create_shouldRejectNullPrice() {
		assertThatThrownBy(() -> TicketType.create(
				EVENT_ID,
				"VIP",
				null,
				null,
				100,
				CREATED_AT
		)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void create_shouldRejectNegativePrice() {
		assertThatThrownBy(() -> TicketType.create(
				EVENT_ID,
				"VIP",
				null,
				new BigDecimal("-100"),
				100,
				CREATED_AT
		)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void create_shouldAcceptPriceWithZeroFractionalDigits() {
		TicketType result = TicketType.create(
				EVENT_ID,
				"VIP",
				null,
				new BigDecimal("500000.00"),
				100,
				CREATED_AT
		);

		assertThat(result.getPrice())
				.isEqualByComparingTo("500000");
	}

	@Test
	void create_shouldRejectPriceWithNonZeroFractionalDigits() {
		assertThatThrownBy(() -> TicketType.create(
				EVENT_ID,
				"VIP",
				null,
				new BigDecimal("500000.50"),
				100,
				CREATED_AT
		)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void create_shouldRejectPriceExceedingPrecision15() {
		assertThatThrownBy(() -> TicketType.create(
				EVENT_ID,
				"VIP",
				null,
				new BigDecimal("1000000000000000"),
				100,
				CREATED_AT
		)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void updatePrice_shouldPreserveOldPriceWhenNewPriceIsInvalid() {
		assertThatThrownBy(() ->
				ticketType.updatePrice(new BigDecimal("-100"))
		).isInstanceOf(IllegalArgumentException.class);

		assertThat(ticketType.getPrice())
				.isEqualByComparingTo("500000");
	}

	// Inventory validation

	@Test
	void create_shouldAcceptZeroTotalQuantity() {
		TicketType result = TicketType.create(
				EVENT_ID,
				"VIP",
				null,
				new BigDecimal("500000"),
				0,
				CREATED_AT
		);

		assertThat(result.getTotalQuantity()).isZero();
		assertThat(result.getAvailableQuantity()).isZero();
	}

	@Test
	void create_shouldRejectNegativeTotalQuantity() {
		assertThatThrownBy(() -> TicketType.create(
				EVENT_ID,
				"VIP",
				null,
				new BigDecimal("500000"),
				-1,
				CREATED_AT
		)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void reconstitute_shouldRejectInventoryExceedingTotalQuantity() {
		assertThatThrownBy(() -> TicketType.reconstitute(
				10L,
				EVENT_ID,
				"VIP",
				null,
				new BigDecimal("500000"),
				100,
				60,
				50,
				CREATED_AT
		)).isInstanceOf(IllegalArgumentException.class);
	}

	// Update details

	@Test
	void updateDetails_shouldUpdateNameAndDescription() {
		ticketType.updateDetails("Premium", "Premium ticket");

		assertThat(ticketType.getName()).isEqualTo("Premium");
		assertThat(ticketType.getDescription())
				.isEqualTo("Premium ticket");
	}

	@Test
	void updateDetails_shouldPreserveOldDetailsWhenNameIsInvalid() {
		assertThatThrownBy(() ->
				ticketType.updateDetails(" ", "New description")
		).isInstanceOf(IllegalArgumentException.class);

		assertThat(ticketType.getName()).isEqualTo("VIP");
		assertThat(ticketType.getDescription())
				.isEqualTo("VIP ticket");
	}

	// Hold

	@Test
	void hold_shouldIncreaseHeldQuantity() {
		ticketType.hold(20);

		assertThat(ticketType.getHeldQuantity()).isEqualTo(20);
		assertThat(ticketType.getAvailableQuantity()).isEqualTo(80);
	}

	@Test
	void hold_shouldRejectZeroOrNegativeQuantity() {
		assertThatThrownBy(() -> ticketType.hold(0))
				.isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> ticketType.hold(-1))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void hold_shouldRejectQuantityExceedingAvailableInventory() {
		assertThatThrownBy(() -> ticketType.hold(101))
				.isInstanceOf(IllegalStateException.class);

		assertThat(ticketType.getHeldQuantity()).isZero();
	}

	// Release

	@Test
	void release_shouldDecreaseHeldQuantity() {
		ticketType.hold(30);

		ticketType.release(10);

		assertThat(ticketType.getHeldQuantity()).isEqualTo(20);
		assertThat(ticketType.getAvailableQuantity()).isEqualTo(80);
	}

	@Test
	void release_shouldRejectQuantityExceedingHeldInventory() {
		ticketType.hold(10);

		assertThatThrownBy(() -> ticketType.release(11))
				.isInstanceOf(IllegalStateException.class);

		assertThat(ticketType.getHeldQuantity()).isEqualTo(10);
	}

	// Sell

	@Test
	void sell_shouldMoveQuantityFromHeldToSold() {
		ticketType.hold(30);

		ticketType.sell(20);

		assertThat(ticketType.getHeldQuantity()).isEqualTo(10);
		assertThat(ticketType.getSoldQuantity()).isEqualTo(20);
		assertThat(ticketType.getAvailableQuantity()).isEqualTo(70);
	}

	@Test
	void sell_shouldRejectQuantityExceedingHeldInventory() {
		ticketType.hold(10);

		assertThatThrownBy(() -> ticketType.sell(11))
				.isInstanceOf(IllegalStateException.class);

		assertThat(ticketType.getHeldQuantity()).isEqualTo(10);
		assertThat(ticketType.getSoldQuantity()).isZero();
	}
}
