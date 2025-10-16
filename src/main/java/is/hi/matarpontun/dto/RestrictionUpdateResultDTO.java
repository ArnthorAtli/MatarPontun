package is.hi.matarpontun.dto;

public record RestrictionUpdateResultDTO(
    String message,
    String newFoodTypeName,
    String nextMealName,
    String nextMealIngredients
) {}
