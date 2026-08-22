package com.ecommerce.pricing.infrastructure.persistence;

import com.ecommerce.pricing.domain.model.PriceRule;
import com.ecommerce.pricing.domain.repository.PriceRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PriceRuleRepositoryAdapter implements PriceRuleRepository {
    private final JpaPriceRuleRepository jpa;

    @Override public PriceRule save(PriceRule rule)                                           { return jpa.save(rule); }
    @Override public Optional<PriceRule> findById(UUID id)                                    { return jpa.findById(id); }
    @Override public List<PriceRule> findActiveRulesForProduct(UUID productId, Instant at)    { return jpa.findActiveRules(productId, at); }
    @Override public List<PriceRule> findByProductId(UUID productId)                          { return jpa.findByProductId(productId); }
}
