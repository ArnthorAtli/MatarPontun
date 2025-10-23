package is.hi.matarpontun.service;

import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.model.Menu;
import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.repository.MealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class MealService {

    @Autowired
    private MealRepository mealRepository;

    public List<Meal> findAllMeals() {
        return mealRepository.findAll();
    }

    /**
     * Selects the correct meal for a given patient based on current time and their FoodType's menu.
     */
    //á eftir að setja limit á allergies eða restriction
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
}