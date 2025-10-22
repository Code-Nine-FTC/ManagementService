package com.codenine.managementservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.codenine.managementservice.dto.analytics.GroupDemandResponse;
import com.codenine.managementservice.dto.analytics.GroupDemandSeriesResponse;
import com.codenine.managementservice.dto.analytics.GroupSeriesData;
import com.codenine.managementservice.dto.analytics.TopMaterialResponse;
import com.codenine.managementservice.dto.analytics.SectionConsumptionResponse;
import com.codenine.managementservice.dto.analytics.SectionDemandSeriesResponse;
import com.codenine.managementservice.dto.analytics.SectionSeriesData;
import com.codenine.managementservice.repository.AnalyticsRepository;
import com.codenine.managementservice.entity.SectionType;

@Service
public class AnalyticsService {

  @Autowired private AnalyticsRepository analyticsRepository;

  public List<TopMaterialResponse> getTopMaterials(
      LocalDate startDate, LocalDate endDate, int limit, boolean onlyCompleted) {
    LocalDateTime start = startDate.atStartOfDay();
    LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);
    return analyticsRepository.findTopMaterials(
        start, end, onlyCompleted, PageRequest.of(0, limit));
  }

  public List<GroupDemandResponse> getGroupDemand(
      LocalDate startDate, LocalDate endDate, boolean onlyCompleted) {
    LocalDateTime start = startDate.atStartOfDay();
    LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);
    return analyticsRepository.findGroupDemand(start, end, onlyCompleted);
  }

  public GroupDemandSeriesResponse getGroupDemandSeries(
      LocalDate startDate, LocalDate endDate, String step, boolean onlyCompleted) {
    String normalizedStep = normalizeStep(step);
    LocalDateTime start = startDate.atStartOfDay();
    LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);

    List<Object[]> rows =
        analyticsRepository.findGroupDemandSeries(start, end, onlyCompleted, normalizedStep);

    // rows: group_id, group_name, bucket(Timestamp), pedidos(BigInteger), quantidade(BigInteger)
    DateTimeFormatter fmt;
    switch (normalizedStep) {
      case "day" -> fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      case "week" -> fmt = DateTimeFormatter.ofPattern("YYYY-'W'ww");
      default -> fmt = DateTimeFormatter.ofPattern("yyyy-MM");
    }

    Set<String> categorySet = new TreeSet<>();
    Map<Long, GroupSeriesDataBuilder> seriesBuilders = new LinkedHashMap<>();

    for (Object[] r : rows) {
      Long groupId = r[0] != null ? ((Number) r[0]).longValue() : null;
      String groupName = (String) r[1];
      LocalDateTime bucket = ((java.sql.Timestamp) r[2]).toLocalDateTime();
      Long pedidos = r[3] != null ? ((Number) r[3]).longValue() : 0L;
      Long quantidade = r[4] != null ? ((Number) r[4]).longValue() : 0L;

      // We aggregate quantity (quantidade) for series (could also choose pedidos). Using
      // quantidade.
      String cat = bucket.format(fmt);
      categorySet.add(cat);
      seriesBuilders
          .computeIfAbsent(groupId, k -> new GroupSeriesDataBuilder(groupId, groupName))
          .put(cat, quantidade);
    }

    List<String> categories = new ArrayList<>(categorySet);
    List<GroupSeriesData> series = new ArrayList<>();
    for (GroupSeriesDataBuilder b : seriesBuilders.values()) {
      List<Long> data = new ArrayList<>();
      for (String c : categories) {
        data.add(b.values.getOrDefault(c, 0L));
      }
      series.add(new GroupSeriesData(b.groupId, b.name, data));
    }
    // sort series by name for determinism
    series.sort(Comparator.comparing(GroupSeriesData::nome, String.CASE_INSENSITIVE_ORDER));
    return new GroupDemandSeriesResponse(categories, series);
  }

  public List<SectionConsumptionResponse> getSectionConsumption(
      LocalDate startDate, LocalDate endDate, boolean onlyCompleted, boolean onlyConsumers) {
    LocalDateTime start = startDate.atStartOfDay();
    LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);
    SectionType type = onlyConsumers ? SectionType.CONSUMER : null;
    return analyticsRepository.findSectionConsumption(start, end, onlyCompleted, type);
  }

  public SectionDemandSeriesResponse getSectionDemandSeries(
      LocalDate startDate, LocalDate endDate, String step, boolean onlyCompleted, boolean onlyConsumers) {
    String normalizedStep = normalizeStep(step);
    LocalDateTime start = startDate.atStartOfDay();
    LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);

    List<Object[]> rows =
        analyticsRepository.findSectionDemandSeries(start, end, onlyCompleted, normalizedStep, onlyConsumers);

    DateTimeFormatter fmt;
    switch (normalizedStep) {
      case "day" -> fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      case "week" -> fmt = DateTimeFormatter.ofPattern("YYYY-'W'ww");
      default -> fmt = DateTimeFormatter.ofPattern("yyyy-MM");
    }

    Set<String> categorySet = new TreeSet<>();
    Map<Long, SectionSeriesDataBuilder> seriesBuilders = new LinkedHashMap<>();

    for (Object[] r : rows) {
      Long sectionId = r[0] != null ? ((Number) r[0]).longValue() : null;
      String sectionName = (String) r[1];
      LocalDateTime bucket = ((java.sql.Timestamp) r[2]).toLocalDateTime();
      Long pedidos = r[3] != null ? ((Number) r[3]).longValue() : 0L;
      Long quantidade = r[4] != null ? ((Number) r[4]).longValue() : 0L;

      String cat = bucket.format(fmt);
      categorySet.add(cat);
      seriesBuilders
          .computeIfAbsent(sectionId, k -> new SectionSeriesDataBuilder(sectionId, sectionName))
          .put(cat, quantidade);
    }

    List<String> categories = new ArrayList<>(categorySet);
    List<SectionSeriesData> series = new ArrayList<>();
    for (SectionSeriesDataBuilder b : seriesBuilders.values()) {
      List<Long> data = new ArrayList<>();
      for (String c : categories) {
        data.add(b.values.getOrDefault(c, 0L));
      }
      series.add(new SectionSeriesData(b.sectionId, b.name, data));
    }
    series.sort(Comparator.comparing(SectionSeriesData::nome, String.CASE_INSENSITIVE_ORDER));
    return new SectionDemandSeriesResponse(categories, series);
  }

  private String normalizeStep(String raw) {
    if (!StringUtils.hasText(raw)) return "month";
    raw = raw.toLowerCase(Locale.ROOT);
    return switch (raw) {
      case "day", "dias", "diario" -> "day";
      case "week", "semana", "semanal" -> "week";
      default -> "month";
    };
  }

  private static class GroupSeriesDataBuilder {
    Long groupId;
    String name;
    Map<String, Long> values = new LinkedHashMap<>();

    GroupSeriesDataBuilder(Long groupId, String name) {
      this.groupId = groupId;
      this.name = name;
    }

    void put(String category, Long value) {
      values.merge(category, value, Long::sum);
    }
  }

  private static class SectionSeriesDataBuilder {
    Long sectionId;
    String name;
    Map<String, Long> values = new LinkedHashMap<>();

    SectionSeriesDataBuilder(Long sectionId, String name) {
      this.sectionId = sectionId;
      this.name = name;
    }

    void put(String category, Long value) {
      values.merge(category, value, Long::sum);
    }
  }
}
