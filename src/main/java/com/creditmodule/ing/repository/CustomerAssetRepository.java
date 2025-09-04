package com.creditmodule.ing.repository;

import com.creditmodule.ing.entity.Asset;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.CustomerAsset;
import com.creditmodule.ing.entity.CustomerAssetId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerAssetRepository extends JpaRepository<CustomerAsset, CustomerAssetId> {


    // ---- Plain reads ---------------------------------------------------------

    // by entity refs
    Optional<CustomerAsset> findByCustomerAndAsset(Customer customer, Asset asset);

    // by raw ids (useful when you only have ids, avoids extra entity loads)
    @Query("select ca from CustomerAsset ca " +
            "where ca.customer.id = :customerId and ca.asset.id = :assetId")
    Optional<CustomerAsset> findByCustomerIdAndAssetId(@Param("customerId") Long customerId,
                                                       @Param("assetId") Long assetId);

    // list all holdings for a customer
    @Query("select ca from CustomerAsset ca where ca.customer.id = :customerId")
    List<CustomerAsset> findAllByCustomerId(@Param("customerId") Long customerId);

    // quick existence check
    boolean existsByCustomerAndAsset(Customer customer, Asset asset);


    // ---- Locked (matching path) ---------------------------------------------

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ca from CustomerAsset ca " +
            "where ca.customer.id = :customerId and ca.asset.id = :assetId")
    Optional<CustomerAsset> findForUpdate(@Param("customerId") Long customerId,
                                          @Param("assetId") Long assetId);


    // ---- Small helper --------------------------------------------------------

    default CustomerAssetId idOf(Long customerId, Long assetId) {
        CustomerAssetId id = new CustomerAssetId();
        id.setCustomerId(customerId);
        id.setAssetId(assetId);
        return id;
    }
}

