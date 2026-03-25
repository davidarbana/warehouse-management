package com.davidarbana.warehouse.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class AuthRequest {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Login {
        @NotBlank(message = "Username is required")
        private String username;
        @NotBlank(message = "Password is required")
        private String password;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Register {
        @NotBlank(message = "Full name is required")
        private String fullName;
        @NotBlank(message = "Username is required")
        private String username;
        @NotBlank(message = "Password is required")
        private String password;
        @NotBlank(message = "Email is required")
        private String email;
        @NotBlank(message = "Role is required")
        private String role;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ChangePassword {
        @NotBlank(message = "Username is required")
        private String username;
        @NotBlank(message = "Email is required")
        private String email;
        @NotBlank(message = "Current password is required")
        private String currentPassword;
        @NotBlank(message = "New password is required")
        private String newPassword;
    }
}
