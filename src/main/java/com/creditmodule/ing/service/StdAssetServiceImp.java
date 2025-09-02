package com.creditmodule.ing.service;

import com.creditmodule.ing.data.CreateAssetRequest;
import com.creditmodule.ing.data.CreateAssetResponse;
import com.creditmodule.ing.data.DeleteAssetResponse;
import com.creditmodule.ing.entity.Asset;
import com.creditmodule.ing.repository.AssetRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class StdAssetServiceImp implements IAssetService {
    private AssetRepository assetRepository;

    @Override
    public CreateAssetResponse createAsset(CreateAssetRequest request) {
        Asset asset = new Asset();
        asset.setAssetName(request.getAssetName());
        asset.setSize(request.getInitialSize());
        asset.setUsableSize(request.getInitialSize());
        Asset savedAsset = assetRepository.save(asset);
        return new CreateAssetResponse(
                savedAsset.getId(),
                savedAsset.getAssetName(),
                savedAsset.getSize(),
                savedAsset.getUsableSize(),
                "Asset created successfully"
        );
    }

    @Override
    public DeleteAssetResponse deleteAsset(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        if (asset.getSize() != asset.getUsableSize()) {
            throw new IllegalStateException("Asset cannot be deleted because some of it is in use");
        }

        assetRepository.delete(asset);

        return new DeleteAssetResponse(
                asset.getId(),
                asset.getAssetName(),
                "Asset deleted successfully");
    }
}
