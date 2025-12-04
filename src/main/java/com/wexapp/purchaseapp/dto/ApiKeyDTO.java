package com.wexapp.purchaseapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API key data transfer object")
public class ApiKeyDTO {
    @Schema(description = "API key ID", example = "1")
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be less than 101 characters")
    @Schema(description = "API key name/description (max 100 characters)", example = "Production API Key", 
            requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
    private String name;

    @Schema(description = "API key value (format: wk_<uuid>). Auto-generated on creation.", 
            example = "wk_3c1f0f65a19444879772ff82833f5347")
    private String apiKey;

    @NotNull(message = "Expiration date is required")
    @Schema(description = "API key expiration date", example = "2026-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate expirationDate;
}

