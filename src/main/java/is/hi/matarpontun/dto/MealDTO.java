package is.hi.matarpontun.dto;

public record MealDTO(
    String name,
    String ingredients,
    String category,
    Long foodTypeId
) {}
