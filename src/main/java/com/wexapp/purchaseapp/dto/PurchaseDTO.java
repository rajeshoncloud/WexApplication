package com.wexapp.purchaseapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Purchase data transfer object")
public class PurchaseDTO {
    @Schema(description = "Purchase UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
    @Schema(description = "Purchase date", example = "2025-01-20", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate date;
    
    @NotBlank(message = "Description is required")
    @Size(max = 50, message = "Description must be less than 51 characters")
    @Schema(description = "Purchase description (max 50 characters)", example = "Laptop Computer", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 50)
    private String description;
    
    @Schema(description = "Purchase amount in USD", example = "1299.99", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal purchaseAmount;
    
    @Schema(description = "Country name", example = "United States", requiredMode = Schema.RequiredMode.REQUIRED)
    private String country;
    
    @Schema(description = "Currency code in country_currency_desc format", example = "United States-Dollar")
    private String currencyCode;
}

