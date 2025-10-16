package is.hi.matarpontun.repository;

import is.hi.matarpontun.model.FoodType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FoodTypeRepository extends JpaRepository<FoodType, Long> {
    /**
     * Finds a FoodType by its unique type name (e.g., "A1", "M3").
     * @return An Optional containing the FoodType if found, or an empty Optional.
     */
    Optional<FoodType> findByTypeName(String typeName);
}
