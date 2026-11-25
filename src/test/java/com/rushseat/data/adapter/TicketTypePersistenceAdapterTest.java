
package com.rushseat.data.adapter;

import com.rushseat.business.application.port.output.TicketTypeRepository;
import com.rushseat.business.domain.enums.EventStatus;
import com.rushseat.business.domain.enums.UserRole;
import com.rushseat.business.domain.model.TicketType;
import com.rushseat.data.entity.EventEntity;
import com.rushseat.data.entity.TicketTypeEntity;
import com.rushseat.data.entity.UserEntity;
import com.rushseat.data.repository.EventJpaRepository;
import com.rushseat.data.repository.TicketTypeJpaRepository;
import com.rushseat.data.repository.UserJpaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
		replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import(TicketTypePersistenceAdapter.class)
class TicketTypePersistenceAdapterTest {

	private static final Instant NOW =
			Instant.parse("2026-11-25T10:00:00Z");

	@Autowired
	private TicketTypeRepository ticketTypeRepository;

	@Autowired
	private TicketTypeJpaRepository ticketTypeJpaRepository;

	@Autowired
	private EventJpaRepository eventJpaRepository;

	@Autowired
	private UserJpaRepository userJpaRepository;

	private Long eventId;
	private Long anotherEventId;

	@BeforeEach
	void setUp() {
		UserEntity user = userJpaRepository.save(
				new UserEntity(
						null,
						"organizer@test.com",
						"password-hash",
						UserRole.ORGANIZER,
						NOW
				)
		);

		EventEntity event = eventJpaRepository.save(
				new EventEntity(
						null,
						user.getId(),
						"RushSeat Concert",
						"Test event",
						EventStatus.DRAFT,
						Instant.parse("2027-01-01T10:00:00Z"),
						Instant.parse("2027-01-01T12:00:00Z"),
						NOW
				)
		);

		EventEntity anotherEvent = eventJpaRepository.save(
				new EventEntity(
						null,
						user.getId(),
						"Another Concert",
						"Another test event",
						EventStatus.DRAFT,
						Instant.parse("2027-02-01T10:00:00Z"),
						Instant.parse("2027-02-01T12:00:00Z"),
						NOW
				)
		);

		eventId = event.getId();
		anotherEventId = anotherEvent.getId();
	}

	@Test
	void save_shouldPersistTicketTypeAndReturnDomainModel() {
		TicketType ticketType = createTicketType(
				eventId,
				"VIP",
				new BigDecimal("500000"),
				100
		);

		TicketType saved = ticketTypeRepository.save(ticketType);

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getEventId()).isEqualTo(eventId);
		assertThat(saved.getName()).isEqualTo("VIP");
		assertThat(saved.getDescription()).isEqualTo("VIP ticket");
		assertThat(saved.getPrice())
				.isEqualByComparingTo("500000");
		assertThat(saved.getTotalQuantity()).isEqualTo(100);
		assertThat(saved.getHeldQuantity()).isZero();
		assertThat(saved.getSoldQuantity()).isZero();
		assertThat(saved.getCreatedAt()).isEqualTo(NOW);
	}

	@Test
	void findById_shouldRestoreTicketTypeAndInventory() {
		TicketTypeEntity entity = ticketTypeJpaRepository.save(
				new TicketTypeEntity(
						null,
						eventId,
						"VIP",
						"VIP ticket",
						new BigDecimal("500000"),
						100,
						20,
						30,
						NOW
				)
		);

		Optional<TicketType> result =
				ticketTypeRepository.findById(entity.getId());

		assertThat(result).isPresent();

		TicketType ticketType = result.orElseThrow();

		assertThat(ticketType.getId()).isEqualTo(entity.getId());
		assertThat(ticketType.getEventId()).isEqualTo(eventId);
		assertThat(ticketType.getName()).isEqualTo("VIP");
		assertThat(ticketType.getPrice())
				.isEqualByComparingTo("500000");
		assertThat(ticketType.getTotalQuantity()).isEqualTo(100);
		assertThat(ticketType.getHeldQuantity()).isEqualTo(20);
		assertThat(ticketType.getSoldQuantity()).isEqualTo(30);
		assertThat(ticketType.getAvailableQuantity()).isEqualTo(50);
	}

	@Test
	void findById_shouldReturnEmptyWhenTicketTypeDoesNotExist() {
		Optional<TicketType> result =
				ticketTypeRepository.findById(Long.MAX_VALUE);

		assertThat(result).isEmpty();
	}

	@Test
	void findAllByEventId_shouldReturnOnlyTicketTypesOfRequestedEvent() {
		ticketTypeRepository.save(
				createTicketType(
						eventId,
						"VIP",
						new BigDecimal("500000"),
						100
				)
		);

		ticketTypeRepository.save(
				createTicketType(
						eventId,
						"Regular",
						new BigDecimal("200000"),
						200
				)
		);

		ticketTypeRepository.save(
				createTicketType(
						anotherEventId,
						"Early Bird",
						new BigDecimal("150000"),
						50
				)
		);

		List<TicketType> result =
				ticketTypeRepository.findAllByEventId(eventId);

		assertThat(result).hasSize(2);
		assertThat(result)
				.extracting(TicketType::getEventId)
				.containsOnly(eventId);

		assertThat(result)
				.extracting(TicketType::getName)
				.containsExactlyInAnyOrder("VIP", "Regular");
	}

	@Test
	void existsById_shouldReturnTrueWhenTicketTypeExists() {
		TicketType saved = ticketTypeRepository.save(
				createTicketType(
						eventId,
						"VIP",
						new BigDecimal("500000"),
						100
				)
		);

		assertThat(
				ticketTypeRepository.existsById(saved.getId())
		).isTrue();
	}

	@Test
	void existsById_shouldReturnFalseWhenTicketTypeDoesNotExist() {
		assertThat(
				ticketTypeRepository.existsById(Long.MAX_VALUE)
		).isFalse();
	}

	private TicketType createTicketType(
			Long eventId,
			String name,
			BigDecimal price,
			int totalQuantity
	) {
		return TicketType.create(
				eventId,
				name,
				name + " ticket",
				price,
				totalQuantity,
				NOW
		);
	}
}
