package com.creditmodule.ing.service;

import com.creditmodule.ing.data.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderCreatedListener {
    private final OrderQueue orderQueue;
    private final MatchingSwitch matchingSwitch;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent evt) {
        if (matchingSwitch.isRunning()) {
            orderQueue.addOrder(evt.orderId());
        }
    }
}