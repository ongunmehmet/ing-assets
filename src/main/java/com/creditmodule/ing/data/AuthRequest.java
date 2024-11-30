package com.creditmodule.ing.data;

import lombok.Data;

@Data
public class AuthRequest {
    private String accountNumber;
    private String password;
}
