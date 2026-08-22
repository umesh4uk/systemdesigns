package com.ecommerce.pricing.infrastructure.persistence;

import com.ecommerce.pricing.domain.model.PriceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface JpaPriceRuleRepository extends JpaRepository<PriceRule, UUID> {

    @Query("""
        SELECT r FROM PriceRule r
        WHERE r.productId = :productId
          AND r.active = true
          AND r.validFrom <= :at
          AND (r.validUntil IS NULL OR r.validUntil >= :at)
        ORDER BY r.discountValue DESC
        """)
    List<PriceRule> findActiveRules(@Param("productId") UUID productId, @Param("at") Instant at);

    List<PriceRule> findByProductId(UUID productId);
}
