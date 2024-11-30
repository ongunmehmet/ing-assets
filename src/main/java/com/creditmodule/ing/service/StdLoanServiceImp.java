package com.creditmodule.ing.service;

import com.creditmodule.ing.data.CreateLoanRequest;
import com.creditmodule.ing.data.CreateLoanRessponse;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.Loan;
import com.creditmodule.ing.repository.CustomerRepository;
import com.creditmodule.ing.repository.LoanRepository;
import com.creditmodule.ing.utils.UserUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StdLoanServiceImp implements LoanService {
    private CustomerRepository customerRepository;
    private LoanRepository loanRepository;
    private UserUtils userUtils;

    @Override
    public CreateLoanRessponse createLoan(CreateLoanRequest request) {
        String username= userUtils.getCurrentUsername();
        Optional<Customer> customer = customerRepository.findById(1L);

        return null;
    }

    @Override
    public List<Loan> findLoansById(Long id) {
        return null;
    }
}
