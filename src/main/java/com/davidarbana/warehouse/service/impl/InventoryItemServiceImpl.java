package com.davidarbana.warehouse.service.impl;

import com.davidarbana.warehouse.dto.request.InventoryItemRequest;
import com.davidarbana.warehouse.dto.response.ResponseDtos;
import com.davidarbana.warehouse.entity.InventoryItem;
import com.davidarbana.warehouse.exception.ResourceNotFoundException;
import com.davidarbana.warehouse.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryItemServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(InventoryItemServiceImpl.class);

    private final InventoryItemRepository inventoryItemRepository;

    public List<ResponseDtos.InventoryItemResponse> getAllItems() {
        return inventoryItemRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ResponseDtos.InventoryItemResponse getItemById(Long id) {
        return mapToResponse(findById(id));
    }

    public ResponseDtos.InventoryItemResponse createItem(InventoryItemRequest.Create request) {
        InventoryItem item = InventoryItem.builder()
                .itemName(request.getItemName())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .packageVolume(request.getPackageVolume())
                .build();
        inventoryItemRepository.save(item);
        log.info("New inventory item added: {}", item.getItemName());
        return mapToResponse(item);
    }

    public ResponseDtos.InventoryItemResponse updateItem(Long id, InventoryItemRequest.Update request) {
        InventoryItem item = findById(id);

        if (request.getItemName() != null) item.setItemName(request.getItemName());
        if (request.getQuantity() != null) item.setQuantity(request.getQuantity());
        if (request.getUnitPrice() != null) item.setUnitPrice(request.getUnitPrice());
        if (request.getPackageVolume() != null) item.setPackageVolume(request.getPackageVolume());

        inventoryItemRepository.save(item);
        return mapToResponse(item);
    }

    public void deleteItem(Long id) {
        InventoryItem item = findById(id);
        inventoryItemRepository.delete(item);
    }

    // used by OrderServiceImpl as well
    public InventoryItem findById(Long id) {
        return inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
    }

    public ResponseDtos.InventoryItemResponse mapToResponse(InventoryItem item) {
        return ResponseDtos.InventoryItemResponse.builder()
                .id(item.getId())
                .itemName(item.getItemName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .packageVolume(item.getPackageVolume())
                .build();
    }
}
