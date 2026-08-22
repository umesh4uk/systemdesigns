package com.ecommerce.notification.application.listener;

import com.ecommerce.shared.domain.event.payment.PaymentCompletedEvent;
import com.ecommerce.shared.domain.event.payment.PaymentFailedEvent;
import com.ecommerce.notification.application.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component("notificationPaymentEventListener")
@RequiredArgsConstructor
public class PaymentEventListener {

    private final EmailNotificationService emailService;

    @Async
    @EventListener
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Notification: payment completed orderId={}", event.getOrderId());
        emailService.sendPaymentConfirmation(event.getCustomerId(), event.getOrderId(),
                event.getAmount().getAmount(), event.getAmount().getCurrencyCode());
    }

    @Async
    @EventListener
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("Notification: payment failed orderId={}", event.getOrderId());
        emailService.sendPaymentFailureNotification(event.getCustomerId(),
                event.getOrderId(), event.getReason());
    }
}
