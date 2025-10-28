package is.hi.matarpontun.repository;

import is.hi.matarpontun.model.FoodType;
import is.hi.matarpontun.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByFoodTypeAndDate(FoodType foodType, LocalDate date);

}
