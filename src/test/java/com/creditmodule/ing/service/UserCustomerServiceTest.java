package com.creditmodule.ing.service;


import com.creditmodule.ing.data.UserCustomerCreateRequest;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.User;
import com.creditmodule.ing.exceptions.ResourceNotFoundException;
import com.creditmodule.ing.repository.CustomerRepository;
import com.creditmodule.ing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserCustomerServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserCustomerService userCustomerService;

    private UserCustomerCreateRequest validRequest;
    private UserCustomerCreateRequest invalidRequest;
    private Customer mockCustomer;
    private String mockAccountNumber;

    @BeforeEach
    void setUp() {
        validRequest = TestUtils.createMockUserCustomerCreateRequest();
        mockCustomer = TestUtils.createMockCustomer();
        mockAccountNumber = mockCustomer.getUser().getAccountNumber();
    }

    @Test
    void testCreateUserAndCustomer_Success() {
        // Arrange:
        when(passwordEncoder.encode(validRequest.getPassword())).thenReturn("encodedPassword");

        // Arrange:
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setAccountNumber(UUID.randomUUID().toString());

        Customer mockCustomer = new Customer();
        mockCustomer.setId(mockUser.getId());
        mockCustomer.setUser(mockUser);

        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);

        // Act:
        String accountNumber = userCustomerService.createUserAndCustomer(validRequest);

        // Assert:
        assertNotNull(accountNumber);
        assertTrue(accountNumber.length() > 0);
        verify(userRepository).save(any(User.class));
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void testFindCustomerById_Success() {
        // Arrange:
        when(customerRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));

        // Act:
        Customer customer = userCustomerService.findCustomerById(1L);

        // Assert:
        assertNotNull(customer);
        assertEquals(mockCustomer.getId(), customer.getId());
        verify(customerRepository).findById(1L);
    }

    @Test
    void testFindCustomerById_CustomerNotFound() {
        // Arrange:
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert:
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userCustomerService.findCustomerById(1L);
        });

        assertEquals("Customer not found", exception.getMessage());
        verify(customerRepository).findById(1L);
    }

    @Test
    void testFindCustomerIdByAccountNumber_Success() {
        // Arrange:
        when(customerRepository.findByAccountNumber(mockAccountNumber)).thenReturn(Optional.of(mockCustomer));

        // Act:
        Long customerId = userCustomerService.findCustomerIdByAccountNumber(mockAccountNumber);

        // Assert:
        assertNotNull(customerId);
        assertEquals(mockCustomer.getId(), customerId);
        verify(customerRepository).findByAccountNumber(mockAccountNumber);
    }

    @Test
    void testFindCustomerIdByAccountNumber_CustomerNotFound() {
        // Arrange:
        when(customerRepository.findByAccountNumber(mockAccountNumber)).thenReturn(Optional.empty());

        // Act & Assert:
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userCustomerService.findCustomerIdByAccountNumber(mockAccountNumber);
        });

        assertEquals("Customer not found", exception.getMessage());
        verify(customerRepository).findByAccountNumber(mockAccountNumber);
    }
}

