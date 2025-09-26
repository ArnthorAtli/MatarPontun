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
    public String[] faediOptions = {
      "A1 - almennt fæði",
      "A2 - Hentar eldri kynslóðinni",
      "A3 - Grænmetisfæði",
      "OP - Orku og próteinbætt fæði",
      "RDS kjöt/fiskur",
      "RDS grænmetisfæði",
      "M1 - Mjúkt",
      "M2 - Hakkað",
      "M3 - Fínmaukað",
      "F1 - Fljótandi fæði",
      "F1 Sykurskert",
      "F1 Mjólkurlaust",
      "F2 - Tært fljótandi",
      "F3 - Fljótandi fæði eftir aðgerð",
      "F4 - Þykkfljótandi fæði",
      "F4 Sykurskert",
      "F5 - Fljótandi fæði kalt",
      "Fituskert 40g",
      "Próteinskert 50g",
      "Próteinskert 50g Sykurskert",
      "Próteinskert 60g",
      "Saltskert",
      "Salskert sykurskert",
      "Blóðskilunarfæði",
      "Blóðskilunarfæði Sykurskert",
      "FSMS",
      "Mjólkursykurskert",
      "Glútensnautt",
      "Örveruskert",
      "Joðsnautt",
      "Ungbarnafæði",
      "FASTANDI",
      "EINNOTA"
  };
}
