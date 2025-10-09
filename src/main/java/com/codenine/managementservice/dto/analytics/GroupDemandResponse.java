package com.codenine.managementservice.dto.analytics;

public record GroupDemandResponse(
    Long grupoId,
    String grupoNome,
    Long pedidos,
    Long quantidade) {}
