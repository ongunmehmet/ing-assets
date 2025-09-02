package com.creditmodule.ing.service;

import com.creditmodule.ing.data.*;

public interface IAssetService {
    CreateAssetResponse createAsset(CreateAssetRequest request);

    DeleteAssetResponse deleteAsset(Long id);
}
