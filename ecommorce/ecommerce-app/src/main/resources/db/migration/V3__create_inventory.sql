-- =============================================================
-- V3 : Inventory context
-- =============================================================

CREATE TABLE inventory_items (
    id                  UUID        NOT NULL DEFAULT gen_random_uuid(),
    sku                 VARCHAR(64) NOT NULL,
    product_id          UUID        NOT NULL,
    warehouse_id        VARCHAR(100) NOT NULL DEFAULT 'WH-DEFAULT',
    available_quantity  INT         NOT NULL DEFAULT 0,
    reserved_quantity   INT         NOT NULL DEFAULT 0,
    reorder_threshold   INT         NOT NULL DEFAULT 5,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    version             BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_inventory_items         PRIMARY KEY (id),
    CONSTRAINT uq_inventory_sku_warehouse UNIQUE (sku, warehouse_id),
    CONSTRAINT chk_inventory_available    CHECK (available_quantity >= 0),
    CONSTRAINT chk_inventory_reserved     CHECK (reserved_quantity >= 0)
);

CREATE INDEX idx_inventory_sku        ON inventory_items (sku);
CREATE INDEX idx_inventory_product    ON inventory_items (product_id);
CREATE INDEX idx_inventory_warehouse  ON inventory_items (warehouse_id);
