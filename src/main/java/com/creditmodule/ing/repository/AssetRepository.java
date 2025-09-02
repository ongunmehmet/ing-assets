package com.creditmodule.ing.repository;

import com.creditmodule.ing.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset,Long> {
    Optional<Asset> findByAssetName(String assetName);
}
