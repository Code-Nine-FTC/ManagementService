package com.codenine.managementservice.dto.analytics;

import java.util.List;

public record GroupDemandSeriesResponse(List<String> categories, List<GroupSeriesData> series) {}
