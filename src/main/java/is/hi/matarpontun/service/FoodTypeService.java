package is.hi.matarpontun.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import is.hi.matarpontun.model.FoodType;
import is.hi.matarpontun.repository.FoodTypeRepository;
import java.util.List;



@Service
public class FoodTypeService {

    private final FoodTypeRepository foodTypeRepository;

    public FoodTypeService(FoodTypeRepository foodTypeRepository) {
        this.foodTypeRepository = foodTypeRepository;
    }

    /**
     * Clears the menuOfTheDay for all food types.
     * This allows menus to be safely deleted afterward.
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
