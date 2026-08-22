-- =============================================================
-- V10 : Two-phase payment support
--       Adds authorization_id to store the provider's hold reference
--       before the payment is captured.
-- =============================================================

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS authorization_id VARCHAR(255);

COMMENT ON COLUMN payments.authorization_id IS
    'Provider authorization hold ID returned by authorize(). NULL until authorization succeeds.';

COMMENT ON COLUMN payments.provider_transaction_id IS
    'Provider capture/charge transaction ID. Populated after capture() succeeds.';
