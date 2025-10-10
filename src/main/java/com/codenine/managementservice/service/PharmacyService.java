package com.codenine.managementservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.item.ItemResponse;
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.ItemRepository;

@Service
public class PharmacyService {

  @Autowired private ItemRepository itemRepository;

  public record ExpirySummary(long expiredCount, long expiringSoonCount) {}

  public record ExpiryLists(List<ItemResponse> expired, List<ItemResponse> expiringSoon) {}

  public ExpirySummary getExpirySummary(int days) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime futureDate = now.plusDays(days);
    List<Long> userSectionIds = getUserSectionIds();

    long expiredCount = itemRepository.countExpiredItemsBySection(userSectionIds, "farmácia", now);
    long expiringSoonCount =
        itemRepository.countExpiringSoonItemsBySection(userSectionIds, "farmácia", now, futureDate);

    return new ExpirySummary(expiredCount, expiringSoonCount);
  }

  public ExpiryLists getExpiryLists(int days, Pageable pageable) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime futureDate = now.plusDays(days);
    List<Long> userSectionIds = getUserSectionIds();
    List<ItemResponse> expiredItems =
        itemRepository.findExpiredItemsBySection(userSectionIds, "farmácia", now);
    List<ItemResponse> expiringSoonItems =
        itemRepository.findExpiringSoonItemsBySection(userSectionIds, "farmácia", now, futureDate);

    return new ExpiryLists(expiredItems, expiringSoonItems);
  }

  public ExpirySummary getAllItemsExpirySummary(int days) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime futureDate = now.plusDays(days).withHour(23).withMinute(59).withSecond(59);
    List<Long> userSectionIds = getUserSectionIds();

    long expiredCount = itemRepository.countExpiredItemsBySection(userSectionIds, "farmácia", now);
    long expiringSoonCount =
        itemRepository.countExpiringSoonItemsBySection(userSectionIds, "farmácia", now, futureDate);

    return new ExpirySummary(expiredCount, expiringSoonCount);
  }

  public ExpiryLists getAllItemsExpiryLists(int days, Pageable pageable) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime futureDate = now.plusDays(days).withHour(23).withMinute(59).withSecond(59);
    List<Long> userSectionIds = getUserSectionIds();
    List<ItemResponse> expiredItems =
        itemRepository.findExpiredItemsBySection(userSectionIds, "farmácia", now);
    List<ItemResponse> expiringSoonItems =
        itemRepository.findExpiringSoonItemsBySection(userSectionIds, "farmácia", now, futureDate);

    return new ExpiryLists(expiredItems, expiringSoonItems);
  }

  private List<Long> getUserSectionIds() {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return user.getSections().stream().map(Section::getId).toList();
  }
}
