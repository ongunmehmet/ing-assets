package com.creditmodule.ing.service;

import com.creditmodule.ing.data.AssetDetailDto;
import com.creditmodule.ing.repository.AssetRepository;
import com.creditmodule.ing.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StdAssetServiceImpTest {

    @Mock private AssetRepository assetRepository;
    @Mock private CustomerRepository customerRepository;

    @InjectMocks private StdAssetServiceImp assetService;

    @Test
    void createAsset_shouldSucceed_whenNameIsUnique() {
        var request = TestUtils.createAssetRequest("Laptop", BigDecimal.TEN, BigDecimal.valueOf(5000));
        var asset = TestUtils.asset("Laptop", BigDecimal.TEN, BigDecimal.valueOf(5000));
        asset.setId(1L);

        when(assetRepository.save(any())).thenReturn(asset);

        var result = assetService.createAsset(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getAssetName());
        assertEquals(BigDecimal.TEN, result.getSize());
        assertEquals(BigDecimal.TEN, result.getUsableSize());
        assertEquals("Asset created successfully", result.getMessage());
    }

    @Test
    void createAsset_shouldFail_whenNameAlreadyExists_dueToConstraintViolation() {
        var request = TestUtils.createAssetRequest("Laptop", BigDecimal.TEN, BigDecimal.valueOf(5000));

        when(assetRepository.save(any())).thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        var exception = assertThrows(IllegalArgumentException.class, () -> assetService.createAsset(request));
        assertTrue(exception.getMessage().contains("Laptop"));

        verify(assetRepository).save(any());
    }

    @Test
    void deleteAsset_shouldSucceed_whenUsableSizeEqualsSize() {
        var asset = TestUtils.asset("Monitor", BigDecimal.valueOf(5), BigDecimal.valueOf(1500));
        asset.setId(1L);
        asset.setSize(BigDecimal.valueOf(5));
        asset.setUsableSize(BigDecimal.valueOf(5));

        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));

        assetService.deleteAsset(1L);

        verify(assetRepository).delete(asset);
    }

    @Test
    void deleteAsset_shouldFail_whenUsableSizeDiffersFromSize() {
        var asset = TestUtils.asset("Monitor", BigDecimal.TEN, BigDecimal.valueOf(1500));
        asset.setId(2L);
        asset.setUsableSize(BigDecimal.valueOf(5));

        when(assetRepository.findById(2L)).thenReturn(Optional.of(asset));

        assertThrows(IllegalStateException.class, () -> assetService.deleteAsset(2L));
        verify(assetRepository, never()).delete(any());
    }

    @Test
    void listAllAssets_shouldReturnTwoAssetDtos() {
        var a1 = TestUtils.asset("Asset1", BigDecimal.valueOf(2), BigDecimal.valueOf(500));
        var a2 = TestUtils.asset("Asset2", BigDecimal.valueOf(3), BigDecimal.valueOf(1000));

        when(assetRepository.findAll()).thenReturn(List.of(a1, a2));

        var result = assetService.listAllAssets();

        assertNotNull(result);
        assertEquals(2, result.size());

        AssetDetailDto d1 = result.getFirst();
        assertEquals("Asset1", d1.assetName());
        assertEquals(BigDecimal.valueOf(2), d1.size());
        assertEquals(BigDecimal.valueOf(2), d1.usableSize());
    }

    @Test
    void getCustomerAssets_shouldReturnTwoAssets() {
        var customer = TestUtils.customer("John", "Doe");
        customer.setId(1L);

        var a1 = TestUtils.asset("Asset1", BigDecimal.valueOf(2), BigDecimal.valueOf(500));
        var a2 = TestUtils.asset("Asset2", BigDecimal.valueOf(3), BigDecimal.valueOf(1000));

        var ca1 = TestUtils.customerAsset(customer, a1, BigDecimal.valueOf(2), BigDecimal.valueOf(2));
        var ca2 = TestUtils.customerAsset(customer, a2, BigDecimal.valueOf(3), BigDecimal.valueOf(3));

        customer.setCustomerAssets(List.of(ca1, ca2));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        var result = assetService.listCustomerAssets(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Asset1", result.get(0).getAssetName());
        assertEquals("Asset2", result.get(1).getAssetName());
    }

    @Test
    void findAssetById_shouldReturnAssetDetail_whenExists() {
        var asset = TestUtils.asset("Switch", BigDecimal.valueOf(5), BigDecimal.valueOf(2000));
        asset.setId(99L);

        when(assetRepository.findById(99L)).thenReturn(Optional.of(asset));

        var dto = assetService.findAssetById(99L);

        assertNotNull(dto);
        assertEquals(99L, dto.id());
        assertEquals("Switch", dto.assetName());
        assertEquals(BigDecimal.valueOf(5), dto.size());
        assertEquals(BigDecimal.valueOf(5), dto.usableSize());
        assertEquals(BigDecimal.valueOf(2000), dto.initialPrice());
    }

    @Test
    void findAssetById_shouldThrow_whenNotFound() {
        when(assetRepository.findById(100L)).thenReturn(Optional.empty());

        var ex = assertThrows(RuntimeException.class, () -> assetService.findAssetById(100L));
        assertTrue(ex.getMessage().toLowerCase().contains("asset not found"));
    }
}
