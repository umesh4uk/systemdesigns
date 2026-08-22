package com.ecommerce.cart.infrastructure.redis;

import com.ecommerce.cart.domain.model.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Cart persistence via Redis with 7-day TTL.
 * Key format: cart:{customerId}
 */
@Repository
@RequiredArgsConstructor
public class CartRedisRepository {

    private static final String KEY_PREFIX = "cart:";
    private static final Duration TTL = Duration.ofDays(7);

    private final RedisTemplate<String, Cart> redisTemplate;

    public void save(Cart cart) {
        String key = key(cart.getCustomerId());
        redisTemplate.opsForValue().set(key, cart, TTL);
    }

    public Optional<Cart> findByCustomerId(UUID customerId) {
        Cart cart = redisTemplate.opsForValue().get(key(customerId));
        return Optional.ofNullable(cart);
    }

    public void delete(UUID customerId) {
        redisTemplate.delete(key(customerId));
    }

    public void refreshTtl(UUID customerId) {
        redisTemplate.expire(key(customerId), TTL);
    }

    private String key(UUID customerId) {
        return KEY_PREFIX + customerId;
    }
}
