package com.codenine.managementservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codenine.managementservice.entity.ItemPrediction;

@Repository
public interface ItemPredictionRepository extends JpaRepository<ItemPrediction, Long> {

  List<ItemPrediction> findByItemIdOrderByCreatedAtDesc(Long itemId);

  Optional<ItemPrediction> findFirstByItemIdAndForecastTypeOrderByCreatedAtDesc(
      Long itemId, String forecastType);

  List<ItemPrediction> findByItemIdAndForecastType(Long itemId, String forecastType);
}
