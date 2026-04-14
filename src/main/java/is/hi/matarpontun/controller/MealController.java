package is.hi.matarpontun.controller;

import is.hi.matarpontun.dto.MealDTO;
import is.hi.matarpontun.model.FoodType;
import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.model.Menu;
import is.hi.matarpontun.repository.FoodTypeRepository;
import is.hi.matarpontun.repository.MealRepository;
import is.hi.matarpontun.repository.MenuRepository;
import is.hi.matarpontun.service.FoodTypeService;
import is.hi.matarpontun.service.MealService;
import is.hi.matarpontun.service.MenuService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final FoodTypeRepository foodTypeRepository;

    public MealController(MealService mealService, MealRepository mealRepository, MenuRepository menuRepository,
            MenuService menuService, FoodTypeService foodTypeService, FoodTypeRepository foodTypeRepository) {
        this.mealService = mealService;
        this.mealRepository = mealRepository;
        this.menuRepository = menuRepository;
        this.menuService = menuService;
        this.foodTypeService = foodTypeService;
        this.foodTypeRepository = foodTypeRepository;
    }

    // -------------------------------------------------------------------------
    // Response records
    // -------------------------------------------------------------------------

    /** Summary of a food type, including the ID of its currently assigned menuOfTheDay. */
    record FoodTypeSummary(Long id, String typeName, String description, Long menuId) {}

    /** A single meal slot within a menu (one course of the day). */
    record MealSlot(String name, String ingredients) {}

    /** Full menu detail returned to the client for a given food type. */
    record MenuDetail(
            Long foodTypeId,
            String foodTypeName,
            Long menuId,
            MealSlot breakfast,
            MealSlot lunch,
            MealSlot afternoonSnack,
            MealSlot dinner,
            MealSlot nightSnack
    ) {}

    /**
     * GET {@code /meals/food-types}
     *
     * Returns all food types. menuId is the ID of the food type's currently assigned
     * menuOfTheDay, or null if none has been set.
     */
    @GetMapping("/food-types")
    public ResponseEntity<List<FoodTypeSummary>> getAllFoodTypes() {
        List<FoodTypeSummary> result = foodTypeRepository.findAll().stream()
                .map(ft -> {
                    Menu menu = ft.getMenuOfTheDay();
                    Long menuId = menu != null ? menu.getId() : null;
                    return new FoodTypeSummary(ft.getId(), ft.getTypeName(), ft.getDescription(), menuId);
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    /**
     * UC21 - Create a new meal
     * POST {@code /meals/newMeal}
     * 
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
     * 
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
     * 
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
     * 
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
     * 
     * Creates one random menu per food type for the target date.
     * The request body: {@code "daysInTheFuture": }.
     *
     * @param request the message specifying how many days ahead to generate menus for.
     * @return {@code 200 OK} with a result string.
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
        public void setDaysInTheFuture(int daysInTheFuture) {
            this.daysInTheFuture = daysInTheFuture;
        }
    }

    /**
     * PUT {@code /meals/assignMenuOfTheDay}
     * 
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
     *
     * Clears the "menu of the day" for all food types so menus can be safely deleted.
     *
     * @return {@code 200 OK} with a confirmation message and the number of affected food types
     */
    @PostMapping("/resetMenusOfTheDay")
    public ResponseEntity<?> resetMenusOfTheDay() {
        int updatedCount = foodTypeService.clearAllMenusOfTheDay();
        return ResponseEntity.ok(Map.of(
                "message", "All menus of the day cleared.",
                "foodTypesAffected", updatedCount
        ));
    }

    /**
     * GET {@code /meals/menu/{foodTypeId}}
     *
     * Returns the menuOfTheDay assigned to the given food type, with full meal details
     * (name and ingredients) for each slot.
     *
     * @param foodTypeId the ID of the food type
     * @return {@code 200 OK} with {@link MenuDetail}, or {@code 404} if the food type
     *         doesn't exist or has no menu assigned
     */
    @GetMapping("/menu/{foodTypeId}")
    public ResponseEntity<MenuDetail> getMenuForFoodType(@PathVariable Long foodTypeId) {
        Optional<FoodType> ftOpt = foodTypeRepository.findById(foodTypeId);
        if (ftOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        FoodType ft = ftOpt.get();
        Menu menu = ft.getMenuOfTheDay();
        if (menu == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        return ResponseEntity.ok(new MenuDetail(
                ft.getId(),
                ft.getTypeName(),
                menu.getId(),
                toSlot(menu.getBreakfast()),
                toSlot(menu.getLunch()),
                toSlot(menu.getAfternoonSnack()),
                toSlot(menu.getDinner()),
                toSlot(menu.getNightSnack())
        ));
    }

    /** Maps a {@link Meal} to its slim {@link MealSlot} representation. Returns null for unset slots. */
    private MealSlot toSlot(Meal meal) {
        if (meal == null) return null;
        return new MealSlot(meal.getName(), meal.getIngredients());
    }
}
