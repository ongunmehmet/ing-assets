package com.creditmodule.ing.service;

import com.creditmodule.ing.data.CreateLoanRequest;
import com.creditmodule.ing.data.CreateLoanRessponse;
import com.creditmodule.ing.entity.Loan;

import java.util.List;

public interface LoanService {
    CreateLoanRessponse createLoan(CreateLoanRequest request);

    List<Loan> findLoansById(Long id);
}
