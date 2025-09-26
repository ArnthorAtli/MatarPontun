package is.hi.matarpontun.repository;

import is.hi.matarpontun.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRepository extends JpaRepository<Meal, Long> {
    //a eftir ad baeta vid...
}