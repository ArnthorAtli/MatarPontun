package is.hi.matarpontun.controller;

import is.hi.matarpontun.dto.MealDTO;
import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.model.Menu;
import is.hi.matarpontun.service.MealService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import is.hi.matarpontun.repository.MealRepository;
import is.hi.matarpontun.repository.MenuRepository;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/meals")
public class MealController {

    private final MealService mealService;
    private final MealRepository mealRepository;
    private final MenuRepository menuRepository;

    public MealController(MealService mealService, MealRepository mealRepository, MenuRepository menuRepository) {
        this.mealService = mealService;
        this.mealRepository = mealRepository;
        this.menuRepository = menuRepository;
    }

    // UC21 - Create new meal
    @PostMapping("/newMeal")
    public ResponseEntity<?> createMeal(@RequestBody MealDTO dto) {
        Meal savedMeal = mealService.createMeal(dto);
        return ResponseEntity.ok(savedMeal);
    }

    // UC23 - Modify meal ingredients
    @PutMapping("/modifyMealIngredients/{mealId}")
    public ResponseEntity<?> modifyMeal(@PathVariable Long mealId, @RequestBody Map<String, String> body) {
        String newIngredients = body.get("ingredients");
        if (newIngredients == null || newIngredients.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing or empty 'ingredients' field"));
        }

        Meal updatedMeal = mealService.modifyMealIngredients(mealId, newIngredients);
        return ResponseEntity.ok(Map.of(
                "message", "Meal ingredients updated successfully",
                "mealId", updatedMeal.getId(),
                "newIngredients", updatedMeal.getIngredients()));
    }

    // UC23 - Modify meal name
    @PutMapping("/modifyMealName/{mealId}")
    public ResponseEntity<?> modifyMealName(@PathVariable Long mealId, @RequestBody Map<String, String> body) {
        String newName = body.get("name");
        if (newName == null || newName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing or empty 'name' field"));
        }

        Meal updatedMeal = mealService.modifyMealName(mealId, newName);
        return ResponseEntity.ok(Map.of(
                "message", "Meal name updated successfully",
                "mealId", updatedMeal.getId(),
                "newName", updatedMeal.getName()));
    }

    // UC22 - Delete meal
    @DeleteMapping("/{mealId}")
    public ResponseEntity<?> deleteMeal(@PathVariable Long mealId) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new RuntimeException("Meal with ID " + mealId + " not found"));

        // Check if this meal is used in any menu
        List<Menu> menus = menuRepository.findAll();
        for (Menu menu : menus) {
            if ((menu.getBreakfast() != null && menu.getBreakfast().equals(meal)) ||
                    (menu.getLunch() != null && menu.getLunch().equals(meal)) ||
                    (menu.getAfternoonSnack() != null && menu.getAfternoonSnack().equals(meal)) ||
                    (menu.getDinner() != null && menu.getDinner().equals(meal)) ||
                    (menu.getNightSnack() != null && menu.getNightSnack().equals(meal))) {

                // Meal is still referenced → stop deletion
                return ResponseEntity.status(409).body(
                        Map.of(
                                "error", "Meal with ID " + mealId + " is currently used in Menu " + menu.getId() +
                                        ". Modify or remove it from that menu before deleting the meal."));
            }
        }

        // If it’s not in any menu, delete it safely
        mealRepository.delete(meal);
        return ResponseEntity.ok(Map.of(
                "message", "Meal with ID " + mealId + " deleted successfully."));
    }
}
