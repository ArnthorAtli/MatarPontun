package is.hi.matarpontun.repository;

import is.hi.matarpontun.model.DailyOrder;
import is.hi.matarpontun.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyOrderRepository extends JpaRepository<DailyOrder, Long> {
    Optional<DailyOrder> findByPatientAndOrderDate(Patient patient, LocalDate orderDate);
}