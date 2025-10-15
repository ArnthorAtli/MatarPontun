package is.hi.matarpontun.service;

import is.hi.matarpontun.model.FoodType;
import is.hi.matarpontun.model.Menu;
import is.hi.matarpontun.repository.FoodTypeRepository;
import is.hi.matarpontun.repository.MenuRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final FoodTypeRepository foodTypeRepository;

    @Autowired
    public MenuService(MenuRepository menuRepository, FoodTypeRepository foodTypeRepository) {
        this.menuRepository = menuRepository;
        this.foodTypeRepository = foodTypeRepository;
    }

    public Menu getMenuOfTheDay(Long foodTypeId) {
        FoodType foodType = foodTypeRepository.findById(foodTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Food type not found"));

        // Find today's menu for this food type
        return menuRepository.findByFoodTypeAndDate(foodType, LocalDate.now())
                .orElseThrow(() -> new EntityNotFoundException("No menu found for today"));
    }
}
