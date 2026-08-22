-- =============================================================
-- V2 : Catalog context — categories, products, images, attributes
-- =============================================================

CREATE TABLE categories (
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    name          VARCHAR(150) NOT NULL,
    slug          VARCHAR(150) NOT NULL,
    description   VARCHAR(1000),
    image_url     VARCHAR(1024),
    parent_id     UUID,
    display_order INT          NOT NULL DEFAULT 0,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version       BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_categories       PRIMARY KEY (id),
    CONSTRAINT uq_category_slug    UNIQUE (slug),
    CONSTRAINT fk_category_parent  FOREIGN KEY (parent_id) REFERENCES categories (id) ON DELETE SET NULL
);

CREATE INDEX idx_categories_parent ON categories (parent_id);
CREATE INDEX idx_categories_slug   ON categories (slug);

-- ---------------------------------------------------------------

CREATE TABLE products (
    id                UUID          NOT NULL DEFAULT gen_random_uuid(),
    sku               VARCHAR(64)   NOT NULL,
    name              VARCHAR(300)  NOT NULL,
    description       TEXT,
    short_description VARCHAR(500),
    category_id       UUID,
    brand             VARCHAR(150),
    base_price        NUMERIC(19,4) NOT NULL,
    currency          CHAR(3)       NOT NULL DEFAULT 'USD',
    status            VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    weight_grams      INT,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    version           BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_products          PRIMARY KEY (id),
    CONSTRAINT uq_product_sku       UNIQUE (sku),
    CONSTRAINT fk_product_category  FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL,
    CONSTRAINT chk_product_status   CHECK (status IN ('DRAFT','ACTIVE','ARCHIVED','DELETED')),
    CONSTRAINT chk_product_price    CHECK (base_price >= 0)
);

CREATE INDEX idx_products_sku      ON products (sku);
CREATE INDEX idx_products_status   ON products (status);
CREATE INDEX idx_products_category ON products (category_id);
CREATE INDEX idx_products_brand    ON products (LOWER(brand));

-- Full-text search index
CREATE INDEX idx_products_search ON products
    USING GIN (to_tsvector('english', name || ' ' || COALESCE(description,'') || ' ' || COALESCE(brand,'')));

-- ---------------------------------------------------------------

CREATE TABLE product_images (
    id            UUID          NOT NULL DEFAULT gen_random_uuid(),
    product_id    UUID          NOT NULL,
    url           VARCHAR(1024) NOT NULL,
    alt_text      VARCHAR(255),
    display_order INT           NOT NULL DEFAULT 0,
    is_primary    BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    version       BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_product_images   PRIMARY KEY (id),
    CONSTRAINT fk_image_product    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

CREATE INDEX idx_product_images_product ON product_images (product_id);

-- ---------------------------------------------------------------

CREATE TABLE product_attributes (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    product_id      UUID         NOT NULL,
    attribute_key   VARCHAR(100) NOT NULL,
    attribute_value VARCHAR(500) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version         BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_product_attributes  PRIMARY KEY (id),
    CONSTRAINT uq_product_attribute   UNIQUE (product_id, attribute_key),
    CONSTRAINT fk_attribute_product   FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

CREATE INDEX idx_product_attributes_product ON product_attributes (product_id);
