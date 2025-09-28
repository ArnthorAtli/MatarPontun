package is.hi.matarpontun.dto;
// is the “big picture” version of a ward, so it should expose everything relevant for UC8/UC11 (fetch all data for a ward), but without leaking sensitive stuff (like passwords).

import java.util.List;

public record WardFullDTO(String wardName, List<PatientMealDTO> patients) {}
