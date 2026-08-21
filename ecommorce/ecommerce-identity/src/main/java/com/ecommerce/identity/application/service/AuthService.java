package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.dto.*;
import com.ecommerce.identity.domain.model.Customer;
import com.ecommerce.identity.domain.repository.CustomerRepository;
import com.ecommerce.identity.infrastructure.security.JwtProperties;
import com.ecommerce.identity.infrastructure.security.JwtTokenProvider;
import com.ecommerce.shared.domain.event.DomainEvent;
import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Authentication use-case service.
 * Owns: register, login, refresh-token, logout.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CustomerResponse register(RegisterRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered: " + request.email());
        }

        String hash = passwordEncoder.encode(request.password());
        Customer customer = Customer.register(
                request.email(), hash,
                request.firstName(), request.lastName(), request.phone());

        Customer saved = customerRepository.save(customer);
        publishEvents(saved);

        log.info("Customer registered: id={}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Customer customer = customerRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), customer.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!customer.isActive()) {
            throw new BusinessRuleException("ACCOUNT_NOT_ACTIVE",
                    "Account is not active. Status: " + customer.getStatus());
        }

        String accessToken  = tokenProvider.generateAccessToken(
                customer.getId(), customer.getEmail(), List.of("ROLE_CUSTOMER"));
        String refreshToken = tokenProvider.generateRefreshToken(customer.getId());

        return TokenResponse.of(accessToken, refreshToken, jwtProperties.getAccessTokenExpirySeconds());
    }

    @Transactional(readOnly = true)
    public TokenResponse refresh(String refreshToken) {
        if (!tokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessRuleException("INVALID_REFRESH_TOKEN", "Invalid or expired refresh token");
        }

        UUID customerId = tokenProvider.extractCustomerId(refreshToken);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found: " + customerId));

        if (!customer.isActive()) {
            throw new BusinessRuleException("ACCOUNT_NOT_ACTIVE", "Account is not active");
        }

        String newAccess  = tokenProvider.generateAccessToken(
                customer.getId(), customer.getEmail(), List.of("ROLE_CUSTOMER"));
        String newRefresh = tokenProvider.generateRefreshToken(customer.getId());

        return TokenResponse.of(newAccess, newRefresh, jwtProperties.getAccessTokenExpirySeconds());
    }

    private CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(c.getId(), c.getEmail(), c.getFirstName(),
                c.getLastName(), c.getPhone(), c.getStatus(), c.getCreatedAt());
    }

    private void publishEvents(Customer customer) {
        customer.getDomainEvents().forEach(eventPublisher::publishEvent);
        customer.clearDomainEvents();
    }
}
