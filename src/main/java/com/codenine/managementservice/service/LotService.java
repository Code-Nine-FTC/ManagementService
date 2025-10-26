package com.codenine.managementservice.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codenine.managementservice.dto.item.LotAdjustRequest;
import com.codenine.managementservice.dto.item.LotCreateRequest;
import com.codenine.managementservice.dto.item.LotResponse;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.Lot;
import com.codenine.managementservice.repository.ItemRepository;
import com.codenine.managementservice.repository.LotRepository;

@Service
public class LotService {

  private final LotRepository lotRepository;
  private final ItemRepository itemRepository;

  public LotService(LotRepository lotRepository, ItemRepository itemRepository) {
    this.lotRepository = lotRepository;
    this.itemRepository = itemRepository;
  }

  @Transactional
  public LotResponse create(LotCreateRequest req) {
    if (req == null || req.itemId() == null || req.code() == null || req.code().isBlank()) {
      throw new IllegalArgumentException("Dados do lote inválidos");
    }
    Item item =
        itemRepository
            .findById(req.itemId())
            .orElseThrow(() -> new IllegalArgumentException("Item não encontrado"));
    if (lotRepository.existsByItemAndCode(item, req.code())) {
      throw new IllegalArgumentException("Já existe um lote com este código para o item");
    }
    Lot lot = new Lot();
    lot.setItem(item);
    lot.setCode(req.code());
    lot.setExpireDate(req.expireDate());
    lot.setQuantityOnHand(req.quantity() != null ? Math.max(0, req.quantity()) : 0);
    lot = lotRepository.save(lot);

    // Atualiza estoque do item (simples): soma todos os lotes
    refreshItemStock(item);
    return toResponse(lot);
  }

  @Transactional(readOnly = true)
  public List<LotResponse> listByItem(Long itemId) {
    Item item =
        itemRepository
            .findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item não encontrado"));
    return lotRepository.findByItemOrderByExpireDateAscCreatedAtAsc(item).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public LotResponse adjust(Long lotId, LotAdjustRequest req) {
    Lot lot =
        lotRepository
            .findById(lotId)
            .orElseThrow(() -> new IllegalArgumentException("Lote não encontrado"));
    int delta = req != null && req.delta() != null ? req.delta() : 0;
    int newQty = Math.max(0, lot.getQuantityOnHand() + delta);
    lot.setQuantityOnHand(newQty);
    lot = lotRepository.save(lot);

    refreshItemStock(lot.getItem());
    return toResponse(lot);
  }

  private void refreshItemStock(Item item) {
    int total =
        lotRepository.findByItemOrderByExpireDateAscCreatedAtAsc(item).stream()
            .mapToInt(Lot::getQuantityOnHand)
            .sum();
    item.setCurrentStock(total);
    itemRepository.save(item);
  }

  private LotResponse toResponse(Lot lot) {
    return new LotResponse(
        lot.getId(),
        lot.getItem().getId(),
        lot.getItem().getName(),
        lot.getCode(),
        lot.getExpireDate(),
        lot.getQuantityOnHand());
  }
}
