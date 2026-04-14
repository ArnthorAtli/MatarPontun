package is.hi.matarpontun.dto;

import java.util.List;

/**
 * Summarises all meal-slot conflicts for one patient within a ward order.
 *
 * @param patientName patient's display name
 * @param patientId   patient's database id
 * @param conflicts   list of per-slot conflict details
 * @param status      the resulting order status: "AUTO CHANGED" or "NEEDS MANUAL CHANGE"
 */
public record PatientConflictSummaryDTO(
        String patientName,
        Long patientId,
        List<SlotConflictDTO> conflicts,
        String status
) {}
