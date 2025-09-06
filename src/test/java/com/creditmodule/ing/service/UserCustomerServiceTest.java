package com.creditmodule.ing.service;

import com.creditmodule.ing.data.CustomerDetailDto;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

        when(passwordEncoder.encode(validRequest.getPassword())).thenReturn("encodedPassword");

        when(customerRepository.findAll()).thenReturn(Collections.emptyList());

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setAccountNumber(UUID.randomUUID().toString());

        Customer savedCustomer = new Customer();
        savedCustomer.setId(savedUser.getId());
        savedCustomer.setUser(savedUser);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        String accountNumber = userCustomerService.createUserAndCustomer(validRequest);

        assertNotNull(accountNumber);
        assertFalse(accountNumber.isBlank());
        verify(customerRepository).findAll();
        verify(userRepository).save(any(User.class));
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void testFindCustomerById_Success() {

        when(customerRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));

        CustomerDetailDto dto = userCustomerService.findCustomerById(1L);

        assertNotNull(dto);
        assertEquals(mockCustomer.getId(), dto.id());
        assertEquals(mockCustomer.getName(), dto.name());
        assertEquals(mockCustomer.getSurname(), dto.surname());
        assertEquals(mockCustomer.getCredit(), dto.credit());

        assertNotNull(dto.assets());
        assertNotNull(dto.orders());
        verify(customerRepository).findById(1L);
    }

    @Test
    void testFindCustomerById_CustomerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(ResourceNotFoundException.class, () -> userCustomerService.findCustomerById(1L));

        assertEquals("Customer not found", ex.getMessage());
        verify(customerRepository).findById(1L);
    }

    @Test
    void testFindCustomerIdByAccountNumber_Success() {
        when(customerRepository.findByAccountNumber(mockAccountNumber)).thenReturn(Optional.of(mockCustomer));

        Long customerId = userCustomerService.findCustomerIdByAccountNumber(mockAccountNumber);

        assertNotNull(customerId);
        assertEquals(mockCustomer.getId(), customerId);
        verify(customerRepository).findByAccountNumber(mockAccountNumber);
    }

    @Test
    void testFindCustomerIdByAccountNumber_CustomerNotFound() {
        when(customerRepository.findByAccountNumber(mockAccountNumber)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(ResourceNotFoundException.class,
                        () -> userCustomerService.findCustomerIdByAccountNumber(mockAccountNumber));

        assertEquals("Customer not found", ex.getMessage());
        verify(customerRepository).findByAccountNumber(mockAccountNumber);
    }

    @Test
    void testFindCustomerWithAccountNumber_Success() {
        when(customerRepository.findByAccountNumber(mockAccountNumber)).thenReturn(Optional.of(mockCustomer));

        CustomerDetailDto dto = userCustomerService.findCustomerWithAccountNumber(mockAccountNumber);

        assertNotNull(dto);
        assertEquals(mockCustomer.getId(), dto.id());
        assertEquals(mockCustomer.getName(), dto.name());
        assertEquals(mockCustomer.getSurname(), dto.surname());
        assertEquals(mockCustomer.getCredit(), dto.credit());
        verify(customerRepository).findByAccountNumber(mockAccountNumber);
    }

    @Test
    void testFindCustomerWithAccountNumber_NotFound() {
        when(customerRepository.findByAccountNumber(mockAccountNumber)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(ResourceNotFoundException.class,
                        () -> userCustomerService.findCustomerWithAccountNumber(mockAccountNumber));

        assertEquals("Customer not found", ex.getMessage());
        verify(customerRepository).findByAccountNumber(mockAccountNumber);
    }

    @Test
    void testListAllCustomers_ReturnsMappedDtos() {
        Customer c1 = TestUtils.customer("Alice", "One");
        c1.setCustomerAssets(new ArrayList<>());
        c1.setOrders(new ArrayList<>());

        Customer c2 = TestUtils.customer("Bob", "Two");
        c2.setCustomerAssets(new ArrayList<>());
        c2.setOrders(new ArrayList<>());

        when(customerRepository.findAll()).thenReturn(List.of(c1, c2));

        List<CustomerDetailDto> result = userCustomerService.listAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        CustomerDetailDto d1 = result.getFirst();
        assertEquals(c1.getId(), d1.id());
        assertEquals("Alice", d1.name());
        assertEquals("One", d1.surname());
        assertNotNull(d1.assets());
        assertNotNull(d1.orders());

        CustomerDetailDto d2 = result.get(1);
        assertEquals(c2.getId(), d2.id());
        assertEquals("Bob", d2.name());
        assertEquals("Two", d2.surname());
        assertNotNull(d2.assets());
        assertNotNull(d2.orders());

        verify(customerRepository).findAll();
    }
}