package com.creditmodule.ing.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;


import java.util.Date;

@Data
public class PaymentRequest {
    private double amount;
    @JsonFormat(pattern = "dd-MM-yyyy")
    private Date paymentDate;
    private String AccountNumber;
    private Long loanId;
}
