package com.davidarbana.warehouse.exception;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String message;
}
