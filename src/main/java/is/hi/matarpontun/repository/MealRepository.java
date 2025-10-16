package is.hi.matarpontun.repository;

import is.hi.matarpontun.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findByFoodTypeId(Long foodTypeId);
    //a eftir ad baeta vid...
}