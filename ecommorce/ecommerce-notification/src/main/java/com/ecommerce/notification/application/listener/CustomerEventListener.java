package com.ecommerce.notification.application.listener;

import com.ecommerce.identity.domain.event.CustomerRegisteredEvent;
import com.ecommerce.notification.application.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerEventListener {

    private final EmailNotificationService emailService;

    @Async
    @EventListener
    public void onCustomerRegistered(CustomerRegisteredEvent event) {
        log.info("Notification: customer registered={}", event.getEmail());
        emailService.sendWelcomeEmail(event.getCustomerId(), event.getEmail());
    }
}
