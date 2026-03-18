package com.davidarbana.warehouse.service;

import com.davidarbana.warehouse.dto.request.InventoryItemRequest;
import com.davidarbana.warehouse.dto.response.ResponseDtos;
import com.davidarbana.warehouse.entity.InventoryItem;
import com.davidarbana.warehouse.exception.ResourceNotFoundException;
import com.davidarbana.warehouse.repository.InventoryItemRepository;
import com.davidarbana.warehouse.service.impl.InventoryItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryItemServiceTest {

    @InjectMocks
    private InventoryItemServiceImpl inventoryItemService;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    private InventoryItem item;

    @BeforeEach
    void setUp() {
        item = InventoryItem.builder()
                .id(1L)
                .itemName("Industrial Drill")
                .quantity(50)
                .unitPrice(299.99)
                .packageVolume(2.5)
                .build();
    }

    @Test
    void getAllItems_ShouldReturnList() {
        when(inventoryItemRepository.findAll()).thenReturn(List.of(item));

        List<ResponseDtos.InventoryItemResponse> result = inventoryItemService.getAllItems();

        assertEquals(1, result.size());
        assertEquals("Industrial Drill", result.get(0).getItemName());
    }

    @Test
    void getItemById_ShouldReturnItem() {
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(item));

        ResponseDtos.InventoryItemResponse result = inventoryItemService.getItemById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Industrial Drill", result.getItemName());
    }

    @Test
    void getItemById_NotFound_ShouldThrow() {
        when(inventoryItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                inventoryItemService.getItemById(99L));
    }

    @Test
    void createItem_ShouldSaveAndReturn() {
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(item);

        InventoryItemRequest.Create request = new InventoryItemRequest.Create("Industrial Drill", 50, 299.99, 2.5);
        ResponseDtos.InventoryItemResponse result = inventoryItemService.createItem(request);

        assertNotNull(result);
        verify(inventoryItemRepository, times(1)).save(any(InventoryItem.class));
    }

    @Test
    void deleteItem_ShouldCallDelete() {
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(item));

        inventoryItemService.deleteItem(1L);

        verify(inventoryItemRepository, times(1)).delete(item);
    }

    @Test
    void updateItem_ShouldUpdateFields() {
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(item);

        InventoryItemRequest.Update request = new InventoryItemRequest.Update("Updated Drill", null, 349.99, null);
        ResponseDtos.InventoryItemResponse result = inventoryItemService.updateItem(1L, request);

        assertEquals("Updated Drill", item.getItemName());
        assertEquals(349.99, item.getUnitPrice());
        assertEquals(50, item.getQuantity()); // unchanged
    }
}
