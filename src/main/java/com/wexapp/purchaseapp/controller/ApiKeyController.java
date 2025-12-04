package com.wexapp.purchaseapp.controller;

import com.wexapp.purchaseapp.dto.ApiKeyDTO;
import com.wexapp.purchaseapp.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/apikeys")
@CrossOrigin(origins = "*")
@Tag(name = "API Key Controller", description = "APIs for managing API keys. These endpoints do not require authentication.")
public class ApiKeyController {

    @Autowired
    private ApiKeyService apiKeyService;

    @PostMapping
    @Operation(
            summary = "Create a new API key",
            description = "Creates a new API key. The API key value is automatically generated in the format 'wk_<uuid>'. " +
                    "No authentication is required for this endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "API key created successfully",
                    content = @Content(schema = @Schema(implementation = ApiKeyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<ApiKeyDTO> createApiKey(
            @Parameter(description = "API key details (name and expiration date)", required = true)
            @Valid @RequestBody ApiKeyDTO apiKeyDTO) {
        ApiKeyDTO created = apiKeyService.createApiKey(apiKeyDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "Get all API keys",
            description = "Retrieves a list of all API keys. No authentication is required for this endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved API keys",
                    content = @Content(schema = @Schema(implementation = ApiKeyDTO.class)))
    })
    public ResponseEntity<List<ApiKeyDTO>> getAllApiKeys() {
        List<ApiKeyDTO> apiKeys = apiKeyService.getAllApiKeys();
        return ResponseEntity.ok(apiKeys);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get API key by ID",
            description = "Retrieves a specific API key by its ID. No authentication is required for this endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API key found",
                    content = @Content(schema = @Schema(implementation = ApiKeyDTO.class))),
            @ApiResponse(responseCode = "404", description = "API key not found")
    })
    public ResponseEntity<ApiKeyDTO> getApiKeyById(
            @Parameter(description = "API key ID", required = true, example = "1")
            @PathVariable Long id) {
        return apiKeyService.getApiKeyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an API key",
            description = "Deletes an API key by its ID. No authentication is required for this endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "API key deleted successfully"),
            @ApiResponse(responseCode = "404", description = "API key not found")
    })
    public ResponseEntity<Void> deleteApiKey(
            @Parameter(description = "API key ID", required = true, example = "1")
            @PathVariable Long id) {
        apiKeyService.deleteApiKey(id);
        return ResponseEntity.noContent().build();
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
}

