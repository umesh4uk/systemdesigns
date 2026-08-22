package com.ecommerce.promotion.infrastructure.persistence;

import com.ecommerce.promotion.domain.model.Coupon;
import com.ecommerce.promotion.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryAdapter implements CouponRepository {
    private final JpaCouponRepository jpa;

    @Override public Coupon save(Coupon coupon)               { return jpa.save(coupon); }
    @Override public Optional<Coupon> findById(UUID id)       { return jpa.findById(id); }
    @Override public Optional<Coupon> findByCode(String code) { return jpa.findByCode(code.toUpperCase()); }
    @Override public boolean existsByCode(String code)        { return jpa.existsByCode(code.toUpperCase()); }
}
