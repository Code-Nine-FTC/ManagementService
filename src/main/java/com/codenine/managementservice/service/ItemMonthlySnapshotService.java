package com.codenine.managementservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.ItemMonthlySnapshot;
import com.codenine.managementservice.entity.OrderItem;
import com.codenine.managementservice.repository.ItemMonthlySnapshotRepository;
import com.codenine.managementservice.repository.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemMonthlySnapshotService {

  private final ItemRepository itemRepository;
  private final ItemMonthlySnapshotRepository snapshotRepository;

  // Agenda para rodar todo dia 1º do mês às 2h da manhã
  @Scheduled(cron = "0 0 2 1 * *")
  public void createSnapshot() {
    int yearMonth = LocalDate.now().getYear() * 100 + LocalDate.now().getMonthValue();
    List<Item> items = itemRepository.findAll();

    for (Item item : items) {
      List<OrderItem> orderItemsThisMonth =
          item.getOrderItems().stream()
              .filter(
                  oi -> {
                    LocalDate data = oi.getOrder().getCreatedAt().toLocalDate();
                    int orderYearMonth = data.getYear() * 100 + data.getMonthValue();
                    return orderYearMonth >= yearMonth;
                  })
              .toList();

      int ordersPlaced = orderItemsThisMonth.size();
      float averageConsumed =
          (float) orderItemsThisMonth.stream().mapToInt(oi -> oi.getQuantity()).average().orElse(0);

      ItemMonthlySnapshot snapshot = new ItemMonthlySnapshot();
      snapshot.setItem(item);
      snapshot.setYearMonth(yearMonth);
      snapshot.setStockQuantity(item.getCurrentStock().floatValue());
      snapshot.setOrdersPlaced(ordersPlaced);
      snapshot.setAverageConsumed(averageConsumed);
      snapshot.setCreatedAt(LocalDateTime.now());

      snapshotRepository.save(snapshot);
    }
  }
}
