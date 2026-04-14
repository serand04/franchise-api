package com.accenture.franchiseapi.api.handler;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class FranchiseDtos {

    private FranchiseDtos() {}

    // ── Requests ─────────────────────────────────────────────────────────────

    @Data
    public static class CreateFranchiseRequest {
        @NotBlank(message = "name is required")
        private String name;
    }

    @Data
    public static class UpdateNameRequest {
        @NotBlank(message = "name is required")
        private String name;
    }

    @Data
    public static class AddBranchRequest {
        @NotBlank(message = "name is required")
        private String name;
    }

    @Data
    public static class AddProductRequest {
        @NotBlank(message = "name is required")
        private String name;

        @NotNull(message = "stock is required")
        @Min(value = 0, message = "stock must be >= 0")
        private Integer stock;
    }

    @Data
    public static class UpdateStockRequest {
        @NotNull(message = "stock is required")
        @Min(value = 0, message = "stock must be >= 0")
        private Integer stock;
    }
}
