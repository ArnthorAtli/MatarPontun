package is.hi.matarpontun.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import is.hi.matarpontun.model.FoodType;
import is.hi.matarpontun.repository.FoodTypeRepository;
import java.util.List;

/**
 * Service for managing {@link FoodType} configuration.
 */
@Service
public class FoodTypeService {

    private final FoodTypeRepository foodTypeRepository;

    /**
     * Constructs a new {@code FoodTypeService}.
     *
     * @param foodTypeRepository the repository responsible for accessing {@link FoodType} entities
     */
    public FoodTypeService(FoodTypeRepository foodTypeRepository) {
        this.foodTypeRepository = foodTypeRepository;
    }

    /**
     * Clears the {@code menuOfTheDay} reference for all {@link FoodType}, this allows menus to be safely deleted.
     *
     * @return the number of {@link FoodType} entities updated
     */
    @Transactional
    public int clearAllMenusOfTheDay() {
        List<FoodType> foodTypes = foodTypeRepository.findAll();
        for (FoodType ft : foodTypes) {
            ft.setMenuOfTheDay(null);
        }
        foodTypeRepository.saveAll(foodTypes);
        return foodTypes.size();
    }
}
