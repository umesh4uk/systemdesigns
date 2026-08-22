package com.ecommerce.promotion.domain.event;

import com.ecommerce.shared.domain.event.BaseDomainEvent;
import java.util.UUID;

public final class CouponAppliedEvent extends BaseDomainEvent {
    private final UUID couponId;
    private final String code;

    public CouponAppliedEvent(UUID couponId, String code) {
        super();
        this.couponId = couponId;
        this.code = code;
    }

    @Override public String eventType() { return "promotion.coupon.applied"; }
    public UUID getCouponId() { return couponId; }
    public String getCode()   { return code; }
}
