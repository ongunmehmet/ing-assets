package com.creditmodule.ing.service;

import com.creditmodule.ing.data.CreateLoanRequest;
import com.creditmodule.ing.data.CreateLoanResponse;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.Loan;
import com.creditmodule.ing.entity.User;
import com.creditmodule.ing.exceptions.CreditLimitExceedException;
import com.creditmodule.ing.repository.CustomerRepository;
import com.creditmodule.ing.repository.LoanInstallmentRepository;
import com.creditmodule.ing.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StdLoanServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;

    @InjectMocks
    private StdLoanServiceImp stdLoanServiceImp;

    private CreateLoanRequest request;
    private Customer customer;
    private User user;

    @BeforeEach
    void setUp() {
        user = TestUtils.createMockUser();
        customer = TestUtils.createMockCustomer();
        request = TestUtils.createMockLoanRequest();
    }

    @Test
    void testCreateLoan_Successful() {

        when(customerRepository.findByAccountNumber(request.getAccountNumber())).thenReturn(Optional.of(customer));
        when(loanRepository.save(any(Loan.class))).thenReturn(new Loan());
        when(loanInstallmentRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // Act:
        CreateLoanResponse response = stdLoanServiceImp.createLoan(request);

        // Assert:
        assertNotNull(response);
        assertEquals(request.getLoanAmount(), response.getLoanAmount());
        assertEquals(request.getNumberOfInstallment(), response.getNumberOfInstallment());
        verify(customerRepository).save(customer);
        verify(loanRepository).save(any(Loan.class));
        verify(loanInstallmentRepository).saveAll(anyList());
    }

    @Test
    void testCreateLoan_CreditLimitExceeded() {
        // Arrange
        request.setLoanAmount(12000L);
        when(customerRepository.findByAccountNumber(request.getAccountNumber())).thenReturn(Optional.of(customer));

        // Act & Assert
        CreditLimitExceedException exception = assertThrows(CreditLimitExceedException.class, () -> {
            stdLoanServiceImp.createLoan(request);
        });
        assertEquals("Credit Limit Exceed", exception.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void testCreateLoan_CustomerNotFound() {
        // Arrange
        when(customerRepository.findByAccountNumber(request.getAccountNumber())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            stdLoanServiceImp.createLoan(request);
        });
        verify(customerRepository, never()).save(any(Customer.class));
    }
}
