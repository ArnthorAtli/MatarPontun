package is.hi.matarpontun.dto;


import java.util.List;

public record OrderDTO (
        String wardName,
        List<RoomInfo> rooms
) {
    public record RoomInfo(
            String roomNumber,
            List<PatientInfo> patients
    ) {}

    public record PatientInfo(
            String name,
            String foodType,
            MealPlan meals
    ) {}

     public record MealPlan(
            String breakfast,
            String lunch,
            String afternoonSnack,
            String dinner,
            String nightSnack
    ) {}
}
