package com.wexapp.purchaseapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Purchase with currency conversion data transfer object")
public class PurchaseWithConversionDTO {
    @Schema(description = "Purchase UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
    @Schema(description = "Purchase date", example = "2025-01-20")
    private LocalDate date;
    
    @Schema(description = "Purchase description", example = "Laptop Computer")
    private String description;
    
    @Schema(description = "Purchase amount in USD", example = "1299.99")
    private BigDecimal purchaseAmount;
    
    @Schema(description = "Country name", example = "United States")
    private String country;
    
    @Schema(description = "Currency code in country_currency_desc format", example = "United States-Dollar")
    private String currencyCode;
    
    @Schema(description = "Converted amount in target currency (null if exchange rate not found)", 
            example = "1754.99", nullable = true)
    private BigDecimal convertedAmount;
    
    @Schema(description = "Exchange rate used for conversion (null if exchange rate not found)", 
            example = "1.35", nullable = true)
    private BigDecimal exchangeRate;
}

