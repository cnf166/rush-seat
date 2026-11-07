CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
        CHECK (role IN ('CUSTOMER', 'ORGANIZER', 'ADMIN')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    created_by BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'CANCELLED')),
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_events_time
        CHECK (ends_at > starts_at)
);

CREATE TABLE ticket_types (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events(id),
    name VARCHAR(100) NOT NULL,
    price NUMERIC(15, 0) NOT NULL
        CHECK (price >= 0),
    total_quantity INTEGER NOT NULL
        CHECK (total_quantity >= 0),
    held_quantity INTEGER NOT NULL DEFAULT 0
        CHECK (held_quantity >= 0),
    sold_quantity INTEGER NOT NULL DEFAULT 0
        CHECK (sold_quantity >= 0),

    CONSTRAINT chk_ticket_inventory
        CHECK (held_quantity + sold_quantity <= total_quantity),

    CONSTRAINT uq_ticket_types_event_name
        UNIQUE (event_id, name)
);

CREATE TABLE reservations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    event_id BIGINT NOT NULL REFERENCES events(id),

    status VARCHAR(30) NOT NULL
        CHECK (status IN (
            'HELD',
            'CONFIRMED',
            'CANCELLED',
            'EXPIRED',
            'CANCELLATION_PENDING'
        )),

    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_reservations_time
        CHECK (created_at < expires_at),

    CONSTRAINT chk_reservations_updated_at
        CHECK (updated_at >= created_at)
);

CREATE TABLE reservation_items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reservation_id BIGINT NOT NULL
        REFERENCES reservations(id),
    ticket_type_id BIGINT NOT NULL
        REFERENCES ticket_types(id),
    quantity INTEGER NOT NULL
        CHECK (quantity > 0),
    unit_price NUMERIC(15, 0) NOT NULL
        CHECK (unit_price >= 0),

    CONSTRAINT uq_reservation_items_type
        UNIQUE (reservation_id, ticket_type_id)
);

-- Indexes for common query patterns

CREATE INDEX idx_events_created_by
    ON events(created_by);

CREATE INDEX idx_events_status
    ON events(status);

CREATE INDEX idx_ticket_types_event_id
    ON ticket_types(event_id);

CREATE INDEX idx_reservations_user_id
    ON reservations(user_id);

CREATE INDEX idx_reservations_event_id
    ON reservations(event_id);

CREATE INDEX idx_reservations_status_expires_at
    ON reservations(status, expires_at);

CREATE INDEX idx_reservation_items_ticket_type_id
    ON reservation_items(ticket_type_id);
