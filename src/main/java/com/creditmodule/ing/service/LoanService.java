package com.creditmodule.ing.service;

import com.creditmodule.ing.data.CreateLoanRequest;
import com.creditmodule.ing.data.CreateLoanResponse;
import com.creditmodule.ing.entity.Loan;

import java.util.List;

public interface LoanService {
    CreateLoanResponse createLoan(CreateLoanRequest request);

    List<Loan> findCustomerLoansById(Long id);


    Loan findCustomerLoanById(Long id, Long customerId);
}
