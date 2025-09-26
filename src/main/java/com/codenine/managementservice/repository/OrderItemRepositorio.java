package com.codenine.managementservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codenine.managementservice.entity.OrderItem;

public interface OrderItemRepositorio extends JpaRepository<OrderItem, Long> {

}
