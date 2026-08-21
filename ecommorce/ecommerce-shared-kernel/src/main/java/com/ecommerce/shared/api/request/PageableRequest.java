package com.ecommerce.shared.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Common pagination and sorting parameters extracted from query strings.
 * Bounded contexts may extend this with context-specific filter fields.
 */
public record PageableRequest(
        @Min(0) int page,
        @Min(1) @Max(100) int size,
        String sortBy,
        String sortDir
) {
    public PageableRequest {
        if (page < 0) page = 0;
        if (size < 1) size = 20;
        if (size > 100) size = 100;
        if (sortDir == null) sortDir = "asc";
    }

    public static PageableRequest defaults() {
        return new PageableRequest(0, 20, "createdAt", "desc");
    }

    public Pageable toPageable() {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        String field = (sortBy != null && !sortBy.isBlank()) ? sortBy : "createdAt";
        return PageRequest.of(page, size, Sort.by(direction, field));
    }
}
