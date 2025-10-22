package is.hi.matarpontun.dto;

import is.hi.matarpontun.model.Meal;

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
            String nextMeal
    ) {}
}
