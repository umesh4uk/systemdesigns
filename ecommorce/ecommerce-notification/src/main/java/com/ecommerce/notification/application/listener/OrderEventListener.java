package com.ecommerce.notification.application.listener;

import com.ecommerce.order.domain.event.*;
import com.ecommerce.notification.application.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens to order domain events and dispatches email notifications.
 * Decoupled from the Order bounded context — only depends on published events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final EmailNotificationService emailService;

    @Async
    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Notification: order placed={}", event.getOrderNumber());
        emailService.sendOrderConfirmation(event.getCustomerId(), event.getOrderNumber(),
                event.getTotalAmount(), event.getCurrency());
    }

    @Async
    @EventListener
    public void onOrderShipped(OrderShippedEvent event) {
        log.info("Notification: order shipped={}, tracking={}", event.getOrderNumber(),
                event.getTrackingNumber());
        emailService.sendShippingNotification(event.getCustomerId(), event.getOrderNumber(),
                event.getTrackingNumber());
    }

    @Async
    @EventListener
    public void onOrderDelivered(OrderDeliveredEvent event) {
        log.info("Notification: order delivered={}", event.getOrderNumber());
        emailService.sendDeliveryNotification(event.getCustomerId(), event.getOrderNumber());
    }

    @Async
    @EventListener
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("Notification: order cancelled={}", event.getOrderNumber());
        emailService.sendCancellationNotification(event.getCustomerId(), event.getOrderNumber(),
                event.getReason());
    }
}
