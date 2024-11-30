package com.creditmodule.ing.enums;

public enum Role {

    ADMIN("ROLE_ADMIN"),
    CUSTOMER("ROLE_CUSTOMER");

    private final String roleName;

    Role(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
