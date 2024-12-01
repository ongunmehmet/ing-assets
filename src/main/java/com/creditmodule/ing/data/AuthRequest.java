package com.creditmodule.ing.data;

import jakarta.annotation.Nullable;
import lombok.Data;

@Data
public class AuthRequest {
    @Nullable
    private String accountNumber;
    @Nullable
    private String password;
}
