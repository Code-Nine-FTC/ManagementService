package com.codenine.managementservice.mapper;


import com.codenine.managementservice.dto.ItemRequest;
import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.ItemType;
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.SupplierCompany;
import com.codenine.managementservice.entity.User;

public class ItemMapper {
    public static Item toEntity(ItemRequest dto, User lastUser, SupplierCompany supplier, ItemType itemType) {
        Item item = new Item();
        item.setName(dto.name());
        item.setCurrentStock(dto.currentStock());
        item.setMeasure(dto.measure());
        item.setExpireDate(dto.expireDate());
        item.setMinimumStock(dto.minimumStock());
        item.setQrCode(null);
        item.setLastUser(lastUser);
        item.setSupplier(supplier);
        item.setItemType(itemType);
        return item;
    }
    
    public static void updateEntity(Item item, ItemRequest dto, User lastUser, SupplierCompany supplier) {
        if (dto.name() != null) item.setName(dto.name());
        if (dto.currentStock() != null) item.setCurrentStock(dto.currentStock());
        if (dto.measure() != null) item.setMeasure(dto.measure());
        if (dto.expireDate() != null) item.setExpireDate(dto.expireDate());
        if (dto.minimumStock() != null) item.setMinimumStock(dto.minimumStock());
        if (supplier != null) item.setSupplier(supplier);
        if (dto.name() != null || dto.currentStock() != null || dto.measure() != null || dto.expireDate() != null || dto.minimumStock() != null || supplier != null) {
            item.setLastUpdate(java.time.LocalDateTime.now());
            item.setLastUser(lastUser);
        }
    }

}
