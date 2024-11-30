package com.creditmodule.ing.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "loaninstallmens")
public class LoanInstallment {
    @Id
    private String id;
    private Long loanID;
    private Long amount;
    private Long paidAmount;
    private Date dueDate;
    private Date paymentDate;
    private Boolean isPaid;
}
