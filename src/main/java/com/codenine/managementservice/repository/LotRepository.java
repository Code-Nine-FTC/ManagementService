package com.codenine.managementservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codenine.managementservice.entity.Item;
import com.codenine.managementservice.entity.Lot;

public interface LotRepository extends JpaRepository<Lot, Long> {
  List<Lot> findByItemOrderByExpireDateAscCreatedAtAsc(Item item);
  boolean existsByItemAndCode(Item item, String code);
}
