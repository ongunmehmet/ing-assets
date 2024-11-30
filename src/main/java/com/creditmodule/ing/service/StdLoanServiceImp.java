package com.creditmodule.ing.service;

import com.creditmodule.ing.data.CreateLoanRequest;
import com.creditmodule.ing.data.CreateLoanResponse;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.Loan;
import com.creditmodule.ing.entity.LoanInstallment;
import com.creditmodule.ing.exceptions.CreditLimitExceedException;
import com.creditmodule.ing.repository.CustomerRepository;
import com.creditmodule.ing.repository.LoanInstallmentRepository;
import com.creditmodule.ing.repository.LoanRepository;
import com.creditmodule.ing.utils.DateUtils;
import com.creditmodule.ing.utils.UserUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.creditmodule.ing.utils.DateUtils.addOneMonth;
import static com.creditmodule.ing.utils.DateUtils.getFirstDayOfNextMonth;

@Service
@Slf4j
@AllArgsConstructor
public class StdLoanServiceImp implements LoanService {
    private CustomerRepository customerRepository;
    private LoanRepository loanRepository;
    private LoanInstallmentRepository loanInstallmentRepository;
    private UserUtils userUtils;
    private final double INTEREST_RATE = 0.5;

    @Override
    @Transactional
    public CreateLoanResponse createLoan(CreateLoanRequest request) {

        Customer customer = customerRepository.findByAccountNumber(request.getAccountNumber()).get();
        if(request.getLoanAmount() >= customer.getCreditLimit()){
            log.error("Loan not created for Customer {} Amount {}",customer.getName(), request.getLoanAmount());
            throw new CreditLimitExceedException("Credit Limit Exceed");
        }

        customer.setUsedCreditLimit(customer.getUsedCreditLimit() + request.getLoanAmount());
        customer.setCreditLimit(customer.getCreditLimit() - request.getLoanAmount());
        customerRepository.save(customer);
        Loan loan = new Loan();
        loan.setLoanAmount(request.getLoanAmount());
        loan.setNumberOfInstallment(request.getNumberOfInstallment());
        loan.setNumberOfUnpaidInstallment(request.getNumberOfInstallment());
        loan.setCreateDate(new Date());
        loan.setCustomer(customer);

        loan = loanRepository.save(loan);
        List<LoanInstallment> installments = new ArrayList<>();
        double totalPaymentAmount = (1+INTEREST_RATE) * request.getLoanAmount();
        Date InstallmentDueDate = getFirstDayOfNextMonth(request.getCreateDate());

        for (int i = 0; i < request.getNumberOfInstallment(); i++) {
            LoanInstallment installment = new LoanInstallment();
            installment.setLoan(loan);
            installment.setAmount((totalPaymentAmount / request.getNumberOfInstallment()));  // Amount per installment
            installment.setDueDate(InstallmentDueDate);
            installment.setIsPaid(false);
            InstallmentDueDate = DateUtils.addOneMonth(InstallmentDueDate);

            installments.add(installment);
        }
        loan.setLoanInstallments(installments);
        loanInstallmentRepository.saveAll(installments);
        CreateLoanResponse response = new CreateLoanResponse();
        response.setLoan(loan);
        response.setCreateDate(request.getCreateDate());
        response.setLoanAmount(request.getLoanAmount());
        response.setTotalPayment(totalPaymentAmount);
        response.setNumberOfInstallment(request.getNumberOfInstallment());
        return response;
    }

    @Override
    public List<Loan> findCustomerLoansById(Long customerId) {
        return loanRepository.findByCustomerId(customerId);
    }

    @Override
    public Loan findCustomerLoanById(Long id, Long customerId) {
        return loanRepository.findByCustomerIdAndLoanId(id,customerId);
    }
}
