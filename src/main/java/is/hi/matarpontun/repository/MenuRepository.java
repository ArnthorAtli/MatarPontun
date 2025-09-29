package is.hi.matarpontun.repository;

import is.hi.matarpontun.model.FoodType;
import is.hi.matarpontun.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByFoodTypeAndDate(FoodType foodType, LocalDate date);

    /*
    Menu save(Menu menu);
    Menu delete(Date date, String foodType);
    Menu findAll(Menu menu);
    Menu findByDate(Date date);
    Menu findByFoodType(String foodType);
    */
}
