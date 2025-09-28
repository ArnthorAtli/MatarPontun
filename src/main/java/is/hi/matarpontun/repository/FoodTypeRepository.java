package is.hi.matarpontun.repository;

import is.hi.matarpontun.model.FoodType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FoodTypeRepository extends JpaRepository<FoodType, Long> {
    Optional<FoodType> findByTypeName(String typeName);

    /*
    FoodType save(FoodType foodType);
    FoodType delete(String typeName);
    //FoodType findAll(); -> craches with spring framework
    FoodType findByTypeName(String typeName);
    */
}
