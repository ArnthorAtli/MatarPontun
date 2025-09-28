package is.hi.matarpontun.dto;

import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.model.Menu;

// record er tegund sem við notum fyrir klasa sem geyma gögn, býr til getters, settera og fleira....
public record PatientMealDTO(
        Long id,
        String name,
        int age,
        int bedNumber,
        String foodTypeName,
        Meal nextMeal,
        Menu menuOfTheDay
) {}