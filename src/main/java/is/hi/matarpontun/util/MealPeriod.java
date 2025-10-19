package is.hi.matarpontun.util;

import is.hi.matarpontun.model.Menu;
import is.hi.matarpontun.model.Meal;

import java.time.LocalTime;

public enum MealPeriod {
    // Getum fínstillt tímann
    BREAKFAST("Breakfast", LocalTime.of(0, 0), LocalTime.of(10, 0)),
    LUNCH("Lunch", LocalTime.of(10, 0), LocalTime.of(13, 0)),
    AFTERNOON_SNACK("AfternoonSnack", LocalTime.of(13, 0), LocalTime.of(17, 0)),
    DINNER("Dinner", LocalTime.of(17, 0), LocalTime.of(21, 0)),
    NIGHT_SNACK("NightSnack", LocalTime.of(21, 0), LocalTime.of(23, 59));

    private final String mealCategory; 
    private final LocalTime start;
    private final LocalTime end;

    
    MealPeriod(String mealCategory, LocalTime start, LocalTime end) {
        this.mealCategory = mealCategory;
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
    
   
    public String getMealCategory() {
        return mealCategory;
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