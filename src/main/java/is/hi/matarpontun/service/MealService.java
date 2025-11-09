package is.hi.matarpontun.service;

import is.hi.matarpontun.dto.MealDTO;
import is.hi.matarpontun.model.FoodType;
import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.model.Menu;
import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.repository.FoodTypeRepository;
import is.hi.matarpontun.repository.MealRepository;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

/**
 * Service responsible for managing {@link Meal} entities.
 */
@Service
public class MealService {

    @Autowired
    private MealRepository mealRepository;
    private final FoodTypeRepository foodTypeRepository;

    /**
     * Constructs a new {@code MealService} with required repositories.
     *
     * @param mealRepository      the repository responsible for accessing {@link Meal} entities
     * @param foodTypeRepository  repository for retrieving {@link FoodType} entities
     */
    public MealService(MealRepository mealRepository, FoodTypeRepository foodTypeRepository) {
        this.mealRepository = mealRepository;
        this.foodTypeRepository = foodTypeRepository;
    }

    /**
     * Retrieves all meals stored in the system.
     *
     * @return a list of all {@link Meal} entities
     */
    public List<Meal> findAllMeals() {
        return mealRepository.findAll();
    }

    /**
     * Selects the correct meal for a given patient based on current time and their foodtype's menu.
     */
    // á eftir að setja limit á allergies eða restriction - ennþá eftir? annars taka út
    public Meal selectMealForPatient(Patient patient) {
        if (patient == null || patient.getFoodType() == null) {
            return null;
        }

        Menu menu = patient.getFoodType().getMenuOfTheDay();

        if (menu == null) {
            return null; // no menu configured for this food type
        }

        // Step 1: Determine current time
        LocalTime now = LocalTime.now();

        // Step 2: Pick meal based on the current hour
        if (now.isBefore(LocalTime.of(10, 0))) {
            return menu.getBreakfast();
        } else if (now.isBefore(LocalTime.of(14, 0))) {
            return menu.getLunch();
        } else if (now.isBefore(LocalTime.of(17, 0))) {
            return menu.getAfternoonSnack();
        } else if (now.isBefore(LocalTime.of(21, 0))) {
            return menu.getDinner();
        } else {
            return menu.getNightSnack();
        }
    }

    /**
     * Creates a new {@link Meal} entity from the given {@link MealDTO}.
     *
     * @param dto the {@link MealDTO} containing meal details and the food type id.
     * @return the saved {@link Meal} entity.
     * @throws EntityNotFoundException if the specified food type does not exist.
     */
    public Meal createMeal(MealDTO dto) {
        FoodType foodType = foodTypeRepository.findById(dto.foodTypeId())
                .orElseThrow(() -> new EntityNotFoundException("FoodType not found with ID: " + dto.foodTypeId()));

        Meal meal = new Meal(dto.name(), dto.ingredients(), dto.category(), foodType);
        return mealRepository.save(meal);
    }

    /**
     * Changes the ingredient list for an existing {@link Meal}.
     *
     * @param mealId         the id of the meal to update.
     * @param newIngredients the new ingredient string.
     * @return the updated {@link Meal}.
     * @throws EntityNotFoundException if no meal exists with the given ID.
     */
    public Meal modifyMealIngredients(Long mealId, String newIngredients) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new EntityNotFoundException("Meal not found with ID: " + mealId));

        meal.setIngredients(newIngredients);
        return mealRepository.save(meal);
    }

    /**
     * Changes the name of an existing {@link Meal}.
     *
     * @param mealId  the id of the meal to update.
     * @param newName the new name for the meal.
     * @return the updated {@link Meal}.
     * @throws EntityNotFoundException if no meal exists with the given id.
     */
    public Meal modifyMealName(Long mealId, String newName) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new EntityNotFoundException("Meal not found with ID: " + mealId));

        meal.setName(newName);
        return mealRepository.save(meal);
    }

    

}