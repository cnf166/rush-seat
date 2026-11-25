package com.rushseat.business.application.port.output;

import com.rushseat.business.domain.model.Reservation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

	Reservation save(Reservation reservation);

	Optional<Reservation> findById(Long id);

	List<Reservation> findAllByEventId(Long eventId);

	List<Reservation> findAllByUserId(Long userId);

	List<Reservation> findExpiredHeldReservations(Instant now); // scheduler

}
