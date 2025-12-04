package com.wexapp.purchaseapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wexapp.purchaseapp.dto.PurchaseDTO;
import com.wexapp.purchaseapp.entity.ApiKey;
import com.wexapp.purchaseapp.repository.ApiKeyRepository;
import com.wexapp.purchaseapp.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String TEST_API_KEY = "test-api-key";

    @BeforeEach
    void setUp() {
        // Clear the database before each test
        purchaseRepository.deleteAll();
        apiKeyRepository.deleteAll();

        // Create a test API key that's valid for 1 year from now
        ApiKey testApiKey = new ApiKey();
        testApiKey.setName("Test API Key");
        testApiKey.setApiKey(TEST_API_KEY);
        testApiKey.setExpirationDate(LocalDate.now().plusYears(1));
        apiKeyRepository.save(testApiKey);
    }

    @Test
    void testCreatePurchase_Success() throws Exception {
        PurchaseDTO purchaseDTO = createValidPurchaseDTO();
        purchaseDTO.setDescription("Test Purchase Item");

        mockMvc.perform(post("/api/purchases")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value("Test Purchase Item"))
                .andExpect(jsonPath("$.purchaseAmount").value(100.50))
                .andExpect(jsonPath("$.country").value("United States"))
                .andExpect(jsonPath("$.date").exists());
    }

    @Test
    void testCreatePurchase_ValidationError_DescriptionTooLong() throws Exception {
        PurchaseDTO purchaseDTO = createValidPurchaseDTO();
        // Set description to 51 characters (exceeds max of 50)
        purchaseDTO.setDescription("A".repeat(51));

        mockMvc.perform(post("/api/purchases")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Description must be less than 51 characters"));
    }

    @Test
    void testCreatePurchase_ValidationError_DescriptionEmpty() throws Exception {
        PurchaseDTO purchaseDTO = createValidPurchaseDTO();
        purchaseDTO.setDescription("");

        mockMvc.perform(post("/api/purchases")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    void testCreatePurchase_ValidationError_DescriptionNull() throws Exception {
        PurchaseDTO purchaseDTO = createValidPurchaseDTO();
        purchaseDTO.setDescription(null);

        mockMvc.perform(post("/api/purchases")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    void testCreatePurchase_ValidationError_DescriptionExactly50Characters() throws Exception {
        PurchaseDTO purchaseDTO = createValidPurchaseDTO();
        // Set description to exactly 50 characters (should be valid)
        purchaseDTO.setDescription("A".repeat(50));

        mockMvc.perform(post("/api/purchases")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("A".repeat(50)));
    }

    @Test
    void testDeletePurchase_Success() throws Exception {
        // First, create a purchase
        PurchaseDTO purchaseDTO = createValidPurchaseDTO();
        String createResponse = mockMvc.perform(post("/api/purchases")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PurchaseDTO createdPurchase = objectMapper.readValue(createResponse, PurchaseDTO.class);
        String purchaseId = createdPurchase.getId();

        // Then delete it
        mockMvc.perform(delete("/api/purchases/{id}", purchaseId)
                        .header(API_KEY_HEADER, TEST_API_KEY))
                .andExpect(status().isNoContent());

        // Verify it's deleted by trying to get it
        mockMvc.perform(get("/api/purchases/{id}", purchaseId)
                        .header(API_KEY_HEADER, TEST_API_KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeletePurchase_NotFound() throws Exception {
        String nonExistentId = "non-existent-id-12345";

        mockMvc.perform(delete("/api/purchases/{id}", nonExistentId)
                        .header(API_KEY_HEADER, TEST_API_KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllPurchases_AfterCreate() throws Exception {
        // Create a purchase
        PurchaseDTO purchaseDTO = createValidPurchaseDTO();
        purchaseDTO.setDescription("First Purchase");

        mockMvc.perform(post("/api/purchases")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseDTO)))
                .andExpect(status().isCreated());

        // Get all purchases
        mockMvc.perform(get("/api/purchases")
                        .header(API_KEY_HEADER, TEST_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description").value("First Purchase"));
    }

    @Test
    void testGetAllPurchases_Empty() throws Exception {
        mockMvc.perform(get("/api/purchases")
                        .header(API_KEY_HEADER, TEST_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testCreateAndDelete_CompleteFlow() throws Exception {
        // Create purchase
        PurchaseDTO purchaseDTO = createValidPurchaseDTO();
        purchaseDTO.setDescription("Complete Flow Test");

        String createResponse = mockMvc.perform(post("/api/purchases")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PurchaseDTO createdPurchase = objectMapper.readValue(createResponse, PurchaseDTO.class);
        String purchaseId = createdPurchase.getId();

        // Verify it exists
        mockMvc.perform(get("/api/purchases/{id}", purchaseId)
                        .header(API_KEY_HEADER, TEST_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(purchaseId))
                .andExpect(jsonPath("$.description").value("Complete Flow Test"));

        // Delete it
        mockMvc.perform(delete("/api/purchases/{id}", purchaseId)
                        .header(API_KEY_HEADER, TEST_API_KEY))
                .andExpect(status().isNoContent());

        // Verify it's deleted
        mockMvc.perform(get("/api/purchases/{id}", purchaseId)
                        .header(API_KEY_HEADER, TEST_API_KEY))
                .andExpect(status().isNotFound());

        // Verify it's not in the list
        mockMvc.perform(get("/api/purchases")
                        .header(API_KEY_HEADER, TEST_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // Helper method to create a valid PurchaseDTO
    private PurchaseDTO createValidPurchaseDTO() {
        PurchaseDTO dto = new PurchaseDTO();
        dto.setDate(LocalDate.now());
        dto.setDescription("Valid Purchase Description");
        dto.setPurchaseAmount(new BigDecimal("100.50"));
        dto.setCountry("United States");
        return dto;
    }
}

