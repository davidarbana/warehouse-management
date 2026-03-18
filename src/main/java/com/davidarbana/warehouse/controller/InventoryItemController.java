package com.davidarbana.warehouse.controller;

import com.davidarbana.warehouse.dto.request.InventoryItemRequest;
import com.davidarbana.warehouse.dto.response.ResponseDtos;
import com.davidarbana.warehouse.service.impl.InventoryItemServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory")
public class InventoryItemController {

    private final InventoryItemServiceImpl inventoryItemService;

    @GetMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'CLIENT')")
    public ResponseEntity<List<ResponseDtos.InventoryItemResponse>> getAll() {
        return ResponseEntity.ok(inventoryItemService.getAllItems());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'CLIENT')")
    public ResponseEntity<ResponseDtos.InventoryItemResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryItemService.getItemById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ResponseDtos.InventoryItemResponse> create(@Valid @RequestBody InventoryItemRequest.Create request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryItemService.createItem(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ResponseDtos.InventoryItemResponse> update(@PathVariable Long id,
                                                                      @RequestBody InventoryItemRequest.Update request) {
        return ResponseEntity.ok(inventoryItemService.updateItem(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inventoryItemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
