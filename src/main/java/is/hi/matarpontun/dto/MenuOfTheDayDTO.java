package is.hi.matarpontun.dto;

import java.time.LocalDate;

public class MenuOfTheDayDTO {

    private LocalDate date;
    private String breakfast;
    private String lunch;
    private String afternoonSnack;
    private String dinner;
    private String midnightSnack;

    public MenuOfTheDayDTO() {}

    public MenuOfTheDayDTO(LocalDate date, String breakfast, String lunch,
                           String afternoonSnack, String dinner, String midnightSnack) {
        this.date = date;
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.afternoonSnack = afternoonSnack;
        this.dinner = dinner;
        this.midnightSnack = midnightSnack;
    }

    // --- Getters and Setters
    public LocalDate getDate() { return date; }
    public String getBreakfast() { return breakfast; }
    public String getLunch() { return lunch; }
    public String getAfternoonSnack() { return afternoonSnack; }
    public String getDinner() { return dinner; }
    public String getMidnightSnack() { return midnightSnack; }

    public void setDate(LocalDate date) { this.date = date; }
    public void setBreakfast(String breakfast) { this.breakfast = breakfast; }
    public void setLunch(String lunch) { this.lunch = lunch; }
    public void setAfternoonSnack(String afternoonSnack) { this.afternoonSnack = afternoonSnack; }
    public void setDinner(String dinner) { this.dinner = dinner; }
    public void setMidnightSnack(String midnightSnack) { this.midnightSnack = midnightSnack; }
}
