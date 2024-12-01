package com.creditmodule.ing.data;

import com.creditmodule.ing.annotation.ValidateInstallment;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class CreateLoanRequest {
    private String accountNumber;
    private Long loanAmount;
    @ValidateInstallment
    private int numberOfInstallment;
    @JsonFormat(pattern = "dd-MM-yyyy")
    private Date createDate;
}
