package com.davidarbana.warehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public class InventoryItemRequest {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Create {
        @NotBlank(message = "Item name is required")
        private String itemName;
        @Min(value = 0, message = "Quantity cannot be negative")
        private int quantity;
        @Min(value = 0, message = "Unit price cannot be negative")
        private double unitPrice;
        @Min(value = 0, message = "Package volume cannot be negative")
        private double packageVolume;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Update {
        private String itemName;
        private Integer quantity;
        private Double unitPrice;
        private Double packageVolume;
    }
}
