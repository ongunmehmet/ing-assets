package com.creditmodule.ing.service;

import com.creditmodule.ing.data.UserCustomerCreateRequest;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.User;
import com.creditmodule.ing.enums.Role;
import com.creditmodule.ing.exceptions.ResourceNotFoundException;
import com.creditmodule.ing.repository.CustomerRepository;
import com.creditmodule.ing.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Service
public class UserCustomerService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String createUserAndCustomer(UserCustomerCreateRequest request) {
        User user = new User();
        user.setUsername(request.getSurname());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        if (customerRepository.findAll().size() == 0) {
            user.setRoles(Set.of(Role.ADMIN));
        } else {
            user.setRoles(Set.of(Role.CUSTOMER));
        }
        user.setAccountNumber(UUID.randomUUID().toString());

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setSurname(request.getSurname());
        customer.setCreditLimit(request.getCreditLimit());
        customer.setUsedCreditLimit(0L);

        customer.setId(user.getId());
        customer.setUser(user);
        customerRepository.save(customer);

        user.setCustomer(customer);
        userRepository.save(user);
        return user.getAccountNumber();
    }

    public Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    public Long findCustomerIdByAccountNumber(String accountNumber) {
        Customer customer = customerRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return customer.getId();
    }

    public Customer findCustomerWithAccountNumber(String accountNumber) {
        return customerRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }
}


