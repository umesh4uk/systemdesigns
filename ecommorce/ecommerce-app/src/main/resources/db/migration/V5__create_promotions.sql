-- =============================================================
-- V5 : Promotion context — coupons
-- =============================================================

CREATE TABLE coupons (
    id                       UUID          NOT NULL DEFAULT gen_random_uuid(),
    code                     VARCHAR(50)   NOT NULL,
    description              VARCHAR(255),
    discount_type            VARCHAR(15)   NOT NULL,
    discount_value           NUMERIC(19,4) NOT NULL,
    minimum_order_amount     NUMERIC(19,4),
    maximum_discount_amount  NUMERIC(19,4),
    valid_from               TIMESTAMPTZ   NOT NULL,
    valid_until              TIMESTAMPTZ,
    max_usage_count          INT           NOT NULL DEFAULT 0,   -- 0 = unlimited
    max_usage_per_customer   INT           NOT NULL DEFAULT 0,   -- 0 = unlimited
    usage_count              INT           NOT NULL DEFAULT 0,
    active                   BOOLEAN       NOT NULL DEFAULT TRUE,
    currency                 CHAR(3)       NOT NULL DEFAULT 'USD',
    created_at               TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ   NOT NULL DEFAULT now(),
    version                  BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_coupons              PRIMARY KEY (id),
    CONSTRAINT uq_coupon_code          UNIQUE (code),
    CONSTRAINT chk_coupon_type         CHECK (discount_type IN ('PERCENTAGE','FIXED_AMOUNT')),
    CONSTRAINT chk_coupon_value        CHECK (discount_value > 0),
    CONSTRAINT chk_coupon_usage        CHECK (usage_count >= 0),
    CONSTRAINT chk_coupon_max_usage    CHECK (max_usage_count >= 0)
);

CREATE INDEX idx_coupons_code   ON coupons (code);
CREATE INDEX idx_coupons_active ON coupons (active, valid_from, valid_until);
