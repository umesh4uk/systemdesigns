package com.ecommerce.catalog.application.service;

import com.ecommerce.catalog.application.dto.*;
import com.ecommerce.catalog.application.mapper.ProductMapper;
import com.ecommerce.catalog.domain.model.Category;
import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.domain.repository.CategoryRepository;
import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.catalog.domain.repository.ProductSearchCriteria;
import com.ecommerce.shared.api.response.PageResponse;
import com.ecommerce.shared.domain.valueobject.Money;
import com.ecommerce.shared.exception.ConflictException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ApplicationEventPublisher eventPublisher;

    // ------------------------------------------------------------------ read

    @Transactional(readOnly = true)
    public ProductDetailResponse getProduct(UUID id) {
        return productMapper.toDetail(loadProduct(id));
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProductBySku(String sku) {
        return productMapper.toDetail(
                productRepository.findBySku(sku)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "SKU:" + sku)));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> searchProducts(
            String keyword, UUID categoryId, String brand,
            BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {

        var criteria = ProductSearchCriteria.forCustomers(keyword, categoryId, brand, minPrice, maxPrice);
        return PageResponse.from(
                productRepository.findAll(criteria, pageable).map(productMapper::toSummary));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> adminSearchProducts(
            String keyword, UUID categoryId, String brand,
            com.ecommerce.catalog.domain.model.ProductStatus status, Pageable pageable) {

        var criteria = ProductSearchCriteria.forAdmin(keyword, categoryId, brand, status);
        return PageResponse.from(
                productRepository.findAll(criteria, pageable).map(productMapper::toSummary));
    }

    // ------------------------------------------------------------------ write

    @Transactional
    public ProductDetailResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new ConflictException("SKU already exists: " + request.sku());
        }

        Category category = resolveCategory(request.categoryId());
        Money price = Money.of(request.basePrice(), request.currency());

        Product product = Product.create(
                request.sku(), request.name(), request.description(),
                request.shortDescription(), category, request.brand(),
                price, request.weightGrams());

        Product saved = productRepository.save(product);
        publishEvents(saved);
        log.info("Product created: sku={}, id={}", saved.getSku(), saved.getId());
        return productMapper.toDetail(saved);
    }

    @Transactional
    public ProductDetailResponse updateProduct(UUID id, UpdateProductRequest request) {
        Product product = loadProduct(id);
        Category category = resolveCategory(request.categoryId());

        product.updateDetails(request.name(), request.description(), request.shortDescription(),
                category, request.brand(), request.weightGrams());

        if (request.basePrice() != null && request.currency() != null) {
            product.updatePrice(Money.of(request.basePrice(), request.currency()));
        }

        return productMapper.toDetail(productRepository.save(product));
    }

    @Transactional
    public ProductDetailResponse publishProduct(UUID id) {
        Product product = loadProduct(id);
        product.publish();
        Product saved = productRepository.save(product);
        publishEvents(saved);
        return productMapper.toDetail(saved);
    }

    @Transactional
    public ProductDetailResponse archiveProduct(UUID id) {
        Product product = loadProduct(id);
        product.archive();
        Product saved = productRepository.save(product);
        publishEvents(saved);
        return productMapper.toDetail(saved);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product product = loadProduct(id);
        product.delete();
        productRepository.delete(product);
        log.info("Product soft-deleted: id={}", id);
    }

    @Transactional
    public ProductDetailResponse addImage(UUID productId, String url, String altText, int displayOrder) {
        Product product = loadProduct(productId);
        product.addImage(url, altText, displayOrder);
        return productMapper.toDetail(productRepository.save(product));
    }

    @Transactional
    public ProductDetailResponse setAttribute(UUID productId, String key, String value) {
        Product product = loadProduct(productId);
        product.setAttribute(key, value);
        return productMapper.toDetail(productRepository.save(product));
    }

    // ------------------------------------------------------------------ helpers

    private Product loadProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private Category resolveCategory(UUID categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
    }

    private void publishEvents(Product product) {
        product.getDomainEvents().forEach(eventPublisher::publishEvent);
        product.clearDomainEvents();
    }
}
