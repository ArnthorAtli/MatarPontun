package is.hi.matarpontun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import is.hi.matarpontun.model.MealOrder;
import is.hi.matarpontun.model.Patient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MealOrderRepository extends JpaRepository<MealOrder, Long> {
    List<MealOrder> findByOrderTimeBetween(LocalDateTime start, LocalDateTime end);
    Optional<MealOrder> findFirstByPatientAndStatusAndMealTypeOrderByOrderTimeDesc(
            Patient patient,
            String status,
            String mealType
    );


    @Query("SELECT mo FROM MealOrder mo " +
           "WHERE mo.patient = :patient " +
           "AND mo.status = :status " +
           "AND mo.mealType = :mealType " +
           "ORDER BY mo.orderTime DESC " +
           "LIMIT 1")
    Optional<MealOrder> findNextPendingOrder(
            @Param("patient") Patient patient,
            @Param("status") String status,
            @Param("mealType") String mealType
    );
}



/*
@Query("""
    SELECT mo FROM MealOrder mo
    WHERE mo.patient = :patient
      AND mo.status = :status
      AND mo.mealType = :mealType
    ORDER BY mo.orderTime DESC
    """)
List<MealOrder> findPendingOrders(
        @Param("patient") Patient patient,
        @Param("status") String status,
        @Param("mealType") String mealType
);

Then pick the first result in Java:
var nextOrderOpt = mealOrderRepository.findPendingOrders(patient, "PENDING", currentMealType)
                                      .stream().findFirst();
*/
