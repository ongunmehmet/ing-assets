package com.creditmodule.ing.controller;

import com.creditmodule.ing.enums.Status;
import com.creditmodule.ing.repository.OrderRepository;
import com.creditmodule.ing.service.MatchingSwitch;
import com.creditmodule.ing.service.OrderMatchingService;
import com.creditmodule.ing.service.OrderQueue;
import com.creditmodule.ing.service.OrderWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class OrderMatchingController {

    private final OrderWorker orderWorker;
    private final OrderQueue orderQueue;
    private final OrderRepository orderRepository;
    private final OrderMatchingService orderMatchingService;
    private final MatchingSwitch matchingSwitch;

    @PostMapping("/match/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> matchSingle(@PathVariable Long id) {
        try {
            orderMatchingService.processSingleOrder(id); // direct service call
            return ResponseEntity.ok("Order " + id + " matched successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Order " + id + " failed: " + e.getMessage());
        }
    }


    @PostMapping("/start-matching")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> startMatching() {
        orderRepository.findByStatus(Status.PENDING)
                .forEach(o -> orderQueue.addOrder(o.getId()));

        matchingSwitch.turnOn();
        orderWorker.start();
        return ResponseEntity.ok("Parallel matching started");
    }

    @PostMapping("/stop-matching")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> stopMatching() {
        matchingSwitch.turnOff();
        orderWorker.stop();
        return ResponseEntity.ok("Parallel matching stopped");
    }
}
