package com.davidarbana.warehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

public class OrderRequest {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Create {
        private LocalDate deadlineDate;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AddItem {
        @NotNull(message = "Inventory item ID is required")
        private Long inventoryItemId;
        @Min(value = 1, message = "Quantity must be at least 1")
        private int quantity;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateItem {
        @Min(value = 1, message = "Quantity must be at least 1")
        private int quantity;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Decline {
        private String reason;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ScheduleDelivery {
        @NotNull(message = "Delivery date is required")
        private LocalDate deliveryDate;
        @NotNull(message = "At least one truck is required")
        private List<Long> truckIds;
    }
}
