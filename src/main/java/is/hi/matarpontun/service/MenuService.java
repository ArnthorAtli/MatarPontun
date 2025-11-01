package is.hi.matarpontun.service;

import is.hi.matarpontun.model.FoodType;
import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.model.Menu;
import is.hi.matarpontun.repository.FoodTypeRepository;
import is.hi.matarpontun.repository.MealRepository;
import is.hi.matarpontun.repository.MenuRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final MealRepository mealRepository;
    private final FoodTypeRepository foodTypeRepository;

    public MenuService(MenuRepository menuRepository,
            MealRepository mealRepository,
            FoodTypeRepository foodTypeRepository) {
        this.menuRepository = menuRepository;
        this.mealRepository = mealRepository;
        this.foodTypeRepository = foodTypeRepository;
    }

    /**
     * Creates one Menu per FoodType for the date (today + daysInFuture),
     * randomly choosing a meal for each category.
     */
    public String createMenusForFutureDay(int daysInFuture) {
        LocalDate targetDate = LocalDate.now().plusDays(daysInFuture);
        List<FoodType> foodTypes = foodTypeRepository.findAll();
        Random rand = new Random();

        if (foodTypes.isEmpty()) {
            return "⚠️ No food types found in the database.";
        }

        int createdCount = 0;

        for (FoodType foodType : foodTypes) {
            // Check if a menu already exists for this date and food type
            if (menuRepository.findByFoodTypeAndDate(foodType, targetDate).isPresent()) {
                System.out.println("Skipping existing menu for " + foodType.getTypeName() + " on " + targetDate);
                continue;
            }

            Menu menu = new Menu(targetDate, foodType);

            // Pick one random meal for each category belonging to this foodType
            menu.setBreakfast(randomMealFor(foodType, "breakfast", rand));
            menu.setLunch(randomMealFor(foodType, "lunch", rand));
            menu.setAfternoonSnack(randomMealFor(foodType, "afternoonSnack", rand));
            menu.setDinner(randomMealFor(foodType, "dinner", rand));
            menu.setNightSnack(randomMealFor(foodType, "nightSnack", rand));

            menuRepository.save(menu);
            createdCount++;
        }

        return "Created " + createdCount + " new menus for date " + targetDate + ".";
    }

    private Meal randomMealFor(FoodType foodType, String category, Random rand) {
        List<Meal> meals = mealRepository.findByFoodTypeId(foodType.getId())
                .stream()
                .filter(m -> m.getCategory().equalsIgnoreCase(category))
                .toList();

        if (meals.isEmpty()) {
            throw new EntityNotFoundException(
                    "No meals found for category '" + category + "' and foodType '" + foodType.getTypeName() + "'");
        }

        return meals.get(rand.nextInt(meals.size()));
    }

    public void assignMenuOfTheDay() {
        LocalDate today = LocalDate.now();
        List<Menu> todayMenus = menuRepository.findAll()
                .stream()
                .filter(menu -> menu.getDate().equals(today))
                .toList();

        if (todayMenus.isEmpty()) {
            throw new IllegalStateException("No menus found for today (" + today + ")");
        }

        for (Menu menu : todayMenus) {
            FoodType foodType = menu.getFoodType();
            if (foodType != null) {
                foodType.setMenuOfTheDay(menu);
                foodTypeRepository.save(foodType);
            }
        }
    }
}
