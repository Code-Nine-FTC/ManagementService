package com.codenine.managementservice.dto.analytics;

import java.util.List;

public record SectionDemandSeriesResponse(
    List<String> categories, List<SectionSeriesData> series) {}
