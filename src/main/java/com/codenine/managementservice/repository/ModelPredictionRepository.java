package com.codenine.managementservice.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.codenine.managementservice.entity.ModelPrediction;

public interface ModelPredictionRepository extends JpaRepository<ModelPrediction, Long> {

  @Query("select max(p.refDate) from ModelPrediction p where p.horizonDays = :h")
  LocalDate findMaxRefDateByHorizon(@Param("h") Integer horizonDays);

  List<ModelPrediction> findByRefDateAndHorizonDays(LocalDate refDate, Integer horizonDays);

  @Modifying
  @Transactional
  @Query("delete from ModelPrediction p where p.refDate = :refDate and p.horizonDays = :horizon")
  void deleteByRefDateAndHorizon(
      @Param("refDate") LocalDate refDate, @Param("horizon") int horizon);
}
