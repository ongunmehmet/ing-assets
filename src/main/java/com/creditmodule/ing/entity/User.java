package com.creditmodule.ing.entity;

import com.creditmodule.ing.enums.Role;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.Set;


@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;

    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL) // Bidirectional mapping
    private Customer customer;

}