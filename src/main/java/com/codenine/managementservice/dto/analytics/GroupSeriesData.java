package com.codenine.managementservice.dto.analytics;

import java.util.List;

public record GroupSeriesData(
    Long grupoId,
    String nome,
    List<Long> data) {}
