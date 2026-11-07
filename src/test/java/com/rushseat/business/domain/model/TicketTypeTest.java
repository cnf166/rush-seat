package com.rushseat.business.domain.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TicketTypeTest {

	private static final Long EVENT_ID = 1L;
	private static final Long TICKET_TYPE_ID = 10L;

	private static final String NAME = "Standard";
	private static final String DESCRIPTION = "Standard admission ticket";

	private static final long PRICE = 150_000L;
	private static final int TOTAL_QUANTITY = 100;

	private static final Instant CREATED_AT =
			Instant.parse("2026-11-01T10:00:00Z");

	// Fixtures

	private TicketType createTicketType() {
		return TicketType.create(
				EVENT_ID,
				NAME,
				DESCRIPTION,
				PRICE,
				TOTAL_QUANTITY,
				CREATED_AT
		);
	}

	private TicketType createReconstitutedTicketType() {
		return TicketType.reconstitute(
				TICKET_TYPE_ID,
				EVENT_ID,
				NAME,
				DESCRIPTION,
				PRICE,
				TOTAL_QUANTITY,
				10,
				20,
				CREATED_AT
		);
	}

	// Create

	@Nested
	class CreateTests {

		@Test
		void shouldCreateTicketTypeWithDefaultInventory() {
			TicketType ticketType = createTicketType();

			assertAll(
					() -> assertNull(ticketType.getId()),
					() -> assertEquals(EVENT_ID, ticketType.getEventId()),
					() -> assertEquals(NAME, ticketType.getName()),
					() -> assertEquals(DESCRIPTION, ticketType.getDescription()),
					() -> assertEquals(PRICE, ticketType.getPrice()),
					() -> assertEquals(TOTAL_QUANTITY, ticketType.getTotalQuantity()),
					() -> assertEquals(0, ticketType.getHeldQuantity()),
					() -> assertEquals(0, ticketType.getSoldQuantity()),
					() -> assertEquals(CREATED_AT, ticketType.getCreatedAt())
			);
		}

		@Test
		void shouldAllowNullDescription() {
			TicketType ticketType = TicketType.create(
					EVENT_ID,
					NAME,
					null,
					PRICE,
					TOTAL_QUANTITY,
					CREATED_AT
			);

			assertNull(ticketType.getDescription());
		}

		@Test
		void shouldAllowZeroPrice() {
			TicketType ticketType = TicketType.create(
					EVENT_ID,
					NAME,
					DESCRIPTION,
					0,
					TOTAL_QUANTITY,
					CREATED_AT
			);

			assertEquals(0, ticketType.getPrice());
		}

		@Test
		void shouldAllowZeroTotalQuantity() {
			TicketType ticketType = TicketType.create(
					EVENT_ID,
					NAME,
					DESCRIPTION,
					PRICE,
					0,
					CREATED_AT
			);

			assertEquals(0, ticketType.getTotalQuantity());
		}

		@Test
		void shouldThrowExceptionWhenEventIdIsNull() {
			assertThrows(IllegalArgumentException.class,
					() -> TicketType.create(
							null, NAME, DESCRIPTION,
							PRICE, TOTAL_QUANTITY, CREATED_AT
					));
		}

		@Test
		void shouldThrowExceptionWhenEventIdIsNotPositive() {
			assertThrows(IllegalArgumentException.class,
					() -> TicketType.create(
							0L, NAME, DESCRIPTION,
							PRICE, TOTAL_QUANTITY, CREATED_AT
					));
		}

		@Test
		void shouldThrowExceptionWhenNameIsNull() {
			assertThrows(IllegalArgumentException.class,
					() -> TicketType.create(
							EVENT_ID, null, DESCRIPTION,
							PRICE, TOTAL_QUANTITY, CREATED_AT
					));
		}

		@Test
		void shouldThrowExceptionWhenNameIsBlank() {
			assertThrows(IllegalArgumentException.class,
					() -> TicketType.create(
							EVENT_ID, "   ", DESCRIPTION,
							PRICE, TOTAL_QUANTITY, CREATED_AT
					));
		}

		@Test
		void shouldThrowExceptionWhenNameExceeds100Characters() {
			String name = "A".repeat(101);

			assertThrows(IllegalArgumentException.class,
					() -> TicketType.create(
							EVENT_ID, name, DESCRIPTION,
							PRICE, TOTAL_QUANTITY, CREATED_AT
					));
		}

		@Test
		void shouldAllowNameWithExactly100Characters() {
			String name = "A".repeat(100);

			TicketType ticketType = TicketType.create(
					EVENT_ID, name, DESCRIPTION,
					PRICE, TOTAL_QUANTITY, CREATED_AT
			);

			assertEquals(name, ticketType.getName());
		}

		@Test
		void shouldThrowExceptionWhenPriceIsNegative() {
			assertThrows(IllegalArgumentException.class,
					() -> TicketType.create(
							EVENT_ID, NAME, DESCRIPTION,
							-1, TOTAL_QUANTITY, CREATED_AT
					));
		}

		@Test
		void shouldThrowExceptionWhenTotalQuantityIsNegative() {
			assertThrows(IllegalArgumentException.class,
					() -> TicketType.create(
							EVENT_ID, NAME, DESCRIPTION,
							PRICE, -1, CREATED_AT
					));
		}

		@Test
		void shouldThrowExceptionWhenCreatedAtIsNull() {
			assertThrows(NullPointerException.class,
					() -> TicketType.create(
							EVENT_ID, NAME, DESCRIPTION,
							PRICE, TOTAL_QUANTITY, null
					));
		}
	}

	// Reconstitute

	@Nested
	class ReconstituteTests {

		@Test
		void shouldReconstituteTicketTypeWithExistingInventory() {
			TicketType ticketType = createReconstitutedTicketType();

			assertAll(
					() -> assertEquals(TICKET_TYPE_ID, ticketType.getId()),
					() -> assertEquals(EVENT_ID, ticketType.getEventId()),
					() -> assertEquals(NAME, ticketType.getName()),
					() -> assertEquals(DESCRIPTION, ticketType.getDescription()),
					() -> assertEquals(PRICE, ticketType.getPrice()),
					() -> assertEquals(TOTAL_QUANTITY, ticketType.getTotalQuantity()),
					() -> assertEquals(10, ticketType.getHeldQuantity()),
					() -> assertEquals(20, ticketType.getSoldQuantity()),
					() -> assertEquals(CREATED_AT, ticketType.getCreatedAt())
			);
		}

		@Test
		void shouldAllowNullDescription() {
			TicketType ticketType = TicketType.reconstitute(
					TICKET_TYPE_ID,
					EVENT_ID,
					NAME,
					null,
					PRICE,
					TOTAL_QUANTITY,
					0,
					0,
					CREATED_AT
			);

			assertNull(ticketType.getDescription());
		}

		@Test
		void shouldThrowExceptionWhenIdIsNull() {
			assertThrows(NullPointerException.class,
					() -> TicketType.reconstitute(
							null, EVENT_ID, NAME, DESCRIPTION,
							PRICE, TOTAL_QUANTITY, 0, 0, CREATED_AT
					));
		}

		@Test
		void shouldThrowExceptionWhenIdIsNotPositive() {
			assertThrows(IllegalArgumentException.class,
					() -> TicketType.reconstitute(
							0L, EVENT_ID, NAME, DESCRIPTION,
							PRICE, TOTAL_QUANTITY, 0, 0, CREATED_AT
					));
		}

		@Test
		void shouldThrowExceptionWhenInventoryExceedsTotalQuantity() {
			assertThrows(IllegalArgumentException.class,
					() -> TicketType.reconstitute(
							TICKET_TYPE_ID,
							EVENT_ID,
							NAME,
							DESCRIPTION,
							PRICE,
							100,
							60,
							50,
							CREATED_AT
					));
		}

		@Test
		void shouldThrowExceptionWhenHeldQuantityIsNegative() {
			assertThrows(IllegalArgumentException.class,
					() -> TicketType.reconstitute(
							TICKET_TYPE_ID,
							EVENT_ID,
							NAME,
							DESCRIPTION,
							PRICE,
							TOTAL_QUANTITY,
							-1,
							0,
							CREATED_AT
					));
		}

		@Test
		void shouldThrowExceptionWhenSoldQuantityIsNegative() {
			assertThrows(IllegalArgumentException.class,
					() -> TicketType.reconstitute(
							TICKET_TYPE_ID,
							EVENT_ID,
							NAME,
							DESCRIPTION,
							PRICE,
							TOTAL_QUANTITY,
							0,
							-1,
							CREATED_AT
					));
		}
	}

	// Update details

	@Nested
	class UpdateDetailsTests {

		@Test
		void shouldUpdateNameAndDescription() {
			TicketType ticketType = createTicketType();

			ticketType.updateDetails(
					"VIP",
					"VIP admission ticket"
			);

			assertAll(
					() -> assertEquals("VIP", ticketType.getName()),
					() -> assertEquals("VIP admission ticket", ticketType.getDescription())
			);
		}

		@Test
		void shouldAllowSettingDescriptionToNull() {
			TicketType ticketType = createTicketType();

			ticketType.updateDetails("VIP", null);

			assertAll(
					() -> assertEquals("VIP", ticketType.getName()),
					() -> assertNull(ticketType.getDescription())
			);
		}

		@Test
		void shouldThrowExceptionWhenNewNameIsBlank() {
			TicketType ticketType = createTicketType();

			assertThrows(IllegalArgumentException.class,
					() -> ticketType.updateDetails(" ", "New description"));

			assertAll(
					() -> assertEquals(NAME, ticketType.getName()),
					() -> assertEquals(DESCRIPTION, ticketType.getDescription())
			);
		}

		@Test
		void shouldThrowExceptionWhenNewNameExceeds100Characters() {
			TicketType ticketType = createTicketType();
			String newName = "A".repeat(101);

			assertThrows(IllegalArgumentException.class,
					() -> ticketType.updateDetails(newName, "New description"));

			assertAll(
					() -> assertEquals(NAME, ticketType.getName()),
					() -> assertEquals(DESCRIPTION, ticketType.getDescription())
			);
		}
	}

	// Update price

	@Nested
	class UpdatePriceTests {

		@Test
		void shouldUpdatePrice() {
			TicketType ticketType = createTicketType();

			ticketType.updatePrice(200_000L);

			assertEquals(200_000L, ticketType.getPrice());
		}

		@Test
		void shouldAllowZeroPrice() {
			TicketType ticketType = createTicketType();

			ticketType.updatePrice(0);

			assertEquals(0, ticketType.getPrice());
		}

		@Test
		void shouldThrowExceptionWhenPriceIsNegative() {
			TicketType ticketType = createTicketType();

			assertThrows(IllegalArgumentException.class,
					() -> ticketType.updatePrice(-1));

			assertEquals(PRICE, ticketType.getPrice());
		}
	}
}