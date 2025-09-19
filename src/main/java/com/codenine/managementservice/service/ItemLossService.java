package com.codenine.managementservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codenine.managementservice.dto.ItemLossRequest;
import com.codenine.managementservice.repository.ItemLossRepository;
import com.codenine.managementservice.repository.ItemRepository;
import com.codenine.managementservice.repository.UserRepository;

import java.time.LocalDateTime;

import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.ItemLoss;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.mapper.ItemLossMapper;

@Service
public class ItemLossService {
    @Autowired
    private ItemLossRepository itemLossRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;


    public void createItemLoss(ItemLossRequest request) {
        User recordedUser = userRepository.findById(request.recordedById())
                .orElseThrow(() -> new NullPointerException("User not found with id: " + request.recordedById()));
        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(() -> new NullPointerException("Item not found with id: " + request.itemId()));
        // arrumar last user segunda
        ItemLoss itemLoss = ItemLossMapper.toEntity(request, item, recordedUser, recordedUser);
        item.setCurrentStock(item.getCurrentStock() - request.lostQuantity());
        item.setLastUpdate(LocalDateTime.now());
        item.setLastUser(recordedUser);
        item.setLastUpdate(LocalDateTime.now());
        itemLossRepository.save(itemLoss);
        itemRepository.save(item);
    }
    

    public void updateItemLoss(Long id, ItemLossRequest request) {
        ItemLoss itemLoss = itemLossRepository.findById(id)
                .orElseThrow(() -> new NullPointerException("ItemLoss not found with id: " + id));
        Item item = null;
        if (request.itemId() != null) {
            item = itemRepository.findById(request.itemId())
                    .orElseThrow(() -> new NullPointerException("Item not found with id: " + request.itemId()));
        }
        // arrumar last usar segunda
        User lastUser = userRepository.findById(request.recordedById())
                .orElseThrow(() -> new NullPointerException("User not found with id: " + request.recordedById()));
        ItemLossMapper.updateEntity(itemLoss, request, item, lastUser);
        if (item != null && request.lostQuantity() != null) {
            int stockDifference = request.lostQuantity() - itemLoss.getLostQuantity();
            item.setCurrentStock(item.getCurrentStock() - stockDifference);
            item.setLastUpdate(LocalDateTime.now());
            item.setLastUser(lastUser);
            itemRepository.save(item);
        }
        itemLossRepository.save(itemLoss);
        }
}
