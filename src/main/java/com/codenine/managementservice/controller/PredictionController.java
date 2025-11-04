package com.codenine.managementservice.controller;

import com.codenine.managementservice.dto.prediction.PredictionResponseDTO;
import com.codenine.managementservice.service.PredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/predictions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Predictions", description = "Endpoints para gerenciar previsões de IA")
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping("/generate-all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Gera previsões para todos os itens", 
               description = "Chama a API de IA e salva previsões de todos os itens no banco de dados")
    public ResponseEntity<List<PredictionResponseDTO>> generateAllPredictions() {
        List<PredictionResponseDTO> predictions = predictionService.generateAndSaveAllPredictions();
        return ResponseEntity.ok(predictions);
    }

    @PostMapping("/generate/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(summary = "Gera previsão para um item específico",
               description = "Chama a API de IA e salva a previsão do item no banco")
    public ResponseEntity<PredictionResponseDTO> generatePrediction(
            @PathVariable Long itemId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        
        LocalDate predictionMonth = month != null ? month : YearMonth.now().plusMonths(1).atDay(1);
        
        PredictionResponseDTO prediction = predictionService
                .generateAndSavePrediction(itemId, predictionMonth)
                .orElseThrow(() -> new RuntimeException("Erro ao gerar previsão"));
        
        return ResponseEntity.ok(prediction);
    }

    @GetMapping("/item/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(summary = "Busca previsão de um item",
               description = "Retorna a previsão do banco, ou gera uma nova se não existir")
    public ResponseEntity<PredictionResponseDTO> getPrediction(
            @PathVariable Long itemId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        
        LocalDate predictionMonth = month != null ? month : YearMonth.now().plusMonths(1).atDay(1);
        
        PredictionResponseDTO prediction = predictionService.getPrediction(itemId, predictionMonth);
        return ResponseEntity.ok(prediction);
    }

    @GetMapping("/item/{itemId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(summary = "Lista histórico de previsões de um item",
               description = "Retorna todas as previsões salvas para um item específico")
    public ResponseEntity<List<PredictionResponseDTO>> getItemPredictions(@PathVariable Long itemId) {
        List<PredictionResponseDTO> predictions = predictionService.getItemPredictions(itemId);
        return ResponseEntity.ok(predictions);
    }

    @GetMapping("/month/{month}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Lista previsões de um mês",
               description = "Retorna todas as previsões de todos os itens para um mês específico")
    public ResponseEntity<List<PredictionResponseDTO>> getPredictionsByMonth(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        
        List<PredictionResponseDTO> predictions = predictionService.getPredictionsByMonth(month);
        return ResponseEntity.ok(predictions);
    }
}
