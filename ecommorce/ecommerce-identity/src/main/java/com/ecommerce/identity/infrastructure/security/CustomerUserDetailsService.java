package com.ecommerce.identity.infrastructure.security;

import com.ecommerce.identity.domain.model.Customer;
import com.ecommerce.identity.domain.model.CustomerStatus;
import com.ecommerce.identity.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found: " + email));

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_CUSTOMER")
        );

        return User.builder()
                .username(customer.getId().toString())
                .password(customer.getPasswordHash())
                .authorities(authorities)
                .disabled(customer.getStatus() == CustomerStatus.DEACTIVATED)
                .accountLocked(customer.getStatus() == CustomerStatus.SUSPENDED)
                .build();
    }
}
