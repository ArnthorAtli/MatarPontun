package is.hi.matarpontun.dto;

import is.hi.matarpontun.model.Meal;

import java.util.List;

// record er tegund sem við notum fyrir klasa sem geyma gögn, býr til getters, settera og fleira....
public record PatientMealDTO(
        Long id,
        String name,
        int age,
        String roomNumber,
        int bedNumber,
        String foodTypeName,
        Meal nextMeal,
        MenuOfTheDayDTO menuOfTheDay,
        List<String> restrictions,
        List<String> allergies
) {}

