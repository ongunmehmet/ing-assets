package com.creditmodule.ing.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

@Data
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    private Long id; // Shared primary key with User

    private String name;

    private String surname;
    @ColumnDefault("10000")
    private Long creditLimit;

    private Long usedCreditLimit;

    @OneToOne
    @JsonIgnore
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
}
