package com.ecommerce.catalog.application.mapper;

import com.ecommerce.catalog.application.dto.ProductDetailResponse;
import com.ecommerce.catalog.application.dto.ProductSummaryResponse;
import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.domain.model.ProductAttribute;
import com.ecommerce.catalog.domain.model.ProductImage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public ProductSummaryResponse toSummary(Product p) {
        String primaryUrl = p.getImages().stream()
                .filter(ProductImage::isPrimary)
                .map(ProductImage::getUrl)
                .findFirst()
                .orElse(p.getImages().isEmpty() ? null : p.getImages().get(0).getUrl());

        return new ProductSummaryResponse(
                p.getId(), p.getSku(), p.getName(), p.getShortDescription(),
                p.getBrand(), p.getBasePrice().getAmount(), p.getCurrency(),
                p.getStatus(), primaryUrl,
                p.getCategory() != null ? p.getCategory().getName() : null);
    }

    public ProductDetailResponse toDetail(Product p) {
        List<ProductDetailResponse.ImageResponse> images = p.getImages().stream()
                .map(i -> new ProductDetailResponse.ImageResponse(
                        i.getId(), i.getUrl(), i.getAltText(), i.getDisplayOrder(), i.isPrimary()))
                .toList();

        Map<String, String> attrs = p.getAttributes().stream()
                .collect(Collectors.toMap(ProductAttribute::getKey, ProductAttribute::getValue));

        return new ProductDetailResponse(
                p.getId(), p.getSku(), p.getName(), p.getDescription(),
                p.getShortDescription(), p.getBrand(),
                p.getBasePrice().getAmount(), p.getCurrency(), p.getWeightGrams(),
                p.getStatus(),
                p.getCategory() != null ? p.getCategory().getId() : null,
                p.getCategory() != null ? p.getCategory().getName() : null,
                images, attrs, p.getCreatedAt(), p.getUpdatedAt());
    }
}
