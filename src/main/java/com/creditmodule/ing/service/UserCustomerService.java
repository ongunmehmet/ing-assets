package com.creditmodule.ing.service;

import com.creditmodule.ing.data.CustomerDetailDto;
import com.creditmodule.ing.data.UserCustomerCreateRequest;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.User;
import com.creditmodule.ing.enums.Role;
import com.creditmodule.ing.exceptions.ResourceNotFoundException;
import com.creditmodule.ing.mapper.ApiMapper;
import com.creditmodule.ing.repository.CustomerRepository;
import com.creditmodule.ing.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
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
        if (customerRepository.findAll().isEmpty()) {
            user.setRoles(Set.of(Role.ADMIN));
        } else {
            user.setRoles(Set.of(Role.CUSTOMER));
        }
        user.setAccountNumber(UUID.randomUUID().toString());

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setSurname(request.getSurname());
        customer.setCredit(request.getCreditLimit());
        customer.setUsedCredit(BigDecimal.valueOf(0L));

        customer.setId(user.getId());
        customer.setUser(user);
        customerRepository.save(customer);

        user.setCustomer(customer);
        userRepository.save(user);
        return user.getAccountNumber();
    }

    @Transactional(readOnly = true)
    public CustomerDetailDto findCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        // Map to one-level DTO (includes assets + orders)
        return ApiMapper.toCustomerDetail(customer);
    }

    @Transactional(readOnly = true)
    public Long findCustomerIdByAccountNumber(String accountNumber) {
        Customer customer = customerRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return customer.getId();
    }

    @Transactional(readOnly = true)
    public CustomerDetailDto findCustomerWithAccountNumber(String accountNumber) {
        Customer customer = customerRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return ApiMapper.toCustomerDetail(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerDetailDto> listAll() {
        return customerRepository.findAll().stream()
                .map(ApiMapper::toCustomerDetail)
                .toList();
    }
}


