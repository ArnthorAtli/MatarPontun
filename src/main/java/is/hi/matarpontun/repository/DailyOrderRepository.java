package is.hi.matarpontun.repository;

import is.hi.matarpontun.model.DailyOrder;
import is.hi.matarpontun.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyOrderRepository extends JpaRepository<DailyOrder, Long> {
    Optional<DailyOrder> findByPatientAndOrderDate(Patient patient, LocalDate orderDate);

    // UC10 filters
    List<DailyOrder> findByOrderDate(LocalDate orderDate);
    List<DailyOrder> findByFoodType_TypeName(String typeName);
    List<DailyOrder> findByOrderDateAndFoodType_TypeName(LocalDate orderDate, String typeName);
    List<DailyOrder> findByWardName(String wardName);
    List<DailyOrder> findByWardNameAndOrderDate(String wardName, LocalDate orderDate);
    List<DailyOrder> findByWardNameAndFoodType_TypeName(String wardName, String foodType);
    List<DailyOrder> findByWardNameAndOrderDateAndFoodType_TypeName(String wardName, LocalDate orderDate, String foodType);
    List<DailyOrder> findByWardNameAndStatus(String wardName, String status);
    List<DailyOrder> findByWardNameAndOrderDateAndStatus(String wardName, LocalDate orderDate, String status);
    List<DailyOrder> findByWardNameAndFoodType_TypeNameAndStatus(String wardName, String foodType, String status);
    List<DailyOrder> findByWardNameAndOrderDateAndFoodType_TypeNameAndStatus(String wardName, LocalDate orderDate, String foodType, String status);
}