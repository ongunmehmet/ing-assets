package com.creditmodule.ing.data;

import com.creditmodule.ing.entity.LoanInstallment;
import lombok.Data;

import java.util.List;

@Data
public class PaymentResponse {
    private double refundAmount;
    private double paidAmount;
    private int numberOfPaidInstallments;
    private List<LoanInstallment> paidLoanInstallments;
    private Long loanId;
}
