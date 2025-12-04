package com.wexapp.purchaseapp.controller;

import com.wexapp.purchaseapp.dto.CountryCurrencyDTO;
import com.wexapp.purchaseapp.dto.PurchaseDTO;
import com.wexapp.purchaseapp.dto.PurchaseWithConversionDTO;
import com.wexapp.purchaseapp.exception.ExchangeRateNotFoundException;
import com.wexapp.purchaseapp.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchases")
@CrossOrigin(origins = "*")
@Tag(name = "Purchase Controller", description = "APIs for managing purchases and currency conversion")
@SecurityRequirement(name = "ApiKeyAuth")
public class PurchaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(PurchaseController.class);

    @Autowired
    private PurchaseService purchaseService;

    @PostMapping
    @Operation(
            summary = "Create a new purchase",
            description = "Creates a new purchase record. The purchase amount should be in USD. " +
                    "The currency code will be automatically set based on the country."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Purchase created successfully",
                    content = @Content(schema = @Schema(implementation = PurchaseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing API key")
    })
    public ResponseEntity<PurchaseDTO> createPurchase(
            @Parameter(description = "Purchase details", required = true)
            @Valid @RequestBody PurchaseDTO purchaseDTO) {
        PurchaseDTO created = purchaseService.createPurchase(purchaseDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "Get all purchases",
            description = "Retrieves a list of all purchase records"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved purchases",
                    content = @Content(schema = @Schema(implementation = PurchaseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing API key")
    })
    public ResponseEntity<List<PurchaseDTO>> getAllPurchases() {
        List<PurchaseDTO> purchases = purchaseService.getAllPurchases();
        return ResponseEntity.ok(purchases);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get purchase by ID",
            description = "Retrieves a specific purchase by its UUID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Purchase found",
                    content = @Content(schema = @Schema(implementation = PurchaseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Purchase not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing API key")
    })
    public ResponseEntity<PurchaseDTO> getPurchaseById(
            @Parameter(description = "Purchase UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id) {
        return purchaseService.getPurchaseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/converted")
    @Operation(
            summary = "Get purchases with currency conversion",
            description = "Retrieves all purchases with amounts converted to the specified currency. " +
                    "Exchange rates are fetched from the U.S. Treasury API based on the purchase date " +
                    "(within 6 months). If no exchange rate is found, convertedAmount and exchangeRate will be null."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved converted purchases",
                    content = @Content(schema = @Schema(implementation = PurchaseWithConversionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Exchange rate not found for the specified currency/date",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing API key")
    })
    public ResponseEntity<List<PurchaseWithConversionDTO>> getPurchasesWithConversion(
            @Parameter(description = "Target currency in country_currency_desc format (e.g., 'Canada-Dollar', 'UK-Pound')",
                    example = "Canada-Dollar")
            @RequestParam(defaultValue = "United States-Dollar") String currency) {
        logger.debug("Controller received currency parameter: {}", currency);
        List<PurchaseWithConversionDTO> purchases = purchaseService.getPurchasesWithConversion(currency);
        logger.debug("Returning {} purchases", purchases.size());
        return ResponseEntity.ok(purchases);
    }

    @GetMapping("/countries")
    @Operation(
            summary = "Get available countries and currencies",
            description = "Retrieves the list of all available countries and their currencies. " +
                    "Includes 20 popular currencies (always available) plus additional currencies " +
                    "fetched from the U.S. Treasury API."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved countries and currencies",
                    content = @Content(schema = @Schema(implementation = CountryCurrencyDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing API key")
    })
    public ResponseEntity<List<CountryCurrencyDTO>> getAvailableCountries() {
        List<CountryCurrencyDTO> countries = purchaseService.getAvailableCountries();
        return ResponseEntity.ok(countries);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a purchase",
            description = "Deletes a purchase by its UUID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Purchase deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Purchase not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing API key")
    })
    public ResponseEntity<Void> deletePurchase(
            @Parameter(description = "Purchase UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id) {
        boolean deleted = purchaseService.deletePurchase(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(ExchangeRateNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleExchangeRateNotFoundException(ExchangeRateNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}

