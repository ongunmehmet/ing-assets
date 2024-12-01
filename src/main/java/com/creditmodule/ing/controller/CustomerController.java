package com.creditmodule.ing.controller;

import com.creditmodule.ing.entity.Customer;

import com.creditmodule.ing.service.UserCustomerService;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/customers")
@AllArgsConstructor
@Tag(name = "Customer", description = "Endpoints for customer operations")
public class CustomerController {
    private UserCustomerService userCustomerService;

    @Operation(summary = "Get Customer by ID", description = "Retrieve a customer's details by their ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer found"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
        Customer customer = userCustomerService.findCustomerById(id);
        return ResponseEntity.ok(customer);

    }
    @Operation(summary = "Get Customer ID by Account Number", description = "Retrieve a customer's ID using their account number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer ID retrieved"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/getid/{accountNumber}")
    @PreAuthorize("hasRole('ADMIN') or #request.accountNumber == authentication.principal.username")
    public ResponseEntity<Long> getCustomerIdWithAccountNumber(@PathVariable String accountNumber) {
        Long customerId = userCustomerService.findCustomerIdByAccountNumber(accountNumber);
        return ResponseEntity.ok(customerId);
    }
    @Operation(summary = "Get Customer by Account Number", description = "Retrieve a customer's details using their account number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer found"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{accountNumber}")
    @PreAuthorize("hasRole('ADMIN') or #request.accountNumber == authentication.principal.username")
    public ResponseEntity<Customer> getCustomerWithAccountNumber(@PathVariable String accountNumber) {
        Customer customer = userCustomerService.findCustomerWithAccountNumber(accountNumber);
        return ResponseEntity.ok(customer);
    }
}
