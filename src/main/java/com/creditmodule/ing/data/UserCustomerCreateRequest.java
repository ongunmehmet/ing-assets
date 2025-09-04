package com.creditmodule.ing.data;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserCustomerCreateRequest {
    private String surname;
    private String password;
    private String name;
    private BigDecimal creditLimit;
}
