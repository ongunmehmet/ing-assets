package com.creditmodule.ing.service;

import com.creditmodule.ing.data.CreateLoanRequest;
import com.creditmodule.ing.data.PaymentRequest;
import com.creditmodule.ing.data.UserCustomerCreateRequest;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.Loan;
import com.creditmodule.ing.entity.LoanInstallment;
import com.creditmodule.ing.entity.User;
import com.creditmodule.ing.enums.Role;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class TestUtils {
    public static Customer createMockCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John");
        customer.setSurname("Doe");
        customer.setCreditLimit(10000L);
        customer.setUsedCreditLimit(2000L);
        customer.setUser(createMockUser());
        return customer;
    }
    public static Customer createMockCustomer(List<Loan> loan) {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John");
        customer.setSurname("Doe");
        customer.setCreditLimit(10000L);
        customer.setUsedCreditLimit(2000L);
        customer.setLoans(loan);
        return customer;
    }
    public static User createMockUser(){
        User user = new User();
        user.setAccountNumber("123456");
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRoles(Set.of(Role.ADMIN));
        return user;
    }
    public static Loan createMockLoan() {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setLoanAmount(5000L);
        loan.setNumberOfInstallment(5);
        loan.setNumberOfUnpaidInstallment(5);
        loan.setCreateDate(new Date());

        LoanInstallment installment = new LoanInstallment();
        installment.setId(1L);
        installment.setAmount(1000.0);
        installment.setIsPaid(false);
        installment.setDueDate(new Date());
        installment.setLoan(loan);

        loan.setLoanInstallments(Collections.singletonList(installment));
        return loan;
    }

    public static PaymentRequest createMockPaymentRequest() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAccountNumber("123456");
        paymentRequest.setLoanId(1L);
        paymentRequest.setAmount(1000.0);
        paymentRequest.setPaymentDate(new Date());
        return paymentRequest;
    }

    public static UserCustomerCreateRequest createMockUserCustomerCreateRequest() {
        UserCustomerCreateRequest request = new UserCustomerCreateRequest();
        request.setName("John");
        request.setSurname("Doe");
        request.setPassword("password123");
        request.setCreditLimit(10000L);
        return request;
    }
    public static CreateLoanRequest createMockLoanRequest (){
        CreateLoanRequest request = new CreateLoanRequest();
        request.setAccountNumber("123456");
        request.setLoanAmount(5000L);
        request.setNumberOfInstallment(12);
        request.setCreateDate(new Date());
        return request;
    }
}

