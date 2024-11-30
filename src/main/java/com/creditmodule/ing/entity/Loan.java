package com.creditmodule.ing.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "loan")
public class Loan {
    @Id
    private Long id;
    private Long customerId;
    private Long loanAmount;
    private int numberOfInstallment;
    private Date createDate;
    private boolean isPaid;
}
