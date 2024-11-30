package com.creditmodule.ing.data;

import com.creditmodule.ing.entity.Loan;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import lombok.Data;

import java.util.Date;

@Data
public class CreateLoanResponse {
    @Nullable
    private Long customerId;
    private Long loanAmount;
    private double totalPayment;
    private int numberOfInstallment;
    @JsonFormat(pattern = "dd-MM-yyyy")
    private Date createDate;
    private Loan loan;
}
