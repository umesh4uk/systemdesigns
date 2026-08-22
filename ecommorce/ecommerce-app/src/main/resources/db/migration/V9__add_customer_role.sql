-- =============================================================
-- V9 : Add role column to customers table
--      Supports INVENTORY_MANAGER and ORDER_MANAGER staff roles
--      in addition to CUSTOMER and ADMIN.
-- =============================================================

ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS role VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER';

-- Ensure existing admin seed account has the ADMIN role
UPDATE customers
SET    role = 'ADMIN'
WHERE  email = 'admin@ecommerce.example.com';

ALTER TABLE customers
    ADD CONSTRAINT chk_customer_role
    CHECK (role IN ('CUSTOMER', 'ADMIN', 'INVENTORY_MANAGER', 'ORDER_MANAGER'));

CREATE INDEX idx_customers_role ON customers (role);
