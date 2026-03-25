package com.davidarbana.warehouse.dto.response;

import com.davidarbana.warehouse.enums.OrderStatus;
import com.davidarbana.warehouse.enums.Role;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ResponseDtos {

    // Auth
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AuthResponse {
        private String token;
        private String username;
        private String role;
    }

    // User
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UserResponse {
        private Long id;
        private String fullName;
        private String username;
        private String email;
        private Role role;
        private boolean enabled;
    }

    // Inventory Item
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class InventoryItemResponse {
        private Long id;
        private String itemName;
        private int quantity;
        private double unitPrice;
        private double packageVolume;
    }

    // Truck
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TruckResponse {
        private Long id;
        private String chassisNumber;
        private String licensePlate;
        private double containerVolume;
    }

    // Order Item
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderItemResponse {
        private Long id;
        private Long inventoryItemId;
        private String itemName;
        private int requestedQuantity;
        private double unitPrice;
        private double packageVolume;
    }

    // Order Summary (for list views)
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderSummaryResponse {
        private Long id;
        private String orderNumber;
        private OrderStatus status;
        private LocalDateTime submittedDate;
        private LocalDateTime createdAt;
        private LocalDate deadlineDate;
        private int totalItems;
    }

    // Order Detail (full view)
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderDetailResponse {
        private Long id;
        private String orderNumber;
        private String clientUsername;
        private OrderStatus status;
        private LocalDateTime submittedDate;
        private LocalDateTime createdAt;
        private LocalDate deadlineDate;
        private String declineReason;
        private List<OrderItemResponse> items;
        private DeliveryResponse delivery;
    }

    // Delivery
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DeliveryResponse {
        private Long id;
        private LocalDate deliveryDate;
        private List<TruckResponse> trucks;
    }
}
