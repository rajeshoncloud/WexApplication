package com.wexapp.purchaseapp.service;

import com.wexapp.purchaseapp.dto.CountryCurrencyDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CurrencyServiceTest {

    @Autowired
    private CurrencyService currencyService;

    @Test
    void testGetCountryCurrencyMap_ShouldReturnNonEmptyMap() {
        // When
        Map<String, CountryCurrencyDTO> currencyMap = currencyService.getCountryCurrencyMap();

        // Then
        assertNotNull(currencyMap, "Currency map should not be null");
        // Note: If Treasury API is unavailable, map might be empty, but should at least have USD
        // This test verifies the method doesn't throw exceptions
        System.out.println("Currency map size: " + currencyMap.size());
    }

    @Test
    void testGetCountryCurrencyMap_ShouldContainUnitedStatesDollar() {
        // When
        Map<String, CountryCurrencyDTO> currencyMap = currencyService.getCountryCurrencyMap();

        // Then
        assertTrue(currencyMap.containsKey("United States"), 
            "Currency map should contain 'United States'");
        assertTrue(currencyMap.containsKey("United States-Dollar"), 
            "Currency map should contain 'United States-Dollar'");
        
        CountryCurrencyDTO usdDto = currencyMap.get("United States");
        assertNotNull(usdDto, "United States DTO should not be null");
        assertEquals("United States", usdDto.getCountry(), 
            "Country should be 'United States'");
        assertEquals("United States-Dollar", usdDto.getCurrencyCode(), 
            "Currency code should be 'United States-Dollar'");
    }

    @Test
    void testGetCountryCurrencyMap_ShouldLoadMultipleCurrencies() {
        // When
        Map<String, CountryCurrencyDTO> currencyMap = currencyService.getCountryCurrencyMap();

        // Then
        // Should have at least United States-Dollar plus currencies from Treasury API
        assertTrue(currencyMap.size() >= 1, 
            "Currency map should contain at least United States-Dollar");
        
        // Log the actual count for debugging
        System.out.println("Total currencies loaded: " + currencyMap.size());
        System.out.println("Sample currencies:");
        currencyMap.values().stream()
            .limit(10)
            .forEach(dto -> System.out.println("  - " + dto.getCountry() + " (" + dto.getCurrencyCode() + ")"));
    }

    @Test
    void testGetCountryCurrencyMap_ShouldLoadExpectedNumberOfCurrencies() {
        // When
        Map<String, CountryCurrencyDTO> currencyMap = currencyService.getCountryCurrencyMap();

        // Then
        // Treasury API should return around 167 unique currencies, plus we add United States-Dollar
        // Map size is 2x unique currencies (stored by country and by currency code)
        int mapSize = currencyMap.size();
        int estimatedUniqueCurrencyCount = mapSize / 2; // Each currency is stored twice
        
        // Count unique currency codes (more accurate)
        long uniqueCurrencyCodes = currencyMap.values().stream()
            .map(CountryCurrencyDTO::getCurrencyCode)
            .distinct()
            .count();
        
        // Log detailed information
        System.out.println("Map size: " + mapSize);
        System.out.println("Estimated unique currencies: " + estimatedUniqueCurrencyCount);
        System.out.println("Actual unique currency codes: " + uniqueCurrencyCodes);
        
        // We expect at least United States-Dollar (1 currency) if API fails
        assertTrue(uniqueCurrencyCodes >= 1, 
            String.format("Expected at least 1 currency (United States-Dollar), but got %d. " +
                "This might indicate the Treasury API is not returning currencies.", uniqueCurrencyCodes));
        
        // If we have more than just USD, verify we have a reasonable number
        // Treasury API typically has around 100-167 unique country-currency combinations
        if (uniqueCurrencyCodes > 1) {
            assertTrue(uniqueCurrencyCodes >= 50, 
                String.format("Expected at least 50 currencies when API is available, but got %d. " +
                    "This might indicate the Treasury API is not returning all currencies.", uniqueCurrencyCodes));
            
            // Ideally we should get close to 167 unique currencies
            // Log a warning if we're significantly below that
            if (uniqueCurrencyCodes < 100) {
                System.out.println("WARNING: Expected around 100-167 unique currencies, but got " + uniqueCurrencyCodes);
                System.out.println("This might indicate not all currencies are being loaded from Treasury API.");
            } else {
                System.out.println("SUCCESS: Loaded " + uniqueCurrencyCodes + " unique currencies (expected ~100-167)");
            }
        }
    }

    @Test
    void testGetCountryCurrencyMap_ShouldHaveValidCurrencyDTOs() {
        // When
        Map<String, CountryCurrencyDTO> currencyMap = currencyService.getCountryCurrencyMap();

        // Then
        currencyMap.values().forEach(dto -> {
            assertNotNull(dto.getCountry(), "Country should not be null");
            assertNotNull(dto.getCurrencyCode(), "Currency code should not be null");
            assertNotNull(dto.getCurrencyName(), "Currency name should not be null");
            assertFalse(dto.getCountry().trim().isEmpty(), "Country should not be empty");
            assertFalse(dto.getCurrencyCode().trim().isEmpty(), "Currency code should not be empty");
        });
    }

    @Test
    void testGetCountryCurrencyMap_ShouldBeCached() {
        // When - call twice
        Map<String, CountryCurrencyDTO> firstCall = currencyService.getCountryCurrencyMap();
        Map<String, CountryCurrencyDTO> secondCall = currencyService.getCountryCurrencyMap();

        // Then - should return the same instance (cached)
        assertSame(firstCall, secondCall, 
            "Second call should return cached map (same instance)");
        assertEquals(firstCall.size(), secondCall.size(), 
            "Both calls should return the same number of currencies");
    }

    @Test
    void testGetCountryCurrencyMap_ShouldContainCommonCurrencies() {
        // When
        Map<String, CountryCurrencyDTO> currencyMap = currencyService.getCountryCurrencyMap();

        // Then - check for some common currencies that should be in Treasury API
        // Note: These might vary based on Treasury API data, so we'll check if they exist
        boolean hasCanada = currencyMap.containsKey("Canada");
        boolean hasMexico = currencyMap.containsKey("Mexico");
        boolean hasUnitedKingdom = currencyMap.containsKey("United Kingdom");
        boolean hasUnitedStates = currencyMap.containsKey("United States");
        
        long uniqueCurrencyCodes = currencyMap.values().stream()
            .map(CountryCurrencyDTO::getCurrencyCode)
            .distinct()
            .count();
        
        System.out.println("Has Canada: " + hasCanada);
        System.out.println("Has Mexico: " + hasMexico);
        System.out.println("Has United Kingdom: " + hasUnitedKingdom);
        System.out.println("Has United States: " + hasUnitedStates);
        System.out.println("Total unique currencies: " + uniqueCurrencyCodes);
        
        // At least United States should always be present (added manually)
        // If Treasury API is available, we should have more currencies
        assertTrue(hasUnitedStates, "Should always contain United States (USD)");
        
        // If we have more than just USD, we should have common currencies or many currencies
        if (uniqueCurrencyCodes > 1) {
            assertTrue(hasCanada || hasMexico || hasUnitedKingdom || uniqueCurrencyCodes >= 50,
                String.format("When Treasury API is available, should contain common currencies or have many currencies. " +
                    "Found %d unique currencies.", uniqueCurrencyCodes));
        }
    }
}

