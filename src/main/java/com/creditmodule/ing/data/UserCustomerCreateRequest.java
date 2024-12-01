package com.creditmodule.ing.data;

import lombok.Data;

@Data
public class UserCustomerCreateRequest {
    private String surname;
    private String password;
    private String name;
    private Long creditLimit;
}
