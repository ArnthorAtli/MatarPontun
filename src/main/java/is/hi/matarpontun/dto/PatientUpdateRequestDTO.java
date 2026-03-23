package is.hi.matarpontun.dto;

import java.util.List;

public record PatientUpdateRequestDTO(
        String name,
        String foodTypeName,
        List<String> restrictions
) {}
