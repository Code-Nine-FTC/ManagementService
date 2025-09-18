package com.codenine.managementservice.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.codenine.managementservice.entity.ItemType;



public interface ItemTypeRepository  extends JpaRepository<ItemType, Long> {

    
} 