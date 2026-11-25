package com.rushseat.data.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
@Table(name = "ticket_types")
public class TicketTypeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "event_id", nullable = false)
	private Long eventId;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "price", nullable = false)
	private Long price;

	@Column(name = "total_quantity", nullable = false)
	private Integer totalQuantity;

	@Column(name = "held_quantity", nullable = false)
	private Integer heldQuantity;

	@Column(name = "sold_quantity", nullable = false)
	private Integer soldQuantity;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected TicketTypeEntity() {
	// JPA
	}

	public TicketTypeEntity(Long id, Long eventId, String name, String description, Long price, Integer totalQuantity, Integer heldQuantity, Integer soldQuantity, Instant createdAt) {
		this.id = id;
		this.eventId = eventId;
		this.name = name;
		this.description = description;
		this.price = price;
		this.totalQuantity = totalQuantity;
		this.heldQuantity = heldQuantity;
		this.soldQuantity = soldQuantity;
		this.createdAt = createdAt;
	}
}
