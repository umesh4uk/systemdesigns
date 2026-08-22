-- =============================================================
-- V7 : Payment context
-- =============================================================

CREATE TABLE payments (
    id                       UUID          NOT NULL DEFAULT gen_random_uuid(),
    order_id                 UUID          NOT NULL,
    customer_id              UUID          NOT NULL,
    idempotency_key          VARCHAR(100)  NOT NULL,
    status                   VARCHAR(25)   NOT NULL DEFAULT 'PENDING',
    amount                   NUMERIC(19,4) NOT NULL,
    currency                 CHAR(3)       NOT NULL DEFAULT 'USD',
    provider                 VARCHAR(50)   NOT NULL,
    provider_transaction_id  VARCHAR(255),
    failure_reason           VARCHAR(500),
    refunded_amount          NUMERIC(19,4) NOT NULL DEFAULT 0,
    created_at               TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ   NOT NULL DEFAULT now(),
    version                  BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_payments              PRIMARY KEY (id),
    CONSTRAINT uq_payment_idempotency   UNIQUE (idempotency_key),
    CONSTRAINT fk_payment_order         FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_payment_customer      FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT chk_payment_status       CHECK (status IN (
        'PENDING','PROCESSING','COMPLETED','FAILED','REFUNDED','PARTIALLY_REFUNDED'
    )),
    CONSTRAINT chk_payment_amount       CHECK (amount > 0),
    CONSTRAINT chk_payment_refund       CHECK (refunded_amount >= 0 AND refunded_amount <= amount)
);

CREATE INDEX idx_payments_order    ON payments (order_id);
CREATE INDEX idx_payments_customer ON payments (customer_id);
CREATE INDEX idx_payments_status   ON payments (status);
