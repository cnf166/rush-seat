package com.rushseat.data.adapter;

import com.rushseat.business.application.port.output.TicketTypeRepository;
import com.rushseat.business.domain.model.TicketType;
import com.rushseat.data.entity.TicketTypeEntity;
import com.rushseat.data.mapper.TicketTypeMapper;
import com.rushseat.data.repository.TicketTypeJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TicketTypePersistenceAdapter implements TicketTypeRepository {
	private final TicketTypeJpaRepository ticketTypeJpaRepository;

	public TicketTypePersistenceAdapter(TicketTypeJpaRepository ticketTypeJpaRepository) {
		this.ticketTypeJpaRepository = ticketTypeJpaRepository;
	}

	@Override
	public TicketType save(TicketType ticketType) {
		TicketTypeEntity ticketTypeEntity = TicketTypeMapper.toEntity(ticketType);
		TicketTypeEntity savedTicketTypeEntity = ticketTypeJpaRepository.save(ticketTypeEntity);
		return TicketTypeMapper.toDomain(savedTicketTypeEntity);
	}

	@Override
	public Optional<TicketType> findById(Long id) {
		return ticketTypeJpaRepository.findById(id)
				.map(TicketTypeMapper::toDomain);
	}

	@Override
	public List<TicketType> findAllByEventId(Long eventId) {
		return ticketTypeJpaRepository.findAllByEventId(eventId)
				.stream()
				.map(TicketTypeMapper::toDomain)
				.toList();
	}

	@Override
	public boolean existsById(Long id) {
		return ticketTypeJpaRepository.existsById(id);
	}
}
