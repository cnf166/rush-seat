package com.rushseat.data.entity;

import com.rushseat.business.domain.enums.EventStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
@Table(name = "events")
public class EventEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "created_by", nullable = false)
	private Long createdById;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EventStatus status;

	@Column(name = "starts_at", nullable = false)
	private Instant startsAt;

	@Column(name = "ends_at", nullable = false)
	private Instant endsAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public EventEntity() {
	}

	public EventEntity(Long id, Long createdById, String name, String description, EventStatus status, Instant startsAt, Instant endsAt, Instant createdAt) {
		this.id = id;
		this.createdById = createdById;
		this.name = name;
		this.description = description;
		this.status = status;
		this.startsAt = startsAt;
		this.endsAt = endsAt;
		this.createdAt = createdAt;
	}
}
