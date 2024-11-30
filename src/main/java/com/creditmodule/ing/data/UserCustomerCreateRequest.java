package com.creditmodule.ing.data;

import lombok.Data;

@Data
public class UserCustomerCreateRequest {
    private String username;
    private String password;
    private String name;
    private String surname;
    private Long creditLimit;
}
