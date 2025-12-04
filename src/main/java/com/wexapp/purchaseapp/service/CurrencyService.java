package com.wexapp.purchaseapp.service;

import com.wexapp.purchaseapp.dto.CountryCurrencyDTO;
import com.wexapp.purchaseapp.exception.ExchangeRateNotFoundException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CurrencyService {
    
    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);

    @Value("${currency.api.url}")
    private String currencyApiUrl;
    
    private final WebClient webClient;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Cache for country currency map (fetched from Treasury API)
    private Map<String, CountryCurrencyDTO> countryCurrencyMapCache = null;
    private final Object cacheLock = new Object();

    public CurrencyService() {
        // Don't set baseUrl - we'll construct full URLs to avoid URI parsing issues
        this.webClient = WebClient.builder().build();
    }

    /**
     * Get list of 20 popular currencies that should always be available
     * These are added first before fetching from Treasury API
     * Format matches Treasury API country_currency_desc format (e.g., "Canada-Dollar", "UK-Pound")
     */
    private List<CountryCurrencyDTO> getPopularCurrencies() {
        List<CountryCurrencyDTO> popularCurrencies = new ArrayList<>();
        
        // Popular currencies in country_currency_desc format (as returned by Treasury API)
        // 1. North America
        popularCurrencies.add(createCurrencyDTO("United States", "United States-Dollar"));
        popularCurrencies.add(createCurrencyDTO("Canada", "Canada-Dollar"));
        popularCurrencies.add(createCurrencyDTO("Mexico", "Mexico-Peso"));
        
        // 2. Europe (Eurozone and UK)
        popularCurrencies.add(createCurrencyDTO("United Kingdom", "United Kingdom-Pound"));
        popularCurrencies.add(createCurrencyDTO("Germany", "Euro Zone-Euro"));
        popularCurrencies.add(createCurrencyDTO("France", "Euro Zone-Euro"));
        popularCurrencies.add(createCurrencyDTO("Italy", "Euro Zone-Euro"));
        popularCurrencies.add(createCurrencyDTO("Spain", "Euro Zone-Euro"));
        popularCurrencies.add(createCurrencyDTO("Netherlands", "Euro Zone-Euro"));
        popularCurrencies.add(createCurrencyDTO("Switzerland", "Switzerland-Franc"));
        
        // 3. Asia-Pacific
        popularCurrencies.add(createCurrencyDTO("Japan", "Japan-Yen"));
        popularCurrencies.add(createCurrencyDTO("China", "China-Yuan"));
        popularCurrencies.add(createCurrencyDTO("India", "India-Rupee"));
        popularCurrencies.add(createCurrencyDTO("South Korea", "South-Korea-Won"));
        popularCurrencies.add(createCurrencyDTO("Singapore", "Singapore-Dollar"));
        popularCurrencies.add(createCurrencyDTO("Hong Kong", "Hong-Kong-Dollar"));
        popularCurrencies.add(createCurrencyDTO("Australia", "Australia-Dollar"));
        popularCurrencies.add(createCurrencyDTO("New Zealand", "New-Zealand-Dollar"));
        
        // 4. South America
        popularCurrencies.add(createCurrencyDTO("Brazil", "Brazil-Real"));
        popularCurrencies.add(createCurrencyDTO("Argentina", "Argentina-Peso"));
        popularCurrencies.add(createCurrencyDTO("Chile", "Chile-Peso"));
        popularCurrencies.add(createCurrencyDTO("Colombia", "Colombia-Peso"));
        
        // 5. Other regions
        popularCurrencies.add(createCurrencyDTO("South Africa", "South-Africa-Rand"));
        
        return popularCurrencies;
    }
    
    /**
     * Helper method to create a CountryCurrencyDTO
     */
    private CountryCurrencyDTO createCurrencyDTO(String country, String currencyDesc) {
        CountryCurrencyDTO dto = new CountryCurrencyDTO();
        dto.setCountry(country);
        dto.setCurrencyCode(currencyDesc);
        dto.setCurrencyName(currencyDesc);
        return dto;
    }

    /**
     * Fetch available countries and currencies from Treasury API
     * Uses country_currency_desc as the currency code
     * Caches the result for performance
     */
    private Map<String, CountryCurrencyDTO> fetchCountryCurrencyMapFromApi() {
        synchronized (cacheLock) {
            if (countryCurrencyMapCache != null) {
                return countryCurrencyMapCache;
            }

            // Initialize map and set outside try block so they're accessible in catch
            Map<String, CountryCurrencyDTO> map = new HashMap<>();
            Set<String> seenCurrencies = new HashSet<>();
            
            // Add popular currencies first (these are always available)
            logger.info("Adding {} popular currencies", getPopularCurrencies().size());
            for (CountryCurrencyDTO dto : getPopularCurrencies()) {
                String uniqueKey = dto.getCountry() + "|" + dto.getCurrencyCode();
                if (!seenCurrencies.contains(uniqueKey)) {
                    seenCurrencies.add(uniqueKey);
                    map.put(dto.getCountry(), dto);
                    map.put(dto.getCurrencyCode(), dto);
                }
            }
            logger.info("Added {} popular currencies to map", seenCurrencies.size());

            try {
                // Build URL to fetch all available currencies
                logger.debug("Fetching currencies from Treasury API");
                logger.debug("Base URL: {}", currencyApiUrl);
                
                // Fetch pages until we stop getting new unique currencies
                // Note: Treasury API returns many records (same country-currency with different dates)
                // We fetch pages until we get several consecutive pages with no new currencies
                int pageNumber = 1;
                int totalPages = 1;
                int consecutiveEmptyPages = 0;
                int maxConsecutiveEmptyPages = 5; // Stop after 5 consecutive pages with no new currencies
                int maxPages = 500; // Absolute maximum to avoid infinite loops
                
                while (pageNumber <= maxPages && consecutiveEmptyPages < maxConsecutiveEmptyPages) {
                    String pageUrl = UriComponentsBuilder.fromHttpUrl(currencyApiUrl)
                            .queryParam("sort", "-record_date")
                            .queryParam("format", "json")
                            .queryParam("page[number]", String.valueOf(pageNumber))
                            .queryParam("page[size]", "100")  // Treasury API default page size
                            .queryParam("fields", "country,country_currency_desc,record_date")
                            .toUriString();
                    
                    logger.debug("Fetching page {}: {}", pageNumber, pageUrl);
                    
                    TreasuryCurrencyListResponse response = webClient.get()
                            .uri(pageUrl)
                            .retrieve()
                            .bodyToMono(TreasuryCurrencyListResponse.class)
                            .block();
                    
                    if (response != null && response.getMeta() != null) {
                        totalPages = response.getMeta().getTotalPages() != null ? 
                            response.getMeta().getTotalPages() : 1;
                        if (pageNumber == 1) {
                            logger.info("Treasury API pagination: total pages: {}, total count: {}", 
                                totalPages, response.getMeta().getTotalCount());
                        }
                    }
                    
                    if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                        int beforeCount = seenCurrencies.size();
                        
                        // Process each currency entry
                        for (TreasuryCurrencyData currencyData : response.getData()) {
                            String country = currencyData.getCountry();
                            String currencyDesc = currencyData.getCountryCurrencyDesc();
                            
                            if (country != null && currencyDesc != null) {
                                // Create unique key to avoid duplicates
                                String uniqueKey = country + "|" + currencyDesc;
                                
                                if (!seenCurrencies.contains(uniqueKey)) {
                                    seenCurrencies.add(uniqueKey);
                                    
                                    CountryCurrencyDTO dto = new CountryCurrencyDTO();
                                    dto.setCountry(country);
                                    dto.setCurrencyCode(currencyDesc);
                                    dto.setCurrencyName(currencyDesc);
                                    
                                    // Store by country name
                                    map.put(country, dto);
                                    
                                    // Also store by currency description for easy lookup
                                    map.put(currencyDesc, dto);
                                }
                            }
                        }
                        
                        int afterCount = seenCurrencies.size();
                        int newCurrencies = afterCount - beforeCount;
                        
                        if (newCurrencies > 0) {
                            logger.info("Page {}: Added {} new unique currencies (total: {})", 
                                pageNumber, newCurrencies, afterCount);
                            consecutiveEmptyPages = 0; // Reset counter
                        } else {
                            consecutiveEmptyPages++;
                            if (consecutiveEmptyPages == 1) {
                                logger.info("Page {}: No new currencies found (total: {})", 
                                    pageNumber, afterCount);
                            }
                        }
                    } else {
                        logger.warn("Treasury API returned null or empty data for page {}", pageNumber);
                        consecutiveEmptyPages++;
                        if (consecutiveEmptyPages >= maxConsecutiveEmptyPages) {
                            break;
                        }
                    }
                    
                    // Check if there are more pages
                    if (pageNumber >= totalPages) {
                        break;
                    }
                    
                    pageNumber++;
                }
                
                int uniqueCurrencyCount = seenCurrencies.size();
                logger.info("Total unique currencies loaded: {} (map size: {})", uniqueCurrencyCount, map.size());
                
                if (consecutiveEmptyPages >= maxConsecutiveEmptyPages) {
                    logger.info("Stopped fetching after {} consecutive pages with no new currencies", 
                        consecutiveEmptyPages);
                }
                
                countryCurrencyMapCache = map;
                return map;
            } catch (Exception e) {
                logger.error("Error fetching currencies from Treasury API", e);
            }
            
            // Return map with at least USD (always present)
            int uniqueCurrencyCount = seenCurrencies.size();
            logger.info("Total unique currencies loaded: {} (map size: {})", uniqueCurrencyCount, map.size());
            countryCurrencyMapCache = map;
            return map;
        }
    }

    /**
     * Get country currency map - fetches from Treasury API if not cached
     * Only returns currencies available in the Treasury API
     */
    public Map<String, CountryCurrencyDTO> getCountryCurrencyMap() {
        return fetchCountryCurrencyMapFromApi();
    }

    /**
     * Get exchange rate for a currency based on purchase date
     * Uses country_currency_desc directly as the currency identifier
     * Searches for exchange rate within 6 months before purchase date
     * 
     * @param currencyCode The country_currency_desc (e.g., "Canada-Dollar", "Mexico-Peso")
     * @param purchaseDate Date of the purchase
     * @return Exchange rate (amount of target currency per 1 USD)
     * @throws ExchangeRateNotFoundException if no exchange rate found
     */
    public BigDecimal getExchangeRate(String currencyCode, LocalDate purchaseDate) {
        // USD is always 1.0
        if ("USD".equalsIgnoreCase(currencyCode) || "United States-Dollar".equalsIgnoreCase(currencyCode)) {
            return BigDecimal.ONE;
        }

        // Calculate date range: 6 months before purchase date to purchase date
        LocalDate sixMonthsBefore = purchaseDate.minusMonths(6);
        String startDate = sixMonthsBefore.format(DATE_FORMATTER);
        String endDate = purchaseDate.format(DATE_FORMATTER);

        try {
            // Build API query: filter by currency description and date range, sort descending by date
            // Format: country_currency_desc:in:(Canada-Dollar),record_date:gte:2025-01-01,record_date:lte=2025-12-03&sort=-record_date
            String filter = String.format("country_currency_desc:in:(%s),record_date:gte:%s,record_date:lte=%s", 
                currencyCode, startDate, endDate);
            
            // Log the API call for debugging
            logger.debug("Fetching exchange rate for currency: {}", currencyCode);
            logger.debug("Date range: {} to {}", startDate, endDate);
            logger.debug("Base URL: {}", currencyApiUrl);
            logger.debug("Filter: {}", filter);
            
            // Build the full URI with query parameters
            String fullUrl = UriComponentsBuilder.fromHttpUrl(currencyApiUrl)
                    .queryParam("fields", "country_currency_desc,exchange_rate,record_date")
                    .queryParam("filter", filter)
                    .queryParam("sort", "-record_date")
                    .queryParam("page[size]", "1")
                    .toUriString();
            
            logger.debug("Full URL: {}", fullUrl);
            
            TreasuryApiResponse response = webClient.get()
                    .uri(fullUrl)
                    .retrieve()
                    .bodyToMono(TreasuryApiResponse.class)
                    .block();

            logger.debug("Treasury API response: {}", response != null ? "received" : "null");
            if (response != null && response.getData() != null) {
                logger.debug("Response data size: {}", response.getData().size());
            }

            if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                TreasuryRateData rateData = response.getData().get(0);
                String exchangeRateStr = rateData.getExchangeRate();
                if (exchangeRateStr != null && !exchangeRateStr.isEmpty()) {
                    logger.debug("Exchange rate found: {}", exchangeRateStr);
                    return new BigDecimal(exchangeRateStr);
                } else {
                    logger.debug("Exchange rate string is null or empty");
                }
            } else {
                logger.debug("No exchange rate data found in response");
            }
            
            // No exchange rate found
            throw new ExchangeRateNotFoundException(
                String.format("Exchange rate not found for currency %s on or before %s (within last 6 months). Purchase cannot be converted to target currency.", 
                    currencyCode, purchaseDate.format(DATE_FORMATTER))
            );
            
        } catch (WebClientResponseException e) {
            logger.error("WebClientResponseException when fetching exchange rate for currency: {}", currencyCode, e);
            logger.error("Response body: {}", e.getResponseBodyAsString());
            throw new ExchangeRateNotFoundException(
                String.format("Error fetching exchange rate for currency %s: %s. Purchase cannot be converted to target currency.", 
                    currencyCode, e.getMessage())
            );
        } catch (ExchangeRateNotFoundException e) {
            logger.warn("ExchangeRateNotFoundException for currency {}: {}", currencyCode, e.getMessage());
            throw e; // Re-throw our custom exception
        } catch (Exception e) {
            logger.error("Exception in getExchangeRate for currency: {}", currencyCode, e);
            throw new ExchangeRateNotFoundException(
                String.format("Error fetching exchange rate for currency %s: %s. Purchase cannot be converted to target currency.", 
                    currencyCode, e.getMessage())
            );
        }
    }

    /**
     * Convert USD amount to target currency based on purchase date
     * 
     * @param usdAmount Amount in USD
     * @param targetCurrency The country_currency_desc (e.g., "Canada-Dollar", "United States-Dollar")
     * @param purchaseDate Date of the purchase
     * @return Converted amount in target currency
     * @throws ExchangeRateNotFoundException if exchange rate not found
     */
    public BigDecimal convertUSDToCurrency(BigDecimal usdAmount, String targetCurrency, LocalDate purchaseDate) {
        // USD/United States-Dollar is always 1.0 (base currency)
        if (targetCurrency == null) {
            return usdAmount;
        }
        String normalizedCurrency = targetCurrency.trim();
        if ("USD".equalsIgnoreCase(normalizedCurrency) || 
            "United States-Dollar".equalsIgnoreCase(normalizedCurrency) ||
            "United States".equalsIgnoreCase(normalizedCurrency)) {
            return usdAmount;
        }

        BigDecimal rate = getExchangeRate(normalizedCurrency, purchaseDate);
        return usdAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    // Response classes for Treasury API currency list
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TreasuryCurrencyListResponse {
        @JsonProperty("data")
        private List<TreasuryCurrencyData> data;
        
        @JsonProperty("meta")
        private TreasuryMeta meta;
        
        public List<TreasuryCurrencyData> getData() {
            return data;
        }
        
        public void setData(List<TreasuryCurrencyData> data) {
            this.data = data;
        }
        
        public TreasuryMeta getMeta() {
            return meta;
        }
        
        public void setMeta(TreasuryMeta meta) {
            this.meta = meta;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TreasuryMeta {
        @JsonProperty("total-count")
        private Integer totalCount;
        
        @JsonProperty("total-pages")
        private Integer totalPages;
        
        public Integer getTotalCount() {
            return totalCount;
        }
        
        public void setTotalCount(Integer totalCount) {
            this.totalCount = totalCount;
        }
        
        public Integer getTotalPages() {
            return totalPages;
        }
        
        public void setTotalPages(Integer totalPages) {
            this.totalPages = totalPages;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TreasuryCurrencyData {
        @JsonProperty("country")
        private String country;
        
        @JsonProperty("country_currency_desc")
        private String countryCurrencyDesc;
        
        @JsonProperty("record_date")
        private String recordDate;
        
        public String getCountry() {
            return country;
        }
        
        public void setCountry(String country) {
            this.country = country;
        }
        
        public String getCountryCurrencyDesc() {
            return countryCurrencyDesc;
        }
        
        public void setCountryCurrencyDesc(String countryCurrencyDesc) {
            this.countryCurrencyDesc = countryCurrencyDesc;
        }
        
        public String getRecordDate() {
            return recordDate;
        }
        
        public void setRecordDate(String recordDate) {
            this.recordDate = recordDate;
        }
    }

    // Response classes for Treasury API exchange rates
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TreasuryApiResponse {
        @JsonProperty("data")
        private List<TreasuryRateData> data;
        
        public List<TreasuryRateData> getData() {
            return data;
        }
        
        public void setData(List<TreasuryRateData> data) {
            this.data = data;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TreasuryRateData {
        @JsonProperty("country_currency_desc")
        private String countryCurrencyDesc;
        
        @JsonProperty("exchange_rate")
        private String exchangeRate;
        
        @JsonProperty("record_date")
        private String recordDate;
        
        public String getCountryCurrencyDesc() {
            return countryCurrencyDesc;
        }
        
        public void setCountryCurrencyDesc(String countryCurrencyDesc) {
            this.countryCurrencyDesc = countryCurrencyDesc;
        }
        
        public String getExchangeRate() {
            return exchangeRate;
        }
        
        public void setExchangeRate(String exchangeRate) {
            this.exchangeRate = exchangeRate;
        }
        
        public String getRecordDate() {
            return recordDate;
        }
        
        public void setRecordDate(String recordDate) {
            this.recordDate = recordDate;
        }
    }
}
