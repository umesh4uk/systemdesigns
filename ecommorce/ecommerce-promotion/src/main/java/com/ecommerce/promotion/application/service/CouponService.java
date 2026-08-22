package com.ecommerce.promotion.application.service;

import com.ecommerce.promotion.application.dto.CouponRequest;
import com.ecommerce.promotion.application.dto.CouponResponse;
import com.ecommerce.promotion.application.dto.ApplyCouponResult;
import com.ecommerce.promotion.domain.model.Coupon;
import com.ecommerce.promotion.domain.repository.CouponRepository;
import com.ecommerce.shared.domain.valueobject.Money;
import com.ecommerce.shared.exception.ConflictException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        if (couponRepository.existsByCode(request.code())) {
            throw new ConflictException("Coupon code already exists: " + request.code());
        }
        Coupon coupon = Coupon.create(
                request.code(), request.description(), request.discountType(),
                request.discountValue(), request.minimumOrderAmount(),
                request.maximumDiscountAmount(), request.validFrom(), request.validUntil(),
                request.maxUsageCount(), request.maxUsagePerCustomer(), request.currency());
        return toResponse(couponRepository.save(coupon));
    }

    @Transactional
    public ApplyCouponResult applyCoupon(String code, Money orderTotal, int customerUsages) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code:" + code));
        Money discount = coupon.apply(orderTotal, customerUsages);
        Coupon saved = couponRepository.save(coupon);
        saved.getDomainEvents().forEach(eventPublisher::publishEvent);
        saved.clearDomainEvents();
        return new ApplyCouponResult(coupon.getId(), code, discount, orderTotal.subtract(discount));
    }

    @Transactional(readOnly = true)
    public CouponResponse getCoupon(String code) {
        return toResponse(couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code:" + code)));
    }

    @Transactional
    public void deactivateCoupon(UUID id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", id));
        coupon.deactivate();
        couponRepository.save(coupon);
    }

    private CouponResponse toResponse(Coupon c) {
        return new CouponResponse(c.getId(), c.getCode(), c.getDescription(),
                c.getDiscountType(), c.getDiscountValue(), c.getMinimumOrderAmount(),
                c.getMaximumDiscountAmount(), c.getValidFrom(), c.getValidUntil(),
                c.getMaxUsageCount(), c.getUsageCount(), c.isActive());
    }
}
