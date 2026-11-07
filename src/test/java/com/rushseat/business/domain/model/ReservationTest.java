
package com.rushseat.business.domain.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationTest {

	private static final Long RESERVATION_ID = 100L;
	private static final Long USER_ID = 1L;
	private static final Long EVENT_ID = 10L;

	private static final Instant CREATED_AT =
			Instant.parse("2026-11-01T10:00:00Z");

	private static final Instant UPDATED_AT =
			Instant.parse("2026-11-01T10:02:00Z");

	private static final Instant EXPIRES_AT =
			Instant.parse("2026-11-01T10:05:00Z");

	private static final Instant BEFORE_EXPIRATION =
			Instant.parse("2026-11-01T10:04:59Z");

	private static final Instant AT_EXPIRATION =
			EXPIRES_AT;

	private static final Instant AFTER_EXPIRATION =
			Instant.parse("2026-11-01T10:06:00Z");


	// =========================================================
	// Test fixtures
	// =========================================================

	private ReservationItem createItem(
			Long ticketTypeId,
			int quantity,
			long unitPrice
	) {
		return ReservationItem.create(
				ticketTypeId,
				quantity,
				unitPrice
		);
	}

	private Reservation createValidReservation() {
		return Reservation.create(
				USER_ID,
				EVENT_ID,
				List.of(createItem(1L, 2, 100_000L)),
				CREATED_AT,
				EXPIRES_AT
		);
	}

	private Reservation createReservation(
			ReservationStatus status,
			Instant updatedAt
	) {
		return Reservation.reconstitute(
				RESERVATION_ID,
				USER_ID,
				EVENT_ID,
				List.of(createItem(1L, 2, 100_000L)),
				status,
				CREATED_AT,
				updatedAt,
				EXPIRES_AT
		);
	}


	// =========================================================
	// BATCH A: Construction & Invariants
	// =========================================================

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
			assertEquals(CREATED_AT, reservation.getUpdatedAt());
			assertEquals(EXPIRES_AT, reservation.getExpiresAt());

			assertEquals(1, reservation.getItems().size());
		}

		@Test
		void shouldInitializeUpdatedAtWithCreatedAt() {
			Reservation reservation = createValidReservation();

			assertEquals(
					reservation.getCreatedAt(),
					reservation.getUpdatedAt()
			);
		}

		@Test
		void shouldInitializeStatusAsHeld() {
			Reservation reservation = createValidReservation();

			assertEquals(
					ReservationStatus.HELD,
					reservation.getStatus()
			);
		}
	}


	@Nested
	class ReconstituteTests {

		@Test
		void shouldReconstituteReservationSuccessfully() {
			List<ReservationItem> items = List.of(
					createItem(1L, 2, 100_000L),
					createItem(2L, 1, 300_000L)
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

			assertEquals(
					ReservationStatus.CONFIRMED,
					reservation.getStatus()
			);

			assertEquals(CREATED_AT, reservation.getCreatedAt());
			assertEquals(UPDATED_AT, reservation.getUpdatedAt());
			assertEquals(EXPIRES_AT, reservation.getExpiresAt());

			assertEquals(2, reservation.getItems().size());
		}

		@Test
		void shouldRestoreDifferentReservationStatuses() {
			for (ReservationStatus status : ReservationStatus.values()) {
				Reservation reservation = createReservation(
						status,
						UPDATED_AT
				);

				assertEquals(status, reservation.getStatus());
			}
		}

		@Test
		void shouldRejectNullId() {
			assertThrows(
					NullPointerException.class,
					() -> Reservation.reconstitute(
							null,
							USER_ID,
							EVENT_ID,
							List.of(createItem(1L, 1, 100_000L)),
							ReservationStatus.HELD,
							CREATED_AT,
							UPDATED_AT,
							EXPIRES_AT
					)
			);
		}

		@Test
		void shouldRejectZeroId() {
			assertThrows(
					IllegalArgumentException.class,
					() -> Reservation.reconstitute(
							0L,
							USER_ID,
							EVENT_ID,
							List.of(createItem(1L, 1, 100_000L)),
							ReservationStatus.HELD,
							CREATED_AT,
							UPDATED_AT,
							EXPIRES_AT
					)
			);
		}

		@Test
		void shouldRejectNegativeId() {
			assertThrows(
					IllegalArgumentException.class,
					() -> Reservation.reconstitute(
							-1L,
							USER_ID,
							EVENT_ID,
							List.of(createItem(1L, 1, 100_000L)),
							ReservationStatus.HELD,
							CREATED_AT,
							UPDATED_AT,
							EXPIRES_AT
					)
			);
		}
	}


	@Nested
	class IdentityTests {

		@Test
		void shouldRejectNullUserId() {
			assertThrows(
					IllegalArgumentException.class,
					() -> Reservation.create(
							null,
							EVENT_ID,
							List.of(createItem(1L, 1, 100_000L)),
							CREATED_AT,
							EXPIRES_AT
					)
			);
		}

		@Test
		void shouldRejectZeroUserId() {
			assertThrows(
					IllegalArgumentException.class,
					() -> Reservation.create(
							0L,
							EVENT_ID,
							List.of(createItem(1L, 1, 100_000L)),
							CREATED_AT,
							EXPIRES_AT
					)
			);
		}

		@Test
		void shouldRejectNegativeUserId() {
			assertThrows(
					IllegalArgumentException.class,
					() -> Reservation.create(
							-1L,
							EVENT_ID,
							List.of(createItem(1L, 1, 100_000L)),
							CREATED_AT,
							EXPIRES_AT
					)
			);
		}

		@Test
		void shouldRejectNullEventId() {
			assertThrows(
					IllegalArgumentException.class,
					() -> Reservation.create(
							USER_ID,
							null,
							List.of(createItem(1L, 1, 100_000L)),
							CREATED_AT,
							EXPIRES_AT
					)
			);
		}

		@Test
		void shouldRejectZeroEventId() {
			assertThrows(
					IllegalArgumentException.class,
					() -> Reservation.create(
							USER_ID,
							0L,
							List.of(createItem(1L, 1, 100_000L)),
							CREATED_AT,
							EXPIRES_AT
					)
			);
		}

		@Test
		void shouldRejectNegativeEventId() {
			assertThrows(
					IllegalArgumentException.class,
					() -> Reservation.create(
							USER_ID,
							-1L,
							List.of(createItem(1L, 1, 100_000L)),
							CREATED_AT,
							EXPIRES_AT
					)
			);
		}
	}


	@Nested
	class ItemsTests {

		@Test
		void shouldRejectNullItems() {
			assertThrows(
					NullPointerException.class,
					() -> Reservation.create(
							USER_ID,
							EVENT_ID,
							null,
							CREATED_AT,
							EXPIRES_AT
					)
			);
		}

		@Test
		void shouldRejectEmptyItems() {
			assertThrows(
					IllegalArgumentException.class,
					() -> Reservation.create(
							USER_ID,
							EVENT_ID,
							List.of(),
							CREATED_AT,
							EXPIRES_AT
					)
			);
		}

		@Test
		void shouldRejectNullItemInsideList() {
			List<ReservationItem> items =
					Arrays.asList(createItem(1L, 1, 100_000L), null);

			assertThrows(
					NullPointerException.class,
					() -> Reservation.create(
							USER_ID,
							EVENT_ID,
							items,
							CREATED_AT,
							EXPIRES_AT
					)
			);
		}

		@Test
		void shouldRejectDuplicateTicketTypeId() {
			List<ReservationItem> items = List.of(
					createItem(1L, 2, 100_000L),
					createItem(1L, 3, 100_000L)
			);

			assertThrows(
					IllegalArgumentException.class,
					() -> Reservation.create(
							USER_ID,
							EVENT_ID,
							items,
							CREATED_AT,
							EXPIRES_AT
					)
			);
		}

		@Test
		void shouldAllowDifferentTicketTypeIds() {
			Reservation reservation = Reservation.create(
					USER_ID,
					EVENT_ID,
					List.of(
							createItem(1L, 2, 100_000L),
							createItem(2L, 1, 300_000L)
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
			List<ReservationItem> items = new ArrayList<>(
					List.of(createItem(1L, 1, 100_000L))
			);

			Reservation reservation = Reservation.create(
					USER_ID,
					EVENT_ID,
					items,
					CREATED_AT,
					EXPIRES_AT
			);

			items.add(createItem(2L, 1, 200_000L));

			assertEquals(1, reservation.getItems().size());
		}

		@Test
		void shouldNotAllowExternalAdditionOfItems() {
			Reservation reservation = createValidReservation();

			assertThrows(
					UnsupportedOperationException.class,
					() -> reservation.getItems().add(
							createItem(2L, 1, 200_000L)
					)
			);
		}

		@Test
		void shouldNotAllowExternalRemovalOfItems() {
			Reservation reservation = createValidReservation();

			assertThrows(
					UnsupportedOperationException.class,
					() -> reservation.getItems().clear()
			);
		}

		@Test
		void shouldNotAllowExternalModificationThroughIterator() {
			Reservation reservation = createValidReservation();

			assertThrows(
					UnsupportedOperationException.class,
					() -> reservation.getItems().iterator().remove()
			);
		}
	}


	@Nested
	class TimestampTests {

		@Test
		void shouldRejectNullCreatedAt() {
			assertThrows(
					NullPointerException.class,
					() -> Reservation.create(
							USER_ID,
							EVENT_ID,
							List.of(createItem(1L, 1, 100_000L)),
							null,
							EXPIRES_AT
					)
			);
		}

		@Test
		void shouldRejectNullExpiresAt() {
			assertThrows(
					NullPointerException.class,
					() -> Reservation.create(
							USER_ID,
							EVENT_ID,
							List.of(createItem(1L, 1, 100_000L)),
							CREATED_AT,
							null
					)
			);
		}

		@Test
		void shouldRejectExpiresAtEqualToCreatedAt() {
			assertThrows(
					IllegalArgumentException.class,
					() -> Reservation.create(
							USER_ID,
							EVENT_ID,
							List.of(createItem(1L, 1, 100_000L)),
							CREATED_AT,
							CREATED_AT
					)
			);
		}

		@Test
		void shouldRejectExpiresAtBeforeCreatedAt() {
			assertThrows(
					IllegalArgumentException.class,
					() -> Reservation.create(
							USER_ID,
							EVENT_ID,
							List.of(createItem(1L, 1, 100_000L)),
							EXPIRES_AT,
							CREATED_AT
					)
			);
		}

		@Test
		void shouldRejectNullUpdatedAtDuringReconstitution() {
			assertThrows(
					NullPointerException.class,
					() -> Reservation.reconstitute(
							RESERVATION_ID,
							USER_ID,
							EVENT_ID,
							List.of(createItem(1L, 1, 100_000L)),
							ReservationStatus.HELD,
							CREATED_AT,
							null,
							EXPIRES_AT
					)
			);
		}

		@Test
		void shouldRejectUpdatedAtBeforeCreatedAt() {
			Instant invalidUpdatedAt =
					Instant.parse("2026-11-01T09:59:00Z");

			assertThrows(
					IllegalArgumentException.class,
					() -> Reservation.reconstitute(
							RESERVATION_ID,
							USER_ID,
							EVENT_ID,
							List.of(createItem(1L, 1, 100_000L)),
							ReservationStatus.HELD,
							CREATED_AT,
							invalidUpdatedAt,
							EXPIRES_AT
					)
			);
		}

		@Test
		void shouldAllowUpdatedAtEqualToCreatedAt() {
			Reservation reservation = Reservation.reconstitute(
					RESERVATION_ID,
					USER_ID,
					EVENT_ID,
					List.of(createItem(1L, 1, 100_000L)),
					ReservationStatus.HELD,
					CREATED_AT,
					CREATED_AT,
					EXPIRES_AT
			);

			assertEquals(CREATED_AT, reservation.getUpdatedAt());
		}

		@Test
		void shouldAllowUpdatedAtAfterCreatedAt() {
			Reservation reservation = Reservation.reconstitute(
					RESERVATION_ID,
					USER_ID,
					EVENT_ID,
					List.of(createItem(1L, 1, 100_000L)),
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
			assertThrows(
					NullPointerException.class,
					() -> Reservation.reconstitute(
							RESERVATION_ID,
							USER_ID,
							EVENT_ID,
							List.of(createItem(1L, 1, 100_000L)),
							null,
							CREATED_AT,
							UPDATED_AT,
							EXPIRES_AT
					)
			);
		}
	}


	// =========================================================
	// BATCH B: Behavior & State Transitions
	// =========================================================

	@Nested
	class GetTotalAmountTests {

		@Test
		void shouldCalculateTotalAmountForSingleItem() {
			Reservation reservation = createValidReservation();

			assertEquals(200_000L, reservation.getTotalAmount());
		}

		@Test
		void shouldCalculateTotalAmountForMultipleItems() {
			Reservation reservation = Reservation.create(
					USER_ID,
					EVENT_ID,
					List.of(
							createItem(1L, 2, 100_000L),
							createItem(2L, 1, 300_000L),
							createItem(3L, 3, 50_000L)
					),
					CREATED_AT,
					EXPIRES_AT
			);

			assertEquals(650_000L, reservation.getTotalAmount());
		}

		@Test
		void shouldReturnZeroWhenAllItemsHaveZeroPrice() {
			Reservation reservation = Reservation.create(
					USER_ID,
					EVENT_ID,
					List.of(
							createItem(1L, 2, 0L),
							createItem(2L, 3, 0L)
					),
					CREATED_AT,
					EXPIRES_AT
			);

			assertEquals(0L, reservation.getTotalAmount());
		}

		@Test
		void shouldThrowArithmeticExceptionWhenSubtotalOverflows() {
			Reservation reservation = Reservation.create(
					USER_ID,
					EVENT_ID,
					List.of(
							createItem(
									1L,
									Integer.MAX_VALUE,
									Long.MAX_VALUE
							)
					),
					CREATED_AT,
					EXPIRES_AT
			);

			assertThrows(
					ArithmeticException.class,
					reservation::getTotalAmount
			);
		}

		@Test
		void shouldThrowArithmeticExceptionWhenTotalAmountOverflows() {
			// Each subtotal is within Long.MAX_VALUE,
			// but their sum exceeds Long.MAX_VALUE.

			long unitPrice = 999_999_999_999_999L;
			int quantity = 9_000;

			Reservation reservation = Reservation.create(
					USER_ID,
					EVENT_ID,
					List.of(
							createItem(1L, quantity, unitPrice),
							createItem(2L, quantity, unitPrice)
					),
					CREATED_AT,
					EXPIRES_AT
			);

			assertThrows(
					ArithmeticException.class,
					reservation::getTotalAmount
			);
		}
	}


	@Nested
	class ConfirmTests {

		@Test
		void shouldConfirmHeldReservationBeforeExpiration() {
			Reservation reservation = createValidReservation();

			reservation.confirm(BEFORE_EXPIRATION);

			assertEquals(
					ReservationStatus.CONFIRMED,
					reservation.getStatus()
			);

			assertEquals(
					BEFORE_EXPIRATION,
					reservation.getUpdatedAt()
			);
		}

		@Test
		void shouldAllowConfirmationWhenNowEqualsUpdatedAt() {
			Reservation reservation = createValidReservation();

			reservation.confirm(CREATED_AT);

			assertEquals(
					ReservationStatus.CONFIRMED,
					reservation.getStatus()
			);

			assertEquals(CREATED_AT, reservation.getUpdatedAt());
		}

		@Test
		void shouldRejectConfirmationAtExpirationTime() {
			Reservation reservation = createValidReservation();

			assertThrows(
					IllegalStateException.class,
					() -> reservation.confirm(AT_EXPIRATION)
			);
		}

		@Test
		void shouldRejectConfirmationAfterExpiration() {
			Reservation reservation = createValidReservation();

			assertThrows(
					IllegalStateException.class,
					() -> reservation.confirm(AFTER_EXPIRATION)
			);
		}

		@Test
		void shouldRejectConfirmationWhenStatusIsNotHeld() {
			Reservation reservation = createReservation(
					ReservationStatus.CONFIRMED,
					UPDATED_AT
			);

			assertThrows(
					IllegalStateException.class,
					() -> reservation.confirm(BEFORE_EXPIRATION)
			);
		}

		@Test
		void shouldRejectConfirmationWhenNowIsBeforeUpdatedAt() {
			Reservation reservation = createReservation(
					ReservationStatus.HELD,
					UPDATED_AT
			);

			Instant invalidNow =
					Instant.parse("2026-11-01T10:01:00Z");

			assertThrows(
					IllegalArgumentException.class,
					() -> reservation.confirm(invalidNow)
			);
		}

		@Test
		void shouldRejectConfirmationWhenNowIsNull() {
			Reservation reservation = createValidReservation();

			assertThrows(
					NullPointerException.class,
					() -> reservation.confirm(null)
			);
		}
	}


	@Nested
	class CancelTests {

		@Test
		void shouldCancelHeldReservationBeforeExpiration() {
			Reservation reservation = createValidReservation();

			reservation.cancel(BEFORE_EXPIRATION);

			assertEquals(
					ReservationStatus.CANCELLED,
					reservation.getStatus()
			);

			assertEquals(
					BEFORE_EXPIRATION,
					reservation.getUpdatedAt()
			);
		}

		@Test
		void shouldAllowCancellationWhenNowEqualsUpdatedAt() {
			Reservation reservation = createValidReservation();

			reservation.cancel(CREATED_AT);

			assertEquals(
					ReservationStatus.CANCELLED,
					reservation.getStatus()
			);

			assertEquals(CREATED_AT, reservation.getUpdatedAt());
		}

		@Test
		void shouldRejectCancellationAtExpirationTime() {
			Reservation reservation = createValidReservation();

			assertThrows(
					IllegalStateException.class,
					() -> reservation.cancel(AT_EXPIRATION)
			);
		}

		@Test
		void shouldRejectCancellationAfterExpiration() {
			Reservation reservation = createValidReservation();

			assertThrows(
					IllegalStateException.class,
					() -> reservation.cancel(AFTER_EXPIRATION)
			);
		}

		@Test
		void shouldRejectCancellationWhenStatusIsNotHeld() {
			Reservation reservation = createReservation(
					ReservationStatus.CONFIRMED,
					UPDATED_AT
			);

			assertThrows(
					IllegalStateException.class,
					() -> reservation.cancel(BEFORE_EXPIRATION)
			);
		}

		@Test
		void shouldRejectCancellationWhenNowIsBeforeUpdatedAt() {
			Reservation reservation = createReservation(
					ReservationStatus.HELD,
					UPDATED_AT
			);

			Instant invalidNow =
					Instant.parse("2026-11-01T10:01:00Z");

			assertThrows(
					IllegalArgumentException.class,
					() -> reservation.cancel(invalidNow)
			);
		}

		@Test
		void shouldRejectCancellationWhenNowIsNull() {
			Reservation reservation = createValidReservation();

			assertThrows(
					NullPointerException.class,
					() -> reservation.cancel(null)
			);
		}
	}


	@Nested
	class ExpireTests {

		@Test
		void shouldExpireHeldReservationAtExpirationTime() {
			Reservation reservation = createValidReservation();

			reservation.expire(AT_EXPIRATION);

			assertEquals(
					ReservationStatus.EXPIRED,
					reservation.getStatus()
			);

			assertEquals(
					AT_EXPIRATION,
					reservation.getUpdatedAt()
			);
		}

		@Test
		void shouldExpireHeldReservationAfterExpiration() {
			Reservation reservation = createValidReservation();

			reservation.expire(AFTER_EXPIRATION);

			assertEquals(
					ReservationStatus.EXPIRED,
					reservation.getStatus()
			);

			assertEquals(
					AFTER_EXPIRATION,
					reservation.getUpdatedAt()
			);
		}

		@Test
		void shouldRejectExpirationBeforeExpiresAt() {
			Reservation reservation = createValidReservation();

			assertThrows(
					IllegalStateException.class,
					() -> reservation.expire(BEFORE_EXPIRATION)
			);
		}

		@Test
		void shouldRejectExpirationWhenStatusIsNotHeld() {
			Reservation reservation = createReservation(
					ReservationStatus.CONFIRMED,
					UPDATED_AT
			);

			assertThrows(
					IllegalStateException.class,
					() -> reservation.expire(AFTER_EXPIRATION)
			);
		}

		@Test
		void shouldRejectExpirationWhenNowIsBeforeUpdatedAt() {
			Reservation reservation = createReservation(
					ReservationStatus.HELD,
					UPDATED_AT
			);

			Instant invalidNow =
					Instant.parse("2026-11-01T10:01:00Z");

			assertThrows(
					IllegalArgumentException.class,
					() -> reservation.expire(invalidNow)
			);
		}

		@Test
		void shouldRejectExpirationWhenNowIsNull() {
			Reservation reservation = createValidReservation();

			assertThrows(
					NullPointerException.class,
					() -> reservation.expire(null)
			);
		}
	}


	@Nested
	class StateConsistencyTests {

		@Test
		void shouldNotChangeStateWhenConfirmationFails() {
			Reservation reservation = createValidReservation();

			ReservationStatus originalStatus =
					reservation.getStatus();

			Instant originalUpdatedAt =
					reservation.getUpdatedAt();

			assertThrows(
					IllegalStateException.class,
					() -> reservation.confirm(AT_EXPIRATION)
			);

			assertEquals(originalStatus, reservation.getStatus());
			assertEquals(originalUpdatedAt, reservation.getUpdatedAt());
		}

		@Test
		void shouldNotChangeStateWhenCancellationFails() {
			Reservation reservation = createValidReservation();

			ReservationStatus originalStatus =
					reservation.getStatus();

			Instant originalUpdatedAt =
					reservation.getUpdatedAt();

			assertThrows(
					IllegalStateException.class,
					() -> reservation.cancel(AT_EXPIRATION)
			);

			assertEquals(originalStatus, reservation.getStatus());
			assertEquals(originalUpdatedAt, reservation.getUpdatedAt());
		}

		@Test
		void shouldNotChangeStateWhenExpirationFails() {
			Reservation reservation = createValidReservation();

			ReservationStatus originalStatus =
					reservation.getStatus();

			Instant originalUpdatedAt =
					reservation.getUpdatedAt();

			assertThrows(
					IllegalStateException.class,
					() -> reservation.expire(BEFORE_EXPIRATION)
			);

			assertEquals(originalStatus, reservation.getStatus());
			assertEquals(originalUpdatedAt, reservation.getUpdatedAt());
		}

		@Test
		void shouldNotChangeStateWhenTransitionTimeIsInvalid() {
			Reservation reservation = createReservation(
					ReservationStatus.HELD,
					UPDATED_AT
			);

			ReservationStatus originalStatus =
					reservation.getStatus();

			Instant originalUpdatedAt =
					reservation.getUpdatedAt();

			Instant invalidNow =
					Instant.parse("2026-11-01T10:01:00Z");

			assertThrows(
					IllegalArgumentException.class,
					() -> reservation.confirm(invalidNow)
			);

			assertEquals(originalStatus, reservation.getStatus());
			assertEquals(originalUpdatedAt, reservation.getUpdatedAt());
		}
	}
}
