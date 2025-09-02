package com.creditmodule.ing.controller;

import com.creditmodule.ing.data.CreateAssetRequest;
import com.creditmodule.ing.data.CreateAssetResponse;
import com.creditmodule.ing.entity.Asset;
import com.creditmodule.ing.service.IAssetService;
import com.creditmodule.ing.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
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

    @Operation(summary = "Create Asset", description = "Create a new asset ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid asset request")
    })
    @PostMapping("/createAsset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CreateAssetResponse> createAsset(@RequestBody  CreateAssetRequest request) {
        CreateAssetResponse response = assetService.createAsset(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Show asset by ID", description = "Retrieve a specific asset by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset details retrieved"),
            @ApiResponse(responseCode = "404", description = "Asset not found")
    })
    @DeleteMapping("/assets/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.ok("Asset deleted successfully");
    }
    @GetMapping("/assets/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<List<Asset>> listAssets(@PathVariable Long id) {
        List<Asset> assets = orderService.listAssets(id);
        return ResponseEntity.ok(assets);
    }

}
