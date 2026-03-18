package com.davidarbana.warehouse.scheduled;

import com.davidarbana.warehouse.enums.OrderStatus;
import com.davidarbana.warehouse.repository.DeliveryRepository;
import com.davidarbana.warehouse.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DeliveryFulfillmentScheduler {

    private static final Logger log = LoggerFactory.getLogger(DeliveryFulfillmentScheduler.class);

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;

    // runs every day at midnight, marks UNDER_DELIVERY orders as FULFILLED
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void checkAndFulfillDeliveries() {
        var due = deliveryRepository.findDeliveriesDueByDate(LocalDate.now());

        if (due.isEmpty()) return;

        due.forEach(d -> {
            d.getOrder().setStatus(OrderStatus.FULFILLED);
            orderRepository.save(d.getOrder());
        });

        log.info("Fulfilled {} orders", due.size());
    }
}
