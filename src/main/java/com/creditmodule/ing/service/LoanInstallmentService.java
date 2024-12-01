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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class LoanInstallmentService {
    private LoanInstallmentRepository loanInstallmentRepository;
    private CustomerRepository customerRepository;
    private LoanRepository loanRepository;

    public LoanInstallment findLoanInstellmentById(Long id) {
        return loanInstallmentRepository.findById(id).orElseThrow(() ->
                new RuntimeException("LoanInstallment not found"));
    }

    public List<LoanInstallment> findLoanInstellmentsById(Long id) {
        return loanInstallmentRepository.findAllByLoanId(id);
    }

    @Transactional
    public PaymentResponse payLoanInstalments(PaymentRequest payment) {
        Customer customer = customerRepository.findByAccountNumber(payment.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        List<Loan> customerLoans = customer.getLoans();
        Loan requestedLoan = customerLoans.stream()
                .filter(loan -> loan.getId().equals(payment.getLoanId()))
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("Loan not found "));
        List<LoanInstallment> unpaidInstallments = requestedLoan.getLoanInstallments().stream()
                .filter(installment -> installment.getIsPaid() == null || !installment.getIsPaid())
                .sorted(Comparator.comparing(LoanInstallment::getDueDate))
                .collect(Collectors.toList());
        int payCounter = 0;
        double amount = payment.getAmount();
        double paidAmount = 0;
        List<LoanInstallment> paidInstallments = new ArrayList<>();
        for (int i = 0; i < unpaidInstallments.size(); i++) {

            LoanInstallment currentInstalment = unpaidInstallments.get(i);
            if (amount >= currentInstalment.getAmount() && payCounter <= 3) {
                int daysDifference = daysBetween(currentInstalment.getDueDate(), payment.getPaymentDate());
                Double LoanIstalmentAmount = currentInstalment.getAmount();
                Double adjustedAmount = LoanIstalmentAmount;
                if (daysDifference < 0) {
                    adjustedAmount -= 0.001 * Math.abs(daysDifference) * LoanIstalmentAmount;
                } else if (daysDifference > 0) {
                    adjustedAmount += 0.001 * Math.abs(daysDifference) * LoanIstalmentAmount;
                }
                if (adjustedAmount > amount) {
                    break;
                }
                currentInstalment.setPaidAmount(adjustedAmount);
                currentInstalment.setIsPaid(true);
                currentInstalment.setPaymentDate(payment.getPaymentDate());
                paidInstallments.add(currentInstalment);
                loanInstallmentRepository.save(currentInstalment);
                log.info("Instalment Paid With Id {} for {} Id loan ", currentInstalment.getId()
                        , currentInstalment.getLoan().getId());
                amount -= adjustedAmount;
                payCounter++;
                paidAmount += adjustedAmount;
            } else {
                log.info("Payment Amount unsucceficend exiting from payment");
                break;
            }
        }
        requestedLoan.setNumberOfUnpaidInstallment(unpaidInstallments.size() - payCounter);
        if (unpaidInstallments.size() - payCounter == 0) {
            requestedLoan.setPaid(true);
        }
        loanRepository.save(requestedLoan);
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setLoanId(requestedLoan.getId());
        paymentResponse.setNumberOfPaidInstallments(payCounter);
        paymentResponse.setPaidAmount(paidAmount);
        paymentResponse.setPaidLoanInstallments(paidInstallments);
        paymentResponse.setRefundAmount(amount);
        return paymentResponse;
    }

    private int daysBetween(Date dueDate, Date currentDate) {
        Calendar dueCalendar = Calendar.getInstance();
        dueCalendar.setTime(dueDate);
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentDate);

        long diffMillis = currentCalendar.getTimeInMillis() - dueCalendar.getTimeInMillis();
        return (int) (diffMillis / (1000 * 60 * 60 * 24));
    }
}
