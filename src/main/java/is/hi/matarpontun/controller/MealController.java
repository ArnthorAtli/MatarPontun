package is.hi.matarpontun.controller;

import is.hi.matarpontun.dto.MealDTO;
import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.model.Menu;
import is.hi.matarpontun.service.MealService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import is.hi.matarpontun.repository.MealRepository;
import is.hi.matarpontun.repository.MenuRepository;
import is.hi.matarpontun.service.MenuService;
import java.util.Map;
import java.util.List;
import is.hi.matarpontun.service.FoodTypeService;

/**
 * REST controller responsible for handling requests related to meals and menus.
 */
@RestController
@RequestMapping("/meals")
public class MealController {

    private final MealService mealService;
    private final MealRepository mealRepository;
    private final MenuRepository menuRepository;
    private final MenuService menuService;
    private final FoodTypeService foodTypeService;


    /**
     * Constructs a new {@code MealController} with required services and repositories.
     *
     * @param mealService    the service responsible for business logic related to creating and updating meals
     * @param mealRepository the repository responsible for accessing {@link Meal} entities
     * @param menuRepository the repository responsible for accessing {@link Menu} entities
     * @param menuService    the service responsible for business logic related to generating and assigning menus
     */
    public MealController(MealService mealService, MealRepository mealRepository, MenuRepository menuRepository,
            MenuService menuService, FoodTypeService foodTypeService) {
        this.mealService = mealService;
        this.mealRepository = mealRepository;
        this.menuRepository = menuRepository;
        this.foodTypeService = foodTypeService;
        this.menuService = menuService;
    }

    /**
     * UC21 - Create a new meal
     * POST {@code /meals/newMeal}
     * <p>
     * Creates a new {@link Meal} from the provided DTO.
     *
     * @param dto the request body containing meal data
     * @return {@code 200 OK} with the saved {@link Meal}
     */
    @PostMapping("/newMeal")
    public ResponseEntity<?> createMeal(@RequestBody MealDTO dto) {
        Meal savedMeal = mealService.createMeal(dto);
        return ResponseEntity.ok(savedMeal);
    }

    /**
     * UC23 - Modify and existing meal
     * PUT {@code /meals/modifyMealIngredients/{mealId}}
     * <p>
     * Changes the ingredients of an existing meal.
     *
     * @param mealId the id of the meal to update
     * @param body   JSON body containing an {@code "ingredients"} field
     * @return {@code 200 OK} with a confirmation message or {@code 400 Bad Request}
     *         if the {@code "ingredients"} field is missing
     */
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

    /**
     * (UC23 - Modify meal name)
     * PUT {@code /meals/modifyMealName/{mealId}}
     * <p>
     * Changes the name of an existing meal.
     *
     * @param mealId the id of the meal to update
     * @param body   JSON body containing a {@code "name"} field
     * @return {@code 200 OK} with a confirmation message or {@code 400 Bad Request}
     *         if the {@code "name"} field is missing
     */
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

    /**
     * UC22 - Delete a meal
     * DELETE {@code /meals/{mealId}}
     * <p>
     * Deletes a meal if it is not referenced by any {@link Menu}. If the meal is in use,
     * responds with {@code 409 Conflict}.
     *
     * @param mealId the id of the meal to delete
     * @return {@code 200 OK} on successful deletion or {@code 409 Conflict} if the meal
     *         is currently used in a menu
     * @throws RuntimeException if the meal does not exist
     */
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

    /**
     * POST {@code /meals/createMenu}
     * <p>
     * Creates one random menu per food type for the target date.
     * The request body: {@code "daysInTheFuture": }.
     *
     * @param request the message specifying how many days ahead to generate menus for
     * @return {@code 200 OK} with a result string
     */
    @PostMapping("/createMenu")
    public ResponseEntity<String> createMenuForFutureDay(@RequestBody MenuRequest request) {
        String result = menuService.createMenusForFutureDay(request.getDaysInTheFuture());
        return ResponseEntity.ok(result);
    }

    /**
     * Simple DTO used for passing the number of days in the future when creating menus.
     */
    public static class MenuRequest {
        private int daysInTheFuture;
        public int getDaysInTheFuture() {
            return daysInTheFuture;
        }
        //Aldrei notað
        public void setDaysInTheFuture(int daysInTheFuture) {
            this.daysInTheFuture = daysInTheFuture;
        }
    }

    /**
     * PUT {@code /meals/assignMenuOfTheDay}
     * <p>
     * Assigns the “menu of the day” for each food type.
     *
     * @return {@code 200 OK} with a confirmation message or {@code 500 Internal Server Error}
     *         if an unexpected error occurs
     */
    @PutMapping("/assignMenuOfTheDay")
    public ResponseEntity<?> assignMenuOfTheDay() {
        try {
            menuService.assignMenuOfTheDay();
            return ResponseEntity.ok(Map.of("message", "Menus of the day successfully assigned to each FoodType"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST {@code /meals/resetMenusOfTheDay}
     * <p>
     * Clears the "menu of the day" for all food types so menus can be safely deleted.
     *
     * @return {@code 200 OK} with a confirmation message and the number of
     *         affected food types
     */
    @PostMapping("/resetMenusOfTheDay")
    public ResponseEntity<?> resetMenusOfTheDay() {
        int updatedCount = foodTypeService.clearAllMenusOfTheDay();
        return ResponseEntity.ok(Map.of(
                "message", "All menus of the day cleared.",
                "foodTypesAffected", updatedCount
        ));
    }

}
