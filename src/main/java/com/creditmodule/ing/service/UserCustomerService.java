package com.creditmodule.ing.service;

import com.creditmodule.ing.data.UserCustomerCreateRequest;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.User;
import com.creditmodule.ing.enums.Role;
import com.creditmodule.ing.repository.CustomerRepository;
import com.creditmodule.ing.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Service
public class UserCustomerService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;


    public String createUserAndCustomer(UserCustomerCreateRequest request) {
        // Create User
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(Role.CUSTOMER)); // Default role: CUSTOMER
        user.setAccountNumber(UUID.randomUUID().toString());

        // Create Customer
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setSurname(request.getSurname());
        customer.setCreditLimit(request.getCreditLimit());
        customer.setUsedCreditLimit(0L);

        // Manually set the shared ID
        customer.setId(user.getId());  // This ensures Customer has the same ID as User

        customer.setUser(user);  // Set the bi-directional relationship

        // Save Customer first (since User's ID will be set after persisting)
        customerRepository.save(customer);

        // Save User with the customer relationship
        user.setCustomer(customer);
        userRepository.save(user);
            return  user.getAccountNumber();
    }

    public Customer findCustomerById(Long id) {
       return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public Long findCustomerIdByAccountNumber(String accountNumber) {
        Customer customer = customerRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return customer.getId();
    }

    public Customer findCustomerWithAccountNumber(String accountNumber) {
        return  customerRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }
}
