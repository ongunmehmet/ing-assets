package com.creditmodule.ing.service;

import com.creditmodule.ing.data.CreateAssetRequest;
import com.creditmodule.ing.data.CreateAssetResponse;
import com.creditmodule.ing.data.CustomerAssetResponse;
import com.creditmodule.ing.data.DeleteAssetResponse;
import com.creditmodule.ing.entity.Asset;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.repository.AssetRepository;
import com.creditmodule.ing.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class StdAssetServiceImp implements IAssetService {
    private AssetRepository assetRepository;
    private CustomerRepository customerRepository;

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
    public List<Asset> listAllAssets() {
        return assetRepository.findAll();
    }

    @Override
    public List<CustomerAssetResponse> listCustomerAssets(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return customer.getCustomerAssets().stream()
                .map(ca -> new CustomerAssetResponse(
                        ca.getAsset().getId(),
                        ca.getAsset().getAssetName(),
                        ca.getSize(),
                        ca.getUsableSize()
                ))
                .toList();
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
