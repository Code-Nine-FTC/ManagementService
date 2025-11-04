package com.codenine.managementservice.service;

import com.codenine.managementservice.dto.prediction.PredictionApiResponseDTO;
import com.codenine.managementservice.dto.prediction.PredictionResponseDTO;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.Prediction;
import com.codenine.managementservice.repository.ItemRepository;
import com.codenine.managementservice.repository.PredictionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final ItemRepository itemRepository;
    private final RestTemplate restTemplate;

    @Value("${prediction.api.url:http://python-api:8000}")
    private String predictionApiUrl;

    /**
     * Gera e salva previsões para todos os itens ativos
     */
    @Transactional
    public List<PredictionResponseDTO> generateAndSaveAllPredictions() {
        
        List<Item> allItems = itemRepository.findAll();
        List<PredictionResponseDTO> savedPredictions = new ArrayList<>();
        
        LocalDate nextMonth = YearMonth.now().plusMonths(1).atDay(1);
        
        for (Item item : allItems) {
            try {
                Optional<PredictionResponseDTO> prediction = generateAndSavePrediction(item.getId(), nextMonth);
                prediction.ifPresent(savedPredictions::add);
            } catch (Exception e) {
                log.error("❌ Erro ao gerar previsão para item {}: {}", item.getId(), e.getMessage());
            }
        }
        
        return savedPredictions;
    }

    /**
     * Gera e salva previsão para um item específico
     */
    @Transactional
    public Optional<PredictionResponseDTO> generateAndSavePrediction(Long itemId, LocalDate predictionMonth) {
        
        Optional<Prediction> existingPrediction = predictionRepository
                .findByItemIdAndPredictionMonth(itemId, predictionMonth);
        
        if (existingPrediction.isPresent()) {
            return Optional.of(convertToDTO(existingPrediction.get()));
        }
        
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado: " + itemId));
        
        PredictionApiResponseDTO apiResponse = callPredictionApi(itemId);
        
        Prediction prediction = new Prediction();
        prediction.setItem(item);
        prediction.setPredictionMonth(predictionMonth);
        prediction.setPredictedQuantity(apiResponse.getPredictedQuantity());
        prediction.setConfidenceScore(apiResponse.getConfidenceScore());
        prediction.setModelVersion(apiResponse.getModelUsed());
        
        Prediction saved = predictionRepository.save(prediction);
        
        return Optional.of(convertToDTO(saved));
    }

    /**
     * Busca previsão do banco ou gera nova se não existir
     */
    @Transactional
    public PredictionResponseDTO getPrediction(Long itemId, LocalDate predictionMonth) {
        
        Optional<Prediction> existing = predictionRepository
                .findByItemIdAndPredictionMonth(itemId, predictionMonth);
        
        if (existing.isPresent()) {
            return convertToDTO(existing.get());
        }
        
        return generateAndSavePrediction(itemId, predictionMonth)
                .orElseThrow(() -> new RuntimeException("Erro ao gerar previsão"));
    }

    /**
     * Lista todas as previsões de um item
     */
    public List<PredictionResponseDTO> getItemPredictions(Long itemId) {
        return predictionRepository.findByItemIdOrderByPredictionMonthDesc(itemId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista todas as previsões de um mês
     */
    public List<PredictionResponseDTO> getPredictionsByMonth(LocalDate month) {
        return predictionRepository.findByPredictionMonth(month)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Chama a API Python para gerar previsão
     */
    private PredictionApiResponseDTO callPredictionApi(Long itemId) {
        String url = predictionApiUrl + "/predictions/item/" + itemId;
        
        try {
            ResponseEntity<PredictionApiResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<PredictionApiResponseDTO>() {}
            );
            
            if (response.getBody() == null) {
                throw new RuntimeException("API retornou resposta vazia");
            }
            
            return response.getBody();
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao comunicar com API de previsão: " + e.getMessage());
        }
    }

    /**
     * Converte entidade para DTO
     */
    private PredictionResponseDTO convertToDTO(Prediction prediction) {
        PredictionResponseDTO dto = new PredictionResponseDTO();
        dto.setId(prediction.getId());
        dto.setItemId(prediction.getItem().getId());
        dto.setItemName(prediction.getItem().getName());
        dto.setPredictionMonth(prediction.getPredictionMonth());
        dto.setPredictedQuantity(prediction.getPredictedQuantity());
        dto.setConfidenceScore(prediction.getConfidenceScore());
        dto.setModelVersion(prediction.getModelVersion());
        dto.setCreatedAt(prediction.getCreatedAt());
        dto.setUpdatedAt(prediction.getUpdatedAt());
        return dto;
    }
}
