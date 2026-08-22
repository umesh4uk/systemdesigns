-- =============================================================
-- V1 : Identity / Customer context
-- =============================================================

CREATE TABLE customers (
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    email        VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name   VARCHAR(100) NOT NULL,
    last_name    VARCHAR(100) NOT NULL,
    phone        VARCHAR(20),
    status       VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version      BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_customers          PRIMARY KEY (id),
    CONSTRAINT uq_customer_email     UNIQUE (email),
    CONSTRAINT chk_customer_status   CHECK (status IN ('PENDING_VERIFICATION','ACTIVE','SUSPENDED','DEACTIVATED'))
);

CREATE INDEX idx_customers_email  ON customers (email);
CREATE INDEX idx_customers_status ON customers (status);

-- ---------------------------------------------------------------

CREATE TABLE customer_addresses (
    id             UUID         NOT NULL DEFAULT gen_random_uuid(),
    customer_id    UUID         NOT NULL,
    address_line1  VARCHAR(255) NOT NULL,
    address_line2  VARCHAR(255),
    city           VARCHAR(100) NOT NULL,
    state          VARCHAR(100),
    postal_code    VARCHAR(20)  NOT NULL,
    country        CHAR(2)      NOT NULL,
    address_type   VARCHAR(10)  NOT NULL DEFAULT 'SHIPPING',
    is_default     BOOLEAN      NOT NULL DEFAULT FALSE,
    label          VARCHAR(50),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version        BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_customer_addresses  PRIMARY KEY (id),
    CONSTRAINT fk_address_customer    FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE,
    CONSTRAINT chk_address_type       CHECK (address_type IN ('SHIPPING','BILLING','BOTH'))
);

CREATE INDEX idx_customer_addresses_customer ON customer_addresses (customer_id);
