package com.ecommerce.order.domain.model;

import com.ecommerce.shared.domain.model.BaseEntity;
import com.ecommerce.shared.domain.valueobject.Money;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * An order line item. Price is captured at placement time — immutable.
 */
@Getter
@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private Order order;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "sku", nullable = false, updatable = false, length = 64)
    private String sku;

    @Column(name = "product_name", nullable = false, updatable = false, length = 300)
    private String productName;

    @Column(name = "quantity", nullable = false, updatable = false)
    private int quantity;

    /** Unit price at the time of purchase — never changes. */
    @Column(name = "unit_price", nullable = false, updatable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "currency", nullable = false, updatable = false, length = 3)
    private String currency;

    protected OrderItem() {}

    OrderItem(Order order, UUID productId, String sku, String productName,
              int quantity, Money unitPrice) {
        super();
        this.order       = order;
        this.productId   = productId;
        this.sku         = sku;
        this.productName = productName;
        this.quantity    = quantity;
        this.unitPrice   = unitPrice.getAmount();
        this.currency    = unitPrice.getCurrencyCode();
    }

    public Money getUnitPriceMoney() {
        return Money.of(unitPrice, currency);
    }

    public Money getLineTotal() {
        return getUnitPriceMoney().multiply(quantity);
    }
}
