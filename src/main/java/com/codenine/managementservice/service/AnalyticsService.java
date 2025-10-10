package com.codenine.managementservice.service;

import com.codenine.managementservice.entity.OrderItem;
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.repository.OrderRepository;
import com.codenine.managementservice.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepositorio;

    public List<Map<String, Object>> getConsumptionSummary(LocalDateTime from, LocalDateTime to, String metric, int top) {
        List<Object[]> results = orderItemRepositorio.findConsumptionSummary(from, to, Arrays.asList("COMPLETED", "WITHDRAWN"));
        List<Map<String, Object>> summary = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("sectionId", row[0]);
            item.put("sectionName", row[1]);
            item.put("totalQuantity", row[2]);
            item.put("requestsCount", row[3]);
            summary.add(item);
        }
        summary.sort((a, b) -> {
            if ("TOTAL_QUANTITY".equals(metric)) {
                return Long.compare((Long) b.get("totalQuantity"), (Long) a.get("totalQuantity"));
            } else {
                return Long.compare((Long) b.get("requestsCount"), (Long) a.get("requestsCount"));
            }
        });
        return summary.stream().limit(top).collect(Collectors.toList());
    }

    public Map<String, Object> getConsumptionSeries(LocalDateTime from, LocalDateTime to, String metric) {
        List<OrderItem> items = orderItemRepositorio.findByOrderWithdrawDayBetweenAndOrderStatusIn(from, to, Arrays.asList("COMPLETED", "WITHDRAWN"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        // Agrupa por mês e seção
        Map<String, Map<Long, List<OrderItem>>> grouped = items.stream()
                .collect(Collectors.groupingBy(
                        oi -> oi.getOrder().getWithdrawDay().format(formatter),
                        Collectors.groupingBy(oi -> oi.getOrder().getSection().getId())
                ));
        Set<String> months = new TreeSet<>(grouped.keySet());
        Map<Long, String> sectionNames = new HashMap<>();
        for (OrderItem oi : items) {
            Section section = oi.getOrder().getSection();
            sectionNames.put(section.getId(), section.getTitle());
        }
        List<Map<String, Object>> series = new ArrayList<>();
        for (Long sectionId : sectionNames.keySet()) {
            Map<String, Object> serie = new HashMap<>();
            serie.put("name", sectionNames.get(sectionId));
            List<Number> data = new ArrayList<>();
            for (String month : months) {
                List<OrderItem> monthItems = grouped.getOrDefault(month, Collections.emptyMap()).getOrDefault(sectionId, Collections.emptyList());
                if ("TOTAL_QUANTITY".equals(metric)) {
                    data.add(monthItems.stream().mapToLong(OrderItem::getQuantity).sum());
                } else {
                    data.add(monthItems.stream().map(oi -> oi.getOrder().getId()).distinct().count());
                }
            }
            serie.put("data", data);
            series.add(serie);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("categories", months);
        result.put("series", series);
        return result;
    }
}
