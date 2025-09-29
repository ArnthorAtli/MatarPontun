package is.hi.matarpontun.controller;

import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.service.MealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import java.util.List;

@Controller
public class MealController {

    @Autowired
    private MealService mealService;
    
    public List<Meal> fetchAllMeals() {
        return mealService.findAllMeals();
    }
}
