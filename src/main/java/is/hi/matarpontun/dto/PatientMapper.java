package is.hi.matarpontun.dto;

import is.hi.matarpontun.model.*;

public class PatientMapper {

    private PatientMapper() {
        // Prevent instantiation — only static methods
    }

    /**
     * Converts a Patient, Ward, and DailyOrder into a DTO
     * containing meal names, ingredients, and restriction info.
     */
    public static PatientDailyOrderDTO toDailyOrderDTO(Patient patient, Ward ward, DailyOrder order) {
        return new PatientDailyOrderDTO(
                patient.getPatientID(),
                patient.getName(),
                patient.getAge(),
                ward != null ? ward.getWardName() : null,
                patient.getRoom() != null ? patient.getRoom().getRoomNumber() : null,
                patient.getBedNumber(),
                patient.getFoodType() != null ? patient.getFoodType().getTypeName() : null,
                patient.getRestriction(),
                patient.getAllergies(),
                order.getOrderDate(),
                order.getStatus(),
                new PatientDailyOrderDTO.MealDTO(
                        safeMealName(order.getBreakfast()),
                        safeIngredients(order.getBreakfast()),
                        safeMealName(order.getLunch()),
                        safeIngredients(order.getLunch()),
                        safeMealName(order.getAfternoonSnack()),
                        safeIngredients(order.getAfternoonSnack()),
                        safeMealName(order.getDinner()),
                        safeIngredients(order.getDinner()),
                        safeMealName(order.getNightSnack()),
                        safeIngredients(order.getNightSnack())
                )
        );
    }

    // --- Helper Methods ---
    private static String safeMealName(Meal meal) {
        return (meal != null && meal.getName() != null) ? meal.getName() : "N/A";
    }

    private static String safeIngredients(Meal meal) {
        return (meal != null && meal.getIngredients() != null) ? meal.getIngredients() : "N/A";
    }
}
