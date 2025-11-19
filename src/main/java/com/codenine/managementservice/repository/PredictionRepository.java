package com.codenine.managementservice.repository;

import com.codenine.managementservice.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    /**
     * Busca a previsão mais recente para um item e mês específico
     */
    Optional<Prediction> findByItemIdAndPredictionMonth(Long itemId, LocalDate predictionMonth);

    /**
     * Busca todas as previsões de um item ordenadas por mês
     */
    List<Prediction> findByItemIdOrderByPredictionMonthDesc(Long itemId);

    /**
     * Busca previsões de um mês específico
     */
    List<Prediction> findByPredictionMonth(LocalDate predictionMonth);

    /**
     * Busca previsões criadas após uma data específica
     */
    List<Prediction> findByCreatedAtAfter(java.time.LocalDateTime date);

    /**
     * Verifica se existe previsão para um item no mês
     */
    boolean existsByItemIdAndPredictionMonth(Long itemId, LocalDate predictionMonth);

    /**
     * Busca itens que não possuem previsão para o próximo mês
     */
    @Query("SELECT i.id FROM Item i WHERE i.id NOT IN " +
           "(SELECT p.item.id FROM Prediction p WHERE p.predictionMonth = :nextMonth)")
    List<Long> findItemsWithoutPredictionForMonth(@Param("nextMonth") LocalDate nextMonth);
}
