package com.creditmodule.ing.controller;

import com.creditmodule.ing.data.AssetDetailDto;
import com.creditmodule.ing.data.CreateAssetRequest;
import com.creditmodule.ing.data.CreateAssetResponse;
import com.creditmodule.ing.data.CustomerAssetResponse;
import com.creditmodule.ing.service.IAssetService;
import com.creditmodule.ing.service.IOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/asset")
@Tag(name = "Asset", description = "Endpoints for assets")
public class AssetController {

    @Autowired
    private IAssetService assetService;
    @Autowired
    private IOrderService orderService;


    @PostMapping("/createAsset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CreateAssetResponse> createAsset(@RequestBody CreateAssetRequest request) {
        CreateAssetResponse response = assetService.createAsset(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.ok("Asset deleted successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssetDetailDto> showAsset(@PathVariable Long id) {
        AssetDetailDto asset = assetService.findAssetById(id);
        return ResponseEntity.ok(asset);
    }

    @GetMapping("/assets/all")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<List<AssetDetailDto>> listAssets() {
        List<AssetDetailDto> assets = assetService.listAllAssets();
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/assets/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<List<CustomerAssetResponse>> listCustomerAssets(@PathVariable Long id) {
        List<CustomerAssetResponse> assets = assetService.listCustomerAssets(id);
        return ResponseEntity.ok(assets);
    }

}
