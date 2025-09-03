package com.creditmodule.ing.service;

import com.creditmodule.ing.repository.AssetRepository;
import com.creditmodule.ing.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StdAssetServiceImpTest {
    @Mock
    private AssetRepository assetRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private StdAssetServiceImp assetService;


    // 1. Create asset success
    @Test
    void createAsset_shouldSucceed_whenNameIsUnique() {
        var request = TestUtils.createAssetRequest("Laptop", 10.0, 5000.0);
        var asset = TestUtils.asset("Laptop", 10.0, 5000.0);
        asset.setId(1L); // Important: ID is set after save

        when(assetRepository.save(any())).thenReturn(asset);

        var result = assetService.createAsset(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getAssetName());
        assertEquals(10.0, result.getSize());
        assertEquals(10.0, result.getUsableSize());
        assertEquals("Asset created successfully", result.getMessage());
    }

    // 2. Create asset fail (name already exists)
    @Test
    void createAsset_shouldFail_whenNameAlreadyExists_dueToConstraintViolation() {
        var request = TestUtils.createAssetRequest("Laptop", 10.0, 5000.0);

        when(assetRepository.save(any())).thenThrow(new DataIntegrityViolationException("Unique constraint violation"));
        var exception = assertThrows(IllegalArgumentException.class, () -> assetService.createAsset(request));
        assertTrue(exception.getMessage().contains("Laptop"));

        verify(assetRepository).save(any());
    }

    // 3. Delete asset success
    @Test
    void deleteAsset_shouldSucceed_whenUsableSizeEqualsSize() {
        var asset = TestUtils.asset("Monitor", 5.0, 1500.0);
        asset.setId(1L);
        asset.setSize(5.0);
        asset.setUsableSize(5.0);

        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));

        assetService.deleteAsset(1L);

        verify(assetRepository).delete(asset);
    }

    // 4. Delete asset fail due to different size and usable size
    @Test
    void deleteAsset_shouldFail_whenUsableSizeDiffersFromSize() {
        var asset = TestUtils.asset("Monitor", 10.0, 1500.0);
        asset.setId(2L);
        asset.setUsableSize(5.0); // different than size

        when(assetRepository.findById(2L)).thenReturn(Optional.of(asset));

        assertThrows(IllegalStateException.class, () -> assetService.deleteAsset(2L));
        verify(assetRepository, never()).delete(any());
    }

    // 5. Find all assets (after saving 2)
    @Test
    void findAllAssets_shouldReturnTwoAssets() {
        var a1 = TestUtils.asset("Asset1", 2.0, 500.0);
        var a2 = TestUtils.asset("Asset2", 3.0, 1000.0);

        when(assetRepository.findAll()).thenReturn(List.of(a1, a2));

        var result = assetService.listAllAssets();

        assertEquals(2, result.size());
    }

    // 6. List customer's assets (2 assets mapped in CustomerAsset)
    @Test
    void getCustomerAssets_shouldReturnTwoAssets() {
        var customer = TestUtils.customer("John", "Doe");
        customer.setId(1L);

        var a1 = TestUtils.asset("Asset1", 2.0, 500.0);
        var a2 = TestUtils.asset("Asset2", 3.0, 1000.0);

        var ca1 = TestUtils.customerAsset(customer, a1, 2.0, 2.0);
        var ca2 = TestUtils.customerAsset(customer, a2, 3.0, 3.0);

        customer.setCustomerAssets(List.of(ca1, ca2));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        var result = assetService.listCustomerAssets(1L);

        assertEquals(2, result.size());
    }
    @Test
    void findAssetById_shouldReturnAsset_whenExists() {
        var asset = TestUtils.asset("Switch", 5.0, 2000.0);
        asset.setId(99L);

        when(assetRepository.findById(99L)).thenReturn(Optional.of(asset));

        var result = assetService.findAssetById(99L);

        assertTrue(result.isPresent());
        assertEquals("Switch", result.get().getAssetName());
        assertEquals(5.0, result.get().getSize());
    }

    @Test
    void findAssetById_shouldReturnEmpty_whenNotFound() {
        when(assetRepository.findById(100L)).thenReturn(Optional.empty());

        var result = assetService.findAssetById(100L);

        assertTrue(result.isEmpty());
    }
}
