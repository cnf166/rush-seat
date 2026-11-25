
package com.rushseat.business.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationItemTest {

	private static final Long TICKET_TYPE_ID = 1L;

	// Creation

	@Test
	void create_shouldCreateReservationItem() {
		ReservationItem item = ReservationItem.create(
				TICKET_TYPE_ID,
				2,
				new BigDecimal("500000")
		);

		assertThat(item.getId()).isNull();
		assertThat(item.getTicketTypeId()).isEqualTo(TICKET_TYPE_ID);
		assertThat(item.getQuantity()).isEqualTo(2);
		assertThat(item.getUnitPrice())
				.isEqualByComparingTo("500000");
	}

	@Test
	void create_shouldCalculateSubtotal() {
		ReservationItem item = ReservationItem.create(
				TICKET_TYPE_ID,
				2,
				new BigDecimal("500000")
		);

		assertThat(item.getSubtotal())
				.isEqualByComparingTo("1000000");
	}

	@Test
	void create_shouldAcceptZeroUnitPrice() {
		ReservationItem item = ReservationItem.create(
				TICKET_TYPE_ID,
				2,
				BigDecimal.ZERO
		);

		assertThat(item.getUnitPrice())
				.isEqualByComparingTo("0");

		assertThat(item.getSubtotal())
				.isEqualByComparingTo("0");
	}

	// Reconstitution

	@Test
	void reconstitute_shouldRestoreReservationItem() {
		ReservationItem item = ReservationItem.reconstitute(
				10L,
				TICKET_TYPE_ID,
				3,
				new BigDecimal("200000")
		);

		assertThat(item.getId()).isEqualTo(10L);
		assertThat(item.getTicketTypeId()).isEqualTo(TICKET_TYPE_ID);
		assertThat(item.getQuantity()).isEqualTo(3);
		assertThat(item.getUnitPrice())
				.isEqualByComparingTo("200000");
		assertThat(item.getSubtotal())
				.isEqualByComparingTo("600000");
	}

	// Identity validation

	@Test
	void create_shouldRejectInvalidTicketTypeId() {
		assertThatThrownBy(() ->
				ReservationItem.create(
						null,
						2,
						new BigDecimal("500000")
				)
		).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void reconstitute_shouldRejectNullId() {
		assertThatThrownBy(() ->
				ReservationItem.reconstitute(
						null,
						TICKET_TYPE_ID,
						2,
						new BigDecimal("500000")
				)
		).isInstanceOf(NullPointerException.class);
	}

	@Test
	void reconstitute_shouldRejectNonPositiveId() {
		assertThatThrownBy(() ->
				ReservationItem.reconstitute(
						0L,
						TICKET_TYPE_ID,
						2,
						new BigDecimal("500000")
				)
		).isInstanceOf(IllegalArgumentException.class);
	}

	// Quantity validation

	@Test
	void create_shouldRejectZeroQuantity() {
		assertThatThrownBy(() ->
				ReservationItem.create(
						TICKET_TYPE_ID,
						0,
						new BigDecimal("500000")
				)
		).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void create_shouldRejectNegativeQuantity() {
		assertThatThrownBy(() ->
				ReservationItem.create(
						TICKET_TYPE_ID,
						-1,
						new BigDecimal("500000")
				)
		).isInstanceOf(IllegalArgumentException.class);
	}

	// Unit price validation

	@Test
	void create_shouldRejectNullUnitPrice() {
		assertThatThrownBy(() ->
				ReservationItem.create(
						TICKET_TYPE_ID,
						2,
						null
				)
		).isInstanceOf(NullPointerException.class);
	}

	@Test
	void create_shouldRejectNegativeUnitPrice() {
		assertThatThrownBy(() ->
				ReservationItem.create(
						TICKET_TYPE_ID,
						2,
						new BigDecimal("-100")
				)
		).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void create_shouldAcceptUnitPriceWithZeroFractionalDigits() {
		ReservationItem item = ReservationItem.create(
				TICKET_TYPE_ID,
				2,
				new BigDecimal("500000.00")
		);

		assertThat(item.getUnitPrice())
				.isEqualByComparingTo("500000");
	}

	@Test
	void create_shouldRejectUnitPriceWithNonZeroFractionalDigits() {
		assertThatThrownBy(() ->
				ReservationItem.create(
						TICKET_TYPE_ID,
						2,
						new BigDecimal("500000.50")
				)
		).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void create_shouldRejectUnitPriceExceedingPrecision15() {
		assertThatThrownBy(() ->
				ReservationItem.create(
						TICKET_TYPE_ID,
						2,
						new BigDecimal("1000000000000000")
				)
		).isInstanceOf(IllegalArgumentException.class);
	}

	// Subtotal calculation

	@Test
	void getSubtotal_shouldCalculateLargeAmountWithoutOverflow() {
		ReservationItem item = ReservationItem.create(
				TICKET_TYPE_ID,
				Integer.MAX_VALUE,
				new BigDecimal("999999999999999")
		);

		assertThat(item.getSubtotal()).isEqualByComparingTo(
				new BigDecimal("2147483646999997852516353")
		);
	}
}
