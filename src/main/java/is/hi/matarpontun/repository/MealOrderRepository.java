package is.hi.matarpontun.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import is.hi.matarpontun.model.MealOrder;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MealOrderRepository extends JpaRepository<MealOrder, Long> {
    List<MealOrder> findByOrderTimeBetween(LocalDateTime start, LocalDateTime end);
}
