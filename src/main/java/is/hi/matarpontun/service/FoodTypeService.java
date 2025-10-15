package is.hi.matarpontun.service;

import is.hi.matarpontun.model.FoodType;
import is.hi.matarpontun.model.Menu;
import is.hi.matarpontun.repository.FoodTypeRepository;
import is.hi.matarpontun.repository.MenuRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class FoodTypeService {private final MenuRepository menuRepository;
    private final FoodTypeRepository foodTypeRepository;

    public FoodTypeService(MenuRepository menuRepository, FoodTypeRepository foodTypeRepository) {
        this.menuRepository = menuRepository;
        this.foodTypeRepository = foodTypeRepository;
    }

    public Menu getMenuOfTheDay(Long foodTypeId) {
        FoodType foodType = foodTypeRepository.findById(foodTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Food type not found"));
        return menuRepository.findByFoodTypeAndDate(foodType, LocalDate.now())
                .orElse(null);
    }
}
