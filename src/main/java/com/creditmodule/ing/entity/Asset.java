package com.creditmodule.ing.entity;



import jakarta.persistence.*;
import lombok.Data;


import java.util.ArrayList;
import java.util.List;


@Data
@Entity
@Table(name = "assets", uniqueConstraints = {
        @UniqueConstraint(columnNames = "assetName")
})
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String assetName;
    private double size;
    private double usableSize;
    private double initialPrice;
    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerAsset> customerAssets = new ArrayList<>();
}
