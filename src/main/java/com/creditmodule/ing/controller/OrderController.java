package com.creditmodule.ing.controller;

import com.creditmodule.ing.data.CreateOrderRequest;
import com.creditmodule.ing.data.CreateOrderResponse;
import com.creditmodule.ing.data.DeleteOrderResponse;
import com.creditmodule.ing.data.ListOrdersResponse;
import com.creditmodule.ing.entity.Asset;
import com.creditmodule.ing.service.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;


    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or #request.customerId == authentication.principal.id")
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody @Valid CreateOrderRequest request) {
        CreateOrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/list/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ListOrdersResponse> listOrders(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") Date endDate) {

        ListOrdersResponse response = orderService.listOrders(id, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{orderId}/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<DeleteOrderResponse> deleteOrder(@PathVariable Long orderId, @PathVariable Long id) {
        DeleteOrderResponse response = orderService.deleteOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assets/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<List<Asset>> listAssets(@PathVariable Long id) {
        List<Asset> assets = orderService.listAssets(id);
        return ResponseEntity.ok(assets);
    }

    @PostMapping("/match")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> matchOrders() {
        orderService.matchPendingOrders();
        return ResponseEntity.ok("Pending orders matched successfully");
    }
}