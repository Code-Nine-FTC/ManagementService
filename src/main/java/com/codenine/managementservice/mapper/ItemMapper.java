package com.codenine.managementservice.mapper;
import com.codenine.managementservice.dto.ItemRequest;
import com.codenine.managementservice.entity.Item;

public class ItemMapper {
    public static Item toEntity(ItemRequest dto) {
        Item item = new Item();
        item.setName(dto.name());
        item.setCurrentStock(dto.currentStock());
        item.setMeasure(dto.measure());
        item.setExpireDate(dto.expireDate());
        item.setMinimumStock(dto.minimumStock());
        item.setQrCode(dto.qrCode());
        return item;
    }
    
    public static void updateEntity(Item item, ItemRequest dto) {
        if (dto.name() != null) item.setName(dto.name());
        if (dto.currentStock() != null) item.setCurrentStock(dto.currentStock());
        if (dto.measure() != null) item.setMeasure(dto.measure());
        if (dto.expireDate() != null) item.setExpireDate(dto.expireDate());
        if (dto.minimumStock() != null) item.setMinimumStock(dto.minimumStock());
        if (dto.qrCode() != null) item.setQrCode(dto.qrCode());
        if (dto.name() != null || dto.currentStock() != null || dto.measure() != null || dto.expireDate() != null || dto.minimumStock() != null || dto.qrCode() != null) {
            item.setLastUpdate(java.time.LocalDateTime.now());
        }
    }

}
