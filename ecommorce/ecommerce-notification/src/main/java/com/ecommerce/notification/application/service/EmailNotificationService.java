package com.ecommerce.notification.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Email notification service. Uses JavaMailSender — swap template with Thymeleaf
 * HTML emails in production.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final NotificationProperties properties;

    public void sendWelcomeEmail(UUID customerId, String email) {
        sendSimple(email, "Welcome to Our Store!",
                "Thank you for registering. Your account is now active.");
    }

    public void sendOrderConfirmation(UUID customerId, String orderNumber,
                                       BigDecimal total, String currency) {
        sendSimple(resolveEmail(customerId),
                "Order Confirmed: " + orderNumber,
                "Your order " + orderNumber + " has been placed for " + total + " " + currency + ".");
    }

    public void sendShippingNotification(UUID customerId, String orderNumber,
                                          String trackingNumber) {
        sendSimple(resolveEmail(customerId),
                "Your order has shipped: " + orderNumber,
                "Tracking number: " + trackingNumber);
    }

    public void sendDeliveryNotification(UUID customerId, String orderNumber) {
        sendSimple(resolveEmail(customerId),
                "Order Delivered: " + orderNumber,
                "Your order " + orderNumber + " has been delivered.");
    }

    public void sendCancellationNotification(UUID customerId, String orderNumber, String reason) {
        sendSimple(resolveEmail(customerId),
                "Order Cancelled: " + orderNumber,
                "Your order has been cancelled. Reason: " + reason);
    }

    public void sendPaymentConfirmation(UUID customerId, UUID orderId,
                                         BigDecimal amount, String currency) {
        sendSimple(resolveEmail(customerId),
                "Payment Received",
                "Payment of " + amount + " " + currency + " for order " + orderId + " confirmed.");
    }

    public void sendPaymentFailureNotification(UUID customerId, UUID orderId, String reason) {
        sendSimple(resolveEmail(customerId),
                "Payment Failed",
                "Payment for order " + orderId + " failed. Reason: " + reason);
    }

    private void sendSimple(String to, String subject, String body) {
        if (to == null) {
            log.warn("Cannot send email — recipient address is null");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(properties.getFromAddress());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.debug("Email sent to={} subject={}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to={}: {}", to, e.getMessage());
        }
    }

    /**
     * In a real system this would look up the customer email from an in-process
     * cache or a lightweight read model populated from CustomerRegisteredEvent.
     * Simplified here to log only.
     */
    private String resolveEmail(UUID customerId) {
        log.debug("Email lookup for customerId={} — implement customer email cache", customerId);
        return null; // no-op in this scaffold
    }
}
