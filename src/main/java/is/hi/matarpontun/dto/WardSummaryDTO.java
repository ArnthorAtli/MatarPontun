package is.hi.matarpontun.dto;

public record WardSummaryDTO(
        Long wardId,
        String wardName,
        int rooms,
        int patients
) {}
