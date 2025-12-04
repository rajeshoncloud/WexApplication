package com.wexapp.purchaseapp.service;

import com.wexapp.purchaseapp.dto.CountryCurrencyDTO;
import com.wexapp.purchaseapp.dto.PurchaseDTO;
import com.wexapp.purchaseapp.dto.PurchaseWithConversionDTO;
import com.wexapp.purchaseapp.entity.Purchase;
import com.wexapp.purchaseapp.exception.ExchangeRateNotFoundException;
import com.wexapp.purchaseapp.repository.PurchaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PurchaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private CurrencyService currencyService;

    @Transactional
    public PurchaseDTO createPurchase(PurchaseDTO purchaseDTO) {
        // Get currency code (country_currency_desc) for the country from Treasury API
        Map<String, CountryCurrencyDTO> countryMap = currencyService.getCountryCurrencyMap();
        CountryCurrencyDTO countryCurrency = countryMap.get(purchaseDTO.getCountry());
        
        if (countryCurrency == null) {
            // If country not found in Treasury API, throw exception
            throw new IllegalArgumentException("Country '" + purchaseDTO.getCountry() + "' is not supported. Please select a country from the available list.");
        }
        
        // Use country_currency_desc as currency code
        purchaseDTO.setCurrencyCode(countryCurrency.getCurrencyCode());

        Purchase purchase = new Purchase();
        purchase.setDate(purchaseDTO.getDate());
        purchase.setDescription(purchaseDTO.getDescription());
        purchase.setPurchaseAmount(purchaseDTO.getPurchaseAmount());
        purchase.setCountry(purchaseDTO.getCountry());
        purchase.setCurrencyCode(purchaseDTO.getCurrencyCode());

        Purchase saved = purchaseRepository.save(purchase);
        return convertToDTO(saved);
    }

    public List<PurchaseDTO> getAllPurchases() {
        return purchaseRepository.findAllByOrderByDateDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<PurchaseDTO> getPurchaseById(String id) {
        return purchaseRepository.findById(id)
                .map(this::convertToDTO);
    }

    public List<PurchaseWithConversionDTO> getPurchasesWithConversion(String targetCurrency) {
        logger.debug("getPurchasesWithConversion called with currency: {}", targetCurrency);
        List<Purchase> purchases = purchaseRepository.findAllByOrderByDateDesc();
        logger.debug("Found {} purchases", purchases.size());
        
        return purchases.stream()
                .map(purchase -> {
                    PurchaseWithConversionDTO dto = new PurchaseWithConversionDTO();
                    dto.setId(purchase.getId());
                    dto.setDate(purchase.getDate());
                    dto.setDescription(purchase.getDescription());
                    dto.setPurchaseAmount(purchase.getPurchaseAmount());
                    dto.setCountry(purchase.getCountry());
                    dto.setCurrencyCode(purchase.getCurrencyCode());
                    
                    try {
                        logger.debug("Converting purchase {} from USD to {} for date {}", 
                            purchase.getId(), targetCurrency, purchase.getDate());
                        // Convert USD to target currency using purchase date
                        BigDecimal convertedAmount = currencyService.convertUSDToCurrency(
                                purchase.getPurchaseAmount(), 
                                targetCurrency,
                                purchase.getDate()  // Pass purchase date
                        );
                        dto.setConvertedAmount(convertedAmount);
                        logger.debug("Converted amount: {}", convertedAmount);
                        
                        // Get exchange rate based on purchase date
                        BigDecimal rate = currencyService.getExchangeRate(targetCurrency, purchase.getDate());
                        dto.setExchangeRate(rate);
                        logger.debug("Exchange rate: {}", rate);
                    } catch (ExchangeRateNotFoundException e) {
                        logger.warn("ExchangeRateNotFoundException for purchase {}: {}", 
                            purchase.getId(), e.getMessage());
                        // Set null values to indicate conversion failed
                        dto.setConvertedAmount(null);
                        dto.setExchangeRate(null);
                        // Note: You may want to add an errorMessage field to PurchaseWithConversionDTO
                    } catch (Exception e) {
                        logger.error("Unexpected exception for purchase {}: {}", 
                            purchase.getId(), e.getClass().getName(), e);
                        dto.setConvertedAmount(null);
                        dto.setExchangeRate(null);
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<CountryCurrencyDTO> getAvailableCountries() {
        return currencyService.getCountryCurrencyMap().values().stream()
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean deletePurchase(String id) {
        if (purchaseRepository.existsById(id)) {
            purchaseRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private PurchaseDTO convertToDTO(Purchase purchase) {
        PurchaseDTO dto = new PurchaseDTO();
        dto.setId(purchase.getId());
        dto.setDate(purchase.getDate());
        dto.setDescription(purchase.getDescription());
        dto.setPurchaseAmount(purchase.getPurchaseAmount());
        dto.setCountry(purchase.getCountry());
        dto.setCurrencyCode(purchase.getCurrencyCode());
        return dto;
    }
}

