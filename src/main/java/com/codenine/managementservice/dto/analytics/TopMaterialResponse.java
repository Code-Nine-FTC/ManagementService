package com.codenine.managementservice.dto.analytics;

public record TopMaterialResponse(
    Long materialId,
    String nome,
    Long grupoId,
    String grupoNome,
    Long pedidos,
    Long quantidade) {}
