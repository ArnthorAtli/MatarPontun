package is.hi.matarpontun.dto;

import is.hi.matarpontun.model.DailyOrder;
import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.model.Patient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RestrictionCheckResultDTO {
    private String message;
    private String patient;
    private String status;
    private List<String> restrictions;
    private Map<String, MealInfo> meals = new LinkedHashMap<>();

    // ----- Inner class for meal details -----
    public static class MealInfo {
        private String name;
        private String ingredients;

        public MealInfo(Meal meal) {
            if (meal == null) {
                this.name = "N/A";
                this.ingredients = "N/A";
            } else {
                this.name = meal.getName() != null ? meal.getName() : "N/A";
                this.ingredients = meal.getIngredients() != null ? meal.getIngredients() : "N/A";
            }
        }

        public String getName() {
            return name;
        }

        public String getIngredients() {
            return ingredients;
        }
    }

    // ----- Constructor that builds itself -----
    public RestrictionCheckResultDTO(Patient patient, DailyOrder order, String message) {
        this.message = message;
        this.patient = patient.getName();
        this.status = order.getStatus();
        this.restrictions = patient.getRestriction();

        // Add meals in a logical order
        meals.put("breakfast", new MealInfo(order.getBreakfast()));
        meals.put("lunch", new MealInfo(order.getLunch()));
        meals.put("afternoonSnack", new MealInfo(order.getAfternoonSnack()));
        meals.put("dinner", new MealInfo(order.getDinner()));
        meals.put("nightSnack", new MealInfo(order.getNightSnack()));
    }

    // ----- Getters -----
    public String getMessage() {
        return message;
    }

    public String getPatient() {
        return patient;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getRestrictions() {
        return restrictions;
    }

    public Map<String, MealInfo> getMeals() {
        return meals;
    }
}
