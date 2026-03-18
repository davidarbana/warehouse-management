package com.davidarbana.warehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class TruckRequest {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Create {
        @NotBlank(message = "Chassis number is required")
        private String chassisNumber;
        @NotBlank(message = "License plate is required")
        private String licensePlate;
        @Min(value = 1, message = "Container volume must be positive")
        private double containerVolume;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Update {
        private String chassisNumber;
        private String licensePlate;
        private Double containerVolume;
    }
}
