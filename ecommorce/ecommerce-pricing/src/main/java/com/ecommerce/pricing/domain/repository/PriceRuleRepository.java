package com.ecommerce.pricing.domain.repository;

import com.ecommerce.pricing.domain.model.PriceRule;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PriceRuleRepository {
    PriceRule save(PriceRule rule);
    Optional<PriceRule> findById(UUID id);
    /** Find all active rules for a product valid at the given instant. */
    List<PriceRule> findActiveRulesForProduct(UUID productId, Instant at);
    List<PriceRule> findByProductId(UUID productId);
}
