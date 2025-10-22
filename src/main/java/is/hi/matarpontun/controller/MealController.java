package is.hi.matarpontun.controller;

import is.hi.matarpontun.service.MealService;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/meal")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }
}
