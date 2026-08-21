package com.ecommerce.shared.exception;

/**
 * Thrown when a stock reservation or purchase exceeds available inventory.
 * Results in 422 Unprocessable Entity at the API layer.
 */
public class InsufficientStockException extends RuntimeException {

    private final String sku;
    private final int requested;
    private final int available;

    public InsufficientStockException(String sku, int requested, int available) {
        super("Insufficient stock for SKU " + sku
                + ": requested=" + requested + ", available=" + available);
        this.sku = sku;
        this.requested = requested;
        this.available = available;
    }

    public String getSku() { return sku; }
    public int getRequested() { return requested; }
    public int getAvailable() { return available; }
}
