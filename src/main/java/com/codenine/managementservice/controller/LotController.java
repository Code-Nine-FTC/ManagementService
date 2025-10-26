package com.codenine.managementservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.codenine.managementservice.dto.item.LotAdjustRequest;
import com.codenine.managementservice.dto.item.LotCreateRequest;
import com.codenine.managementservice.dto.item.LotResponse;
import com.codenine.managementservice.service.LotService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/lotes")
public class LotController {

  private final LotService lotService;

  public LotController(LotService lotService) {
    this.lotService = lotService;
  }

  @Operation(summary = "Cria um novo lote para um item")
  @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
  @PostMapping
  public ResponseEntity<LotResponse> create(@RequestBody LotCreateRequest request) {
    return ResponseEntity.ok(lotService.create(request));
  }

  @Operation(summary = "Lista lotes de um item")
  @GetMapping
  public ResponseEntity<List<LotResponse>> listByItem(@RequestParam Long itemId) {
    return ResponseEntity.ok(lotService.listByItem(itemId));
  }

  @Operation(summary = "Ajusta a quantidade de um lote")
  @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
  @PatchMapping("/{id}/adjust")
  public ResponseEntity<LotResponse> adjust(
      @PathVariable Long id, @RequestBody LotAdjustRequest request) {
    return ResponseEntity.ok(lotService.adjust(id, request));
  }
}
