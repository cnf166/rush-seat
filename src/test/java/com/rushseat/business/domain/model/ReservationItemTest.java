package com.rushseat.business.domain.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReservationItemTest {

	private static final Long ID = 1L;
	private static final Long TICKET_TYPE_ID = 10L;

	private static final int QUANTITY = 2;
	private static final long UNIT_PRICE = 150_000L;

	private ReservationItem createReservationItem() {
		return ReservationItem.create(
				TICKET_TYPE_ID,
				QUANTITY,
				UNIT_PRICE
		);
	}

	private ReservationItem createReconstitutedReservationItem() {
		return ReservationItem.reconstitute(
				ID,
				TICKET_TYPE_ID,
				QUANTITY,
				UNIT_PRICE
		);
	}

	// Create

	@Nested
	class CreateTests {

		@Test
		void shouldCreateReservationItem() {
			ReservationItem item = createReservationItem();

			assertAll(
					() -> assertNull(item.getId()),
					() -> assertEquals(TICKET_TYPE_ID, item.getTicketTypeId()),
					() -> assertEquals(QUANTITY, item.getQuantity()),
					() -> assertEquals(UNIT_PRICE, item.getUnitPrice())
			);
		}

		@Test
		void shouldAllowZeroUnitPrice() {
			ReservationItem item = ReservationItem.create(
					TICKET_TYPE_ID,
					QUANTITY,
					0
			);

			assertEquals(0, item.getUnitPrice());
		}

		@Test
		void shouldThrowExceptionWhenTicketTypeIdIsNull() {
			assertThrows(
					IllegalArgumentException.class,
					() -> ReservationItem.create(
							null, QUANTITY, UNIT_PRICE
					)
			);
		}

		@Test
		void shouldThrowExceptionWhenTicketTypeIdIsZero() {
			assertThrows(
					IllegalArgumentException.class,
					() -> ReservationItem.create(
							0L, QUANTITY, UNIT_PRICE
					)
			);
		}

		@Test
		void shouldThrowExceptionWhenTicketTypeIdIsNegative() {
			assertThrows(
					IllegalArgumentException.class,
					() -> ReservationItem.create(
							-1L, QUANTITY, UNIT_PRICE
					)
			);
		}

		@Test
		void shouldThrowExceptionWhenQuantityIsZero() {
			assertThrows(
					IllegalArgumentException.class,
					() -> ReservationItem.create(
							TICKET_TYPE_ID, 0, UNIT_PRICE
					)
			);
		}

		@Test
		void shouldThrowExceptionWhenQuantityIsNegative() {
			assertThrows(
					IllegalArgumentException.class,
					() -> ReservationItem.create(
							TICKET_TYPE_ID, -1, UNIT_PRICE
					)
			);
		}

		@Test
		void shouldThrowExceptionWhenUnitPriceIsNegative() {
			assertThrows(
					IllegalArgumentException.class,
					() -> ReservationItem.create(
							TICKET_TYPE_ID, QUANTITY, -1
					)
			);
		}
	}

	// Reconstitute

	@Nested
	class ReconstituteTests {

		@Test
		void shouldReconstituteReservationItem() {
			ReservationItem item = createReconstitutedReservationItem();

			assertAll(
					() -> assertEquals(ID, item.getId()),
					() -> assertEquals(TICKET_TYPE_ID, item.getTicketTypeId()),
					() -> assertEquals(QUANTITY, item.getQuantity()),
					() -> assertEquals(UNIT_PRICE, item.getUnitPrice())
			);
		}

		@Test
		void shouldThrowExceptionWhenIdIsNull() {
			assertThrows(
					NullPointerException.class,
					() -> ReservationItem.reconstitute(
							null,
							TICKET_TYPE_ID,
							QUANTITY,
							UNIT_PRICE
					)
			);
		}

		@Test
		void shouldThrowExceptionWhenIdIsZero() {
			assertThrows(
					IllegalArgumentException.class,
					() -> ReservationItem.reconstitute(
							0L,
							TICKET_TYPE_ID,
							QUANTITY,
							UNIT_PRICE
					)
			);
		}

		@Test
		void shouldThrowExceptionWhenIdIsNegative() {
			assertThrows(
					IllegalArgumentException.class,
					() -> ReservationItem.reconstitute(
							-1L,
							TICKET_TYPE_ID,
							QUANTITY,
							UNIT_PRICE
					)
			);
		}
	}

	// Subtotal

	@Nested
	class SubtotalTests {

		@Test
		void shouldCalculateSubtotal() {
			ReservationItem item = createReservationItem();

			assertEquals(300_000L, item.getSubtotal());
		}

		@Test
		void shouldReturnZeroWhenUnitPriceIsZero() {
			ReservationItem item = ReservationItem.create(
					TICKET_TYPE_ID,
					QUANTITY,
					0
			);

			assertEquals(0L, item.getSubtotal());
		}

		@Test
		void shouldCalculateLargeSubtotalWithoutOverflow() {
			ReservationItem item = ReservationItem.create(
					TICKET_TYPE_ID,
					Integer.MAX_VALUE,
					1
			);

			assertEquals((long) Integer.MAX_VALUE, item.getSubtotal());
		}

		@Test
		void shouldThrowExceptionWhenSubtotalOverflows() {
			ReservationItem item = ReservationItem.create(
					TICKET_TYPE_ID,
					Integer.MAX_VALUE,
					Long.MAX_VALUE
			);

			assertThrows(
					ArithmeticException.class,
					item::getSubtotal
			);
		}
	}
}
