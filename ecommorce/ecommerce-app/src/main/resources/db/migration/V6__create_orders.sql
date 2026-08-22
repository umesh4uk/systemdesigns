-- =============================================================
-- V6 : Order context
-- =============================================================

CREATE TABLE orders (
    id                   UUID          NOT NULL DEFAULT gen_random_uuid(),
    order_number         VARCHAR(30)   NOT NULL,
    customer_id          UUID          NOT NULL,
    status               VARCHAR(25)   NOT NULL DEFAULT 'CREATED',

    -- Shipping address snapshot
    ship_line1           VARCHAR(255)  NOT NULL,
    ship_line2           VARCHAR(255),
    ship_city            VARCHAR(100)  NOT NULL,
    ship_state           VARCHAR(100),
    ship_postal_code     VARCHAR(20)   NOT NULL,
    ship_country         CHAR(2)       NOT NULL,

    -- Billing address snapshot
    bill_line1           VARCHAR(255)  NOT NULL,
    bill_line2           VARCHAR(255),
    bill_city            VARCHAR(100)  NOT NULL,
    bill_state           VARCHAR(100),
    bill_postal_code     VARCHAR(20)   NOT NULL,
    bill_country         CHAR(2)       NOT NULL,

    subtotal_amount      NUMERIC(19,4) NOT NULL,
    discount_amount      NUMERIC(19,4) NOT NULL DEFAULT 0,
    shipping_amount      NUMERIC(19,4) NOT NULL DEFAULT 0,
    total_amount         NUMERIC(19,4) NOT NULL,
    currency             CHAR(3)       NOT NULL DEFAULT 'USD',
    coupon_code          VARCHAR(50),
    tracking_number      VARCHAR(100),
    cancellation_reason  VARCHAR(500),

    created_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    version              BIGINT        NOT NULL DEFAULT 0,

    CONSTRAINT pk_orders           PRIMARY KEY (id),
    CONSTRAINT uq_order_number     UNIQUE (order_number),
    CONSTRAINT fk_order_customer   FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT chk_order_status    CHECK (status IN (
        'CREATED','PAYMENT_PENDING','CONFIRMED','PROCESSING',
        'SHIPPED','DELIVERED','CANCELLED','PAYMENT_FAILED',
        'RETURN_REQUESTED','RETURNED','REFUNDED'
    )),
    CONSTRAINT chk_order_total     CHECK (total_amount >= 0)
);

CREATE INDEX idx_orders_customer    ON orders (customer_id, created_at DESC);
CREATE INDEX idx_orders_status      ON orders (status, created_at DESC);
CREATE INDEX idx_orders_number      ON orders (order_number);

-- ---------------------------------------------------------------

CREATE TABLE order_items (
    id           UUID          NOT NULL DEFAULT gen_random_uuid(),
    order_id     UUID          NOT NULL,
    product_id   UUID          NOT NULL,
    sku          VARCHAR(64)   NOT NULL,
    product_name VARCHAR(300)  NOT NULL,
    quantity     INT           NOT NULL,
    unit_price   NUMERIC(19,4) NOT NULL,
    currency     CHAR(3)       NOT NULL DEFAULT 'USD',
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    version      BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_order_items     PRIMARY KEY (id),
    CONSTRAINT fk_item_order      FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT chk_item_quantity  CHECK (quantity > 0),
    CONSTRAINT chk_item_price     CHECK (unit_price >= 0)
);

CREATE INDEX idx_order_items_order   ON order_items (order_id);
CREATE INDEX idx_order_items_product ON order_items (product_id);
