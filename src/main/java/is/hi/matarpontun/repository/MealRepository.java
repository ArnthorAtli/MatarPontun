package is.hi.matarpontun.repository;

import is.hi.matarpontun.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findByFoodTypeId(Long foodTypeId);
}