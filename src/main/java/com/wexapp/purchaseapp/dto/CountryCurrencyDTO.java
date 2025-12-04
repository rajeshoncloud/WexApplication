package com.wexapp.purchaseapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Country and currency information")
public class CountryCurrencyDTO {
    @Schema(description = "Country name", example = "United States")
    private String country;
    
    @Schema(description = "Currency code in country_currency_desc format", example = "United States-Dollar")
    private String currencyCode;
    
    @Schema(description = "Currency name", example = "United States-Dollar")
    private String currencyName;
}

