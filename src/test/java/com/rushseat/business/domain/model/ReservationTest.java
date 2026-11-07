
package com.rushseat.business.domain.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationTest {

	private static final Long USER_ID = 1L;
	private static final Long EVENT_ID = 10L;
	private static final Long RESERVATION_ID = 100L;

	private static final Instant CREATED_AT =
			Instant.parse("2026-11-01T10:00:00Z");

	private static final Instant EXPIRES_AT =
			Instant.parse("2026-11-01T10:05:00Z");

	private static final Instant UPDATED_AT =
			Instant.parse("2026-11-01T10:02:00Z");

	private ReservationItem createItem(Long ticketTypeId) {
		return ReservationItem.create(ticketTypeId, 2, 100_000L);
	}

	private Reservation createValidReservation() {
		return Reservation.create(
				USER_ID,
				EVENT_ID,
				List.of(createItem(1L)),
				CREATED_AT,
				EXPIRES_AT
		);
	}

	@Nested
	class CreateTests {

		@Test
		void shouldCreateReservationSuccessfully() {
			Reservation reservation = createValidReservation();

			assertNull(reservation.getId());
			assertEquals(USER_ID, reservation.getUserId());
			assertEquals(EVENT_ID, reservation.getEventId());
			assertEquals(ReservationStatus.HELD, reservation.getStatus());
			assertEquals(CREATED_AT, reservation.getCreatedAt());
			assertEquals(EXPIRES_AT, reservation.getExpiresAt());
			assertEquals(CREATED_AT, reservation.getUpdatedAt());
			assertEquals(1, reservation.getItems().size());
		}

		@Test
		void shouldRejectNullUserId() {
			assertThrows(IllegalArgumentException.class, () ->
					Reservation.create(
							null, EVENT_ID,
							List.of(createItem(1L)),
							CREATED_AT, EXPIRES_AT
					)
			);
		}

		@Test
		void shouldRejectInvalidUserId() {
			assertAll(
					() -> assertThrows(IllegalArgumentException.class, () ->
							Reservation.create(
									0L, EVENT_ID,
									List.of(createItem(1L)),
									CREATED_AT, EXPIRES_AT
							)
					),
					() -> assertThrows(IllegalArgumentException.class, () ->
							Reservation.create(
									-1L, EVENT_ID,
									List.of(createItem(1L)),
									CREATED_AT, EXPIRES_AT
							)
					)
			);
		}

		@Test
		void shouldRejectInvalidEventId() {
			assertAll(
					() -> assertThrows(IllegalArgumentException.class, () ->
							Reservation.create(
									USER_ID, null,
									List.of(createItem(1L)),
									CREATED_AT, EXPIRES_AT
							)
					),
					() -> assertThrows(IllegalArgumentException.class, () ->
							Reservation.create(
									USER_ID, 0L,
									List.of(createItem(1L)),
									CREATED_AT, EXPIRES_AT
							)
					),
					() -> assertThrows(IllegalArgumentException.class, () ->
							Reservation.create(
									USER_ID, -1L,
									List.of(createItem(1L)),
									CREATED_AT, EXPIRES_AT
							)
					)
			);
		}
	}

	@Nested
	class ReconstituteTests {

		@Test
		void shouldReconstituteReservationSuccessfully() {
			List<ReservationItem> items = List.of(
					createItem(1L),
					createItem(2L)
			);

			Reservation reservation = Reservation.reconstitute(
					RESERVATION_ID,
					USER_ID,
					EVENT_ID,
					items,
					ReservationStatus.CONFIRMED,
					CREATED_AT,
					UPDATED_AT,
					EXPIRES_AT
			);

			assertEquals(RESERVATION_ID, reservation.getId());
			assertEquals(USER_ID, reservation.getUserId());
			assertEquals(EVENT_ID, reservation.getEventId());
			assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
			assertEquals(CREATED_AT, reservation.getCreatedAt());
			assertEquals(UPDATED_AT, reservation.getUpdatedAt());
			assertEquals(EXPIRES_AT, reservation.getExpiresAt());
			assertEquals(2, reservation.getItems().size());
		}

		@Test
		void shouldRejectNullId() {
			assertThrows(NullPointerException.class, () ->
					Reservation.reconstitute(
							null, USER_ID, EVENT_ID,
							List.of(createItem(1L)),
							ReservationStatus.HELD,
							CREATED_AT, UPDATED_AT, EXPIRES_AT
					)
			);
		}

		@Test
		void shouldRejectInvalidId() {
			assertAll(
					() -> assertThrows(IllegalArgumentException.class, () ->
							Reservation.reconstitute(
									0L, USER_ID, EVENT_ID,
									List.of(createItem(1L)),
									ReservationStatus.HELD,
									CREATED_AT, UPDATED_AT, EXPIRES_AT
							)
					),
					() -> assertThrows(IllegalArgumentException.class, () ->
							Reservation.reconstitute(
									-1L, USER_ID, EVENT_ID,
									List.of(createItem(1L)),
									ReservationStatus.HELD,
									CREATED_AT, UPDATED_AT, EXPIRES_AT
							)
					)
			);
		}
	}

	@Nested
	class ItemsTests {

		@Test
		void shouldRejectNullItems() {
			assertThrows(NullPointerException.class, () ->
					Reservation.create(
							USER_ID, EVENT_ID,
							null, CREATED_AT, EXPIRES_AT
					)
			);
		}

		@Test
		void shouldRejectEmptyItems() {
			assertThrows(IllegalArgumentException.class, () ->
					Reservation.create(
							USER_ID, EVENT_ID,
							List.of(), CREATED_AT, EXPIRES_AT
					)
			);
		}

		@Test
		void shouldRejectNullItem() {
			List<ReservationItem> items =
					Arrays.asList(createItem(1L), null);

			assertThrows(NullPointerException.class, () ->
					Reservation.create(
							USER_ID, EVENT_ID,
							items, CREATED_AT, EXPIRES_AT
					)
			);
		}

		@Test
		void shouldRejectDuplicateTicketTypeId() {
			List<ReservationItem> items = List.of(
					createItem(1L),
					createItem(1L)
			);

			assertThrows(IllegalArgumentException.class, () ->
					Reservation.create(
							USER_ID, EVENT_ID,
							items, CREATED_AT, EXPIRES_AT
					)
			);
		}

		@Test
		void shouldAllowDifferentTicketTypeIds() {
			Reservation reservation = Reservation.create(
					USER_ID,
					EVENT_ID,
					List.of(
							createItem(1L),
							createItem(2L)
					),
					CREATED_AT,
					EXPIRES_AT
			);

			assertEquals(2, reservation.getItems().size());
		}
	}

	@Nested
	class ItemsImmutabilityTests {

		@Test
		void shouldDefensivelyCopyInputItems() {
			List<ReservationItem> items =
					new ArrayList<>(List.of(createItem(1L)));

			Reservation reservation = Reservation.create(
					USER_ID, EVENT_ID, items,
					CREATED_AT, EXPIRES_AT
			);

			items.add(createItem(2L));

			assertEquals(1, reservation.getItems().size());
		}

		@Test
		void shouldNotAllowExternalModificationOfItems() {
			Reservation reservation = createValidReservation();

			assertThrows(UnsupportedOperationException.class, () ->
					reservation.getItems().add(createItem(2L))
			);
		}

		@Test
		void shouldNotAllowExternalRemovalOfItems() {
			Reservation reservation = createValidReservation();

			assertThrows(UnsupportedOperationException.class, () ->
					reservation.getItems().clear()
			);
		}
	}

	@Nested
	class TimestampTests {

		@Test
		void shouldRejectNullCreatedAt() {
			assertThrows(NullPointerException.class, () ->
					Reservation.create(
							USER_ID, EVENT_ID,
							List.of(createItem(1L)),
							null, EXPIRES_AT
					)
			);
		}

		@Test
		void shouldRejectNullExpiresAt() {
			assertThrows(NullPointerException.class, () ->
					Reservation.create(
							USER_ID, EVENT_ID,
							List.of(createItem(1L)),
							CREATED_AT, null
					)
			);
		}

		@Test
		void shouldRejectExpiresAtEqualToCreatedAt() {
			assertThrows(IllegalArgumentException.class, () ->
					Reservation.create(
							USER_ID, EVENT_ID,
							List.of(createItem(1L)),
							CREATED_AT, CREATED_AT
					)
			);
		}

		@Test
		void shouldRejectExpiresAtBeforeCreatedAt() {
			assertThrows(IllegalArgumentException.class, () ->
					Reservation.create(
							USER_ID, EVENT_ID,
							List.of(createItem(1L)),
							EXPIRES_AT, CREATED_AT
					)
			);
		}

		@Test
		void shouldRejectNullUpdatedAtDuringReconstitution() {
			assertThrows(NullPointerException.class, () ->
					Reservation.reconstitute(
							RESERVATION_ID, USER_ID, EVENT_ID,
							List.of(createItem(1L)),
							ReservationStatus.HELD,
							CREATED_AT, null, EXPIRES_AT
					)
			);
		}

		@Test
		void shouldRejectUpdatedAtBeforeCreatedAt() {
			Instant invalidUpdatedAt =
					Instant.parse("2026-11-01T09:59:00Z");

			assertThrows(IllegalArgumentException.class, () ->
					Reservation.reconstitute(
							RESERVATION_ID, USER_ID, EVENT_ID,
							List.of(createItem(1L)),
							ReservationStatus.HELD,
							CREATED_AT, invalidUpdatedAt, EXPIRES_AT
					)
			);
		}

		@Test
		void shouldAllowUpdatedAtAfterCreatedAt() {
			Reservation reservation = Reservation.reconstitute(
					RESERVATION_ID,
					USER_ID,
					EVENT_ID,
					List.of(createItem(1L)),
					ReservationStatus.HELD,
					CREATED_AT,
					UPDATED_AT,
					EXPIRES_AT
			);

			assertEquals(UPDATED_AT, reservation.getUpdatedAt());
		}
	}

	@Nested
	class StatusTests {

		@Test
		void shouldRejectNullStatusDuringReconstitution() {
			assertThrows(NullPointerException.class, () ->
					Reservation.reconstitute(
							RESERVATION_ID, USER_ID, EVENT_ID,
							List.of(createItem(1L)),
							null,
							CREATED_AT, UPDATED_AT, EXPIRES_AT
					)
			);
		}
	}
}
