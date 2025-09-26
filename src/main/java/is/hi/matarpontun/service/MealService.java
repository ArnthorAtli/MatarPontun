package is.hi.matarpontun.service;

import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.repository.MealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MealService {

    @Autowired
    private MealRepository mealRepository;

    public List<Meal> findAllMeals() {
        return mealRepository.findAll();
    }


}