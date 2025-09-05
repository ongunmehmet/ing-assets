package com.creditmodule.ing.service;



import com.creditmodule.ing.data.AssetDetailDto;
import com.creditmodule.ing.data.CreateAssetRequest;
import com.creditmodule.ing.data.CreateAssetResponse;
import com.creditmodule.ing.data.CustomerAssetResponse;
import com.creditmodule.ing.data.DeleteAssetResponse;

import java.util.List;

public interface IAssetService {
    CreateAssetResponse createAsset(CreateAssetRequest request);

    List<AssetDetailDto> listAllAssets();

    List<CustomerAssetResponse> listCustomerAssets(Long customerId);

    DeleteAssetResponse deleteAsset(Long assetId);

    AssetDetailDto findAssetById(Long id);
}
