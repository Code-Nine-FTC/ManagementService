package com.codenine.managementservice.dto.analytics;

import java.util.List;

public record SectionSeriesData(Long secaoId, String nome, List<Long> data) {}
