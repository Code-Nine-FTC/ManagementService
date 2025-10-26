package com.codenine.managementservice.dto.order;

import java.time.LocalDate;
import java.util.Map;

// DTO usado tanto para criação quanto para atualização de pedidos.
// Campos:
// - orderNumber: obrigatório na criação (manual), ignorado na atualização se enviado
// - itemQuantities: mapa itemId -> quantidade; opcional na atualização
// - sectionId: (compat) opcional; se informado na criação, define a seção do pedido (consumidora)
// - consumerSectionId: preferencial; id da seção consumidora do pedido
// - withdrawDay: opcional; no formato yyyy-MM-dd para atualização/definição
public record OrderRequest(
    String orderNumber,
    Map<String, Integer> itemQuantities,
    Long sectionId,
    Long consumerSectionId,
    LocalDate withdrawDay) {}
