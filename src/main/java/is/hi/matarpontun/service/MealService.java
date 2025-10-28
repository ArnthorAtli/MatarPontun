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

@Service
public class MealService {

    @Autowired
    private MealRepository mealRepository;
    private final FoodTypeRepository foodTypeRepository;

    public MealService(MealRepository mealRepository, FoodTypeRepository foodTypeRepository) {
        this.mealRepository = mealRepository;
        this.foodTypeRepository = foodTypeRepository;
    }

    public List<Meal> findAllMeals() {
        return mealRepository.findAll();
    }

    /**
     * Selects the correct meal for a given patient based on current time and their
     * FoodType's menu.
     */
    // á eftir að setja limit á allergiis eða restriction
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

    public Meal createMeal(MealDTO dto) {
        FoodType foodType = foodTypeRepository.findById(dto.foodTypeId())
                .orElseThrow(() -> new EntityNotFoundException("FoodType not found with ID: " + dto.foodTypeId()));

        Meal meal = new Meal(dto.name(), dto.ingredients(), dto.category(), foodType);
        return mealRepository.save(meal);
    }

    public Meal modifyMealIngredients(Long mealId, String newIngredients) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new EntityNotFoundException("Meal not found with ID: " + mealId));

        meal.setIngredients(newIngredients);
        return mealRepository.save(meal);
    }

    public Meal modifyMealName(Long mealId, String newName) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new EntityNotFoundException("Meal not found with ID: " + mealId));

        meal.setName(newName);
        return mealRepository.save(meal);
    }

    

}