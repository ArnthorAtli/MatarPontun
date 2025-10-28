package is.hi.matarpontun.dto;

public record WardCreateRequestDTO(
        String wardName,
        String password,
        int numberOfRooms,
        int patientsPerRoom
) {}
