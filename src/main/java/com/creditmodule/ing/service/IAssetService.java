package com.creditmodule.ing.service;

import com.creditmodule.ing.data.*;
import com.creditmodule.ing.entity.Asset;

import java.util.List;
import java.util.Optional;

public interface IAssetService {
    CreateAssetResponse createAsset(CreateAssetRequest request);

    List<Asset> listAllAssets();

    List<CustomerAssetResponse> listCustomerAssets(Long customerId);

    DeleteAssetResponse deleteAsset(Long assetId);

    Optional<Asset> findAssetById(Long id);
}
