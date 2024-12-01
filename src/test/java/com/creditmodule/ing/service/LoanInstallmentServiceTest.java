package com.creditmodule.ing.service;

import com.creditmodule.ing.data.PaymentRequest;
import com.creditmodule.ing.data.PaymentResponse;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.Loan;
import com.creditmodule.ing.entity.LoanInstallment;
import com.creditmodule.ing.exceptions.ResourceNotFoundException;
import com.creditmodule.ing.repository.CustomerRepository;
import com.creditmodule.ing.repository.LoanInstallmentRepository;
import com.creditmodule.ing.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoanInstallmentServiceTest {

    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private LoanInstallmentService loanInstallmentService;

    private PaymentRequest paymentRequest;
    private Customer customer;
    private Loan loan;
    private LoanInstallment installment;

    @BeforeEach
    void setUp() {
        paymentRequest = TestUtils.createMockPaymentRequest();
        loan = TestUtils.createMockLoan();
        customer = TestUtils.createMockCustomer(List.of(loan));
        installment = loan.getLoanInstallments().get(0);
    }

    @Test
    void testPayLoanInstallments_SuccessfulPayment() {
        // Arrange:
        when(customerRepository.findByAccountNumber(paymentRequest.getAccountNumber()))
                .thenReturn(Optional.of(customer));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        when(loanInstallmentRepository.save(any(LoanInstallment.class))).thenReturn(installment);

        // Act:
        PaymentResponse response = loanInstallmentService.payLoanInstalments(paymentRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getNumberOfPaidInstallments());
        assertEquals(1000.0, response.getPaidAmount());
        assertTrue(response.getPaidLoanInstallments().get(0).getIsPaid());
        assertEquals(0.0, response.getRefundAmount());
    }

    @Test
    void testPayLoanInstallments_CustomerNotFound() {
        // Arrange:
        when(customerRepository.findByAccountNumber(paymentRequest.getAccountNumber()))
                .thenReturn(Optional.empty());

        // Act & Assert:
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            loanInstallmentService.payLoanInstalments(paymentRequest);
        });
        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void testPayLoanInstallments_LoanNotFound() {
        customer.setLoans(Collections.EMPTY_LIST);
        // Arrange:
        when(customerRepository.findByAccountNumber(paymentRequest.getAccountNumber()))
                .thenReturn(Optional.of(customer));

        // Act & Assert:
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            loanInstallmentService.payLoanInstalments(paymentRequest);
        });
        assertEquals("Loan not found ", exception.getMessage());
    }

    @Test
    void testPayLoanInstallments_InsufficientPaymentAmount() {

        paymentRequest.setAmount(500.0);

        // Arrange:
        when(customerRepository.findByAccountNumber(paymentRequest.getAccountNumber()))
                .thenReturn(Optional.of(customer));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        // Act:
        PaymentResponse response = loanInstallmentService.payLoanInstalments(paymentRequest);

        // Assert:
        assertNotNull(response);
        assertEquals(0, response.getNumberOfPaidInstallments());
        assertEquals(500.0, response.getRefundAmount());
    }
}
