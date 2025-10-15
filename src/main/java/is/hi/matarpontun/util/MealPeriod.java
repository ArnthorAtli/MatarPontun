package is.hi.matarpontun.util;

import is.hi.matarpontun.model.Menu;
import is.hi.matarpontun.model.Meal;

import java.time.LocalTime;

public enum MealPeriod {
    // Getum fínstillt tímann
    BREAKFAST(LocalTime.of(0, 0), LocalTime.of(10, 0)),
    LUNCH(LocalTime.of(10, 0), LocalTime.of(13, 0)),
    AFTERNOON_SNACK(LocalTime.of(13, 0), LocalTime.of(17, 0)),
    DINNER(LocalTime.of(17, 0), LocalTime.of(21, 0)),
    NIGHT_SNACK(LocalTime.of(21, 0), LocalTime.of(23, 59));

    private final LocalTime start;
    private final LocalTime end;

    MealPeriod(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }

    /** Determine the current meal period based on the time */
    public static MealPeriod current(LocalTime now) {
        for (MealPeriod p : values()) {
            if (!now.isBefore(p.start) && now.isBefore(p.end)) {
                return p;
            }
        }
        return BREAKFAST;
    }

    /** Return the corresponding meal from the menu */
    public Meal getMealFromMenu(Menu menu) {
        return switch (this) {
            case BREAKFAST -> menu.getBreakfast();
            case LUNCH -> menu.getLunch();
            case AFTERNOON_SNACK -> menu.getAfternoonSnack();
            case DINNER -> menu.getDinner();
            case NIGHT_SNACK -> menu.getNightSnack();
        };
    }
}
