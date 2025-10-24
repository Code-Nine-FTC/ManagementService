package com.codenine.managementservice.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.codenine.managementservice.dto.analytics.GroupDemandResponse;
import com.codenine.managementservice.dto.analytics.GroupDemandSeriesResponse;
import com.codenine.managementservice.dto.analytics.TopMaterialResponse;
import com.codenine.managementservice.dto.analytics.SectionConsumptionResponse;
import com.codenine.managementservice.dto.analytics.SectionDemandSeriesResponse;
import com.codenine.managementservice.service.AnalyticsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

  @Autowired private AnalyticsService analyticsService;

  @Operation(summary = "Top materiais")
  @GetMapping("/materiais/top")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<TopMaterialResponse>> getTopMateriais(
      @Parameter(description = "Data inicial (yyyy-MM-dd)")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @Parameter(description = "Data final (yyyy-MM-dd)")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate,
      @Parameter(description = "Limite de itens retornados", example = "10")
          @RequestParam(defaultValue = "10")
          int limit,
      @Parameter(description = "Considerar apenas pedidos concluídos", example = "false")
          @RequestParam(defaultValue = "false")
          boolean onlyCompleted) {
    if (endDate.isBefore(startDate)) return ResponseEntity.badRequest().build();
    return ResponseEntity.ok(
        analyticsService.getTopMaterials(startDate, endDate, limit, onlyCompleted));
  }

  @Operation(summary = "Demanda agregada por grupo")
  @GetMapping("/grupos/demanda")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<GroupDemandResponse>> getGroupDemand(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(defaultValue = "false") boolean onlyCompleted) {
    if (endDate.isBefore(startDate)) return ResponseEntity.badRequest().build();
    return ResponseEntity.ok(analyticsService.getGroupDemand(startDate, endDate, onlyCompleted));
  }

  @Operation(summary = "Série temporal de demanda por grupo")
  @GetMapping("/grupos/demanda-series")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<GroupDemandSeriesResponse> getGroupDemandSeries(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(defaultValue = "month") String step,
      @RequestParam(defaultValue = "false") boolean onlyCompleted) {
    if (endDate.isBefore(startDate)) return ResponseEntity.badRequest().build();
    return ResponseEntity.ok(
        analyticsService.getGroupDemandSeries(startDate, endDate, step, onlyCompleted));
  }

  @Operation(summary = "Consumo por seção consumidora")
  @GetMapping("/secoes/consumo")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<SectionConsumptionResponse>> getSectionConsumption(
      @Parameter(description = "Data inicial (yyyy-MM-dd)")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @Parameter(description = "Data final (yyyy-MM-dd)")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate,
      @Parameter(description = "Considerar apenas pedidos concluídos", example = "false")
          @RequestParam(defaultValue = "false")
          boolean onlyCompleted,
      @Parameter(description = "Somente seções consumidoras", example = "true")
          @RequestParam(defaultValue = "true")
          boolean onlyConsumers,
      @Parameter(description = "Somente seções ativas", example = "true")
          @RequestParam(defaultValue = "true")
          boolean onlyActiveConsumers) {
    if (endDate.isBefore(startDate)) return ResponseEntity.badRequest().build();
    return ResponseEntity.ok(
        analyticsService.getSectionConsumption(startDate, endDate, onlyCompleted, onlyConsumers, onlyActiveConsumers));
  }

  @Operation(summary = "Série temporal de consumo por seção")
  @GetMapping("/secoes/series")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<SectionDemandSeriesResponse> getSectionDemandSeries(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(defaultValue = "month") String step,
      @RequestParam(defaultValue = "false") boolean onlyCompleted,
    @RequestParam(defaultValue = "true") boolean onlyConsumers,
    @RequestParam(defaultValue = "true") boolean onlyActiveConsumers) {
    if (endDate.isBefore(startDate)) return ResponseEntity.badRequest().build();
    return ResponseEntity.ok(
      analyticsService.getSectionDemandSeries(startDate, endDate, step, onlyCompleted, onlyConsumers, onlyActiveConsumers));
  }
}
