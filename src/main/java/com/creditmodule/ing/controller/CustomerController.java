package com.creditmodule.ing.controller;

import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.repository.CustomerRepository;
import com.creditmodule.ing.service.UserCustomerService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/customers")
@AllArgsConstructor
public class CustomerController {
    private UserCustomerService userCustomerService;


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public Customer getCustomer(@PathVariable Long id) {
        return userCustomerService.findCustomerById(id);
    }
    @GetMapping("/getid/{accountNumber}")
    @PreAuthorize("hasRole('ADMIN') or #request.accountNumber == authentication.principal.username")
    public Long getCustomerIdWithAccountNumber(@PathVariable String accountNumber) {
        return userCustomerService.findCustomerIdByAccountNumber(accountNumber);
    }

    @GetMapping("/{accountNumber}")
    @PreAuthorize("hasRole('ADMIN') or #request.accountNumber == authentication.principal.username")
    public Customer getCustomerWithAccountNumber(@PathVariable String accountNumber) {
        return userCustomerService.findCustomerWithAccountNumber(accountNumber);
    }
}
