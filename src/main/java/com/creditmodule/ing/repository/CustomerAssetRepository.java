package com.creditmodule.ing.repository;

import com.creditmodule.ing.entity.Asset;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.CustomerAsset;
import com.creditmodule.ing.entity.CustomerAssetId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerAssetRepository extends JpaRepository<CustomerAsset, CustomerAssetId> {

    Optional<CustomerAsset> findByCustomerAndAsset(Customer customer, Asset asset);
}
