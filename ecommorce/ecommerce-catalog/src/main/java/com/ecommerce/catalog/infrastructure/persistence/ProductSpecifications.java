package com.ecommerce.catalog.infrastructure.persistence;

import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.domain.repository.ProductSearchCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for dynamic product filtering.
 * Keeps query-building logic out of the repository and service layers.
 */
public final class ProductSpecifications {

    private ProductSpecifications() {}

    public static Specification<Product> from(ProductSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.status() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.status()));
            }

            if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
                String pattern = "%" + criteria.keyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("brand")), pattern),
                        cb.like(cb.lower(root.get("sku")), pattern)
                ));
            }

            if (criteria.categoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), criteria.categoryId()));
            }

            if (criteria.brand() != null && !criteria.brand().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("brand")),
                        criteria.brand().toLowerCase()));
            }

            if (criteria.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("basePrice"), criteria.minPrice()));
            }

            if (criteria.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("basePrice"), criteria.maxPrice()));
            }

            // Avoid N+1 on list queries — fetch images with single join
            if (query != null) {
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
