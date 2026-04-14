package is.hi.matarpontun.dto;

/**
 * Describes a single meal-slot conflict found during restriction checking.
 *
 * @param slot              the meal slot name (e.g. "Breakfast")
 * @param originalMeal      the name of the conflicting meal
 * @param matchedRestriction the patient restriction that triggered the conflict
 * @param replacementMeal   the auto-assigned replacement meal name,
 *                          or {@code null} if manual intervention is required
 */
public record SlotConflictDTO(
        String slot,
        String originalMeal,
        String matchedRestriction,
        String replacementMeal
) {}
