package is.hi.matarpontun.repository;

import is.hi.matarpontun.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    /*
    Menu save(Menu menu);
    Menu delete(Date date, String foodType);
    Menu findAll(Menu menu);
    Menu findByDate(Date date);
    Menu findByFoodType(String foodType);
    */
}
