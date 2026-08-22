package com.ecommerce.order.application.listener;

import com.ecommerce.order.application.service.OrderService;
import com.ecommerce.order.domain.model.OrderStatus;
import com.ecommerce.shared.domain.event.payment.PaymentCompletedEvent;
import com.ecommerce.shared.domain.event.payment.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listens to payment domain events and drives the Order state machine accordingly.
 *
 * <p>Uses {@code @TransactionalEventListener(phase = AFTER_COMMIT)} so that order
 * state changes are only triggered after the payment transaction has fully
 * committed — preventing phantom confirms for rolled-back payments.
 *
 * <p>Both operations are {@code @Async} so a slow order update never blocks the
 * payment response returned to the customer.
 */
@Slf4j
@Component("orderPaymentEventListener")
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderService orderService;

    /**
     * Payment succeeded → transition order PAYMENT_PENDING → CONFIRMED.
     *
     * <p>The order is moved to PAYMENT_PENDING by the payment initiation flow
     * before the provider is called. Once the provider confirms success and the
     * {@link PaymentCompletedEvent} is published, we confirm the order here.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Payment completed — confirming order. orderId={}, paymentId={}",
                event.getOrderId(), event.getPaymentId());
        try {
            orderService.confirmOrderAfterPayment(event.getOrderId());
        } catch (Exception ex) {
            // Log and continue — a compensating job / manual review handles stuck orders.
            log.error("Failed to confirm order after payment. orderId={}, reason={}",
                    event.getOrderId(), ex.getMessage(), ex);
        }
    }

    /**
     * Payment failed → transition order to PAYMENT_FAILED so the customer can retry.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.warn("Payment failed — updating order status. orderId={}, reason={}",
                event.getOrderId(), event.getReason());
        try {
            orderService.updateStatus(event.getOrderId(), OrderStatus.PAYMENT_FAILED, null);
        } catch (Exception ex) {
            log.error("Failed to mark order as PAYMENT_FAILED. orderId={}, reason={}",
                    event.getOrderId(), ex.getMessage(), ex);
        }
    }
}
