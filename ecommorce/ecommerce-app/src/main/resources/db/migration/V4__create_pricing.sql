-- =============================================================
-- V4 : Pricing context
-- =============================================================

CREATE TABLE price_rules (
    id             UUID          NOT NULL DEFAULT gen_random_uuid(),
    product_id     UUID          NOT NULL,
    sku            VARCHAR(64)   NOT NULL,
    discount_type  VARCHAR(15)   NOT NULL,
    discount_value NUMERIC(19,4) NOT NULL,
    valid_from     TIMESTAMPTZ   NOT NULL,
    valid_until    TIMESTAMPTZ,
    active         BOOLEAN       NOT NULL DEFAULT TRUE,
    description    VARCHAR(255),
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    version        BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_price_rules         PRIMARY KEY (id),
    CONSTRAINT chk_discount_type      CHECK (discount_type IN ('FIXED','PERCENTAGE')),
    CONSTRAINT chk_discount_value     CHECK (discount_value >= 0),
    CONSTRAINT chk_percentage_max     CHECK (discount_type != 'PERCENTAGE' OR discount_value <= 100)
);

CREATE INDEX idx_price_rules_product ON price_rules (product_id, active, valid_from, valid_until);
