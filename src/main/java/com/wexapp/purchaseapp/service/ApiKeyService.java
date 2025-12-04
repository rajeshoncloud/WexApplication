package com.wexapp.purchaseapp.service;

import com.wexapp.purchaseapp.dto.ApiKeyDTO;
import com.wexapp.purchaseapp.entity.ApiKey;
import com.wexapp.purchaseapp.repository.ApiKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApiKeyService {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Transactional
    public ApiKeyDTO createApiKey(ApiKeyDTO apiKeyDTO) {
        // Generate a unique API key
        String generatedKey = "wk_" + UUID.randomUUID().toString().replace("-", "");

        ApiKey apiKey = new ApiKey();
        apiKey.setName(apiKeyDTO.getName());
        apiKey.setApiKey(generatedKey);
        apiKey.setExpirationDate(apiKeyDTO.getExpirationDate());

        ApiKey saved = apiKeyRepository.save(apiKey);
        return convertToDTO(saved);
    }

    public boolean isValidApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return false;
        }

        Optional<ApiKey> key = apiKeyRepository.findByApiKey(apiKey);
        if (key.isEmpty()) {
            return false;
        }

        ApiKey foundKey = key.get();
        // Check if the key is expired
        return foundKey.getExpirationDate().isAfter(LocalDate.now()) || 
               foundKey.getExpirationDate().isEqual(LocalDate.now());
    }

    public List<ApiKeyDTO> getAllApiKeys() {
        return apiKeyRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<ApiKeyDTO> getApiKeyById(Long id) {
        return apiKeyRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional
    public void deleteApiKey(Long id) {
        apiKeyRepository.deleteById(id);
    }

    private ApiKeyDTO convertToDTO(ApiKey apiKey) {
        ApiKeyDTO dto = new ApiKeyDTO();
        dto.setId(apiKey.getId());
        dto.setName(apiKey.getName());
        dto.setApiKey(apiKey.getApiKey());
        dto.setExpirationDate(apiKey.getExpirationDate());
        return dto;
    }
}

