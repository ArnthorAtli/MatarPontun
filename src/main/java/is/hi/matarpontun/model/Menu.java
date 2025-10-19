package is.hi.matarpontun.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "menus")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;    // one menu per foodType per day

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_type_id")
    private FoodType foodType;

    @OneToOne
    @JoinColumn(name = "breakfast_id")
    private Meal breakfast;

    @OneToOne
    @JoinColumn(name = "lunch_id")
    private Meal lunch;

    @OneToOne
    @JoinColumn(name = "afternoon_snack_id")
    private Meal afternoonSnack;

    @OneToOne
    @JoinColumn(name = "dinner_id")
    private Meal dinner;

    @OneToOne
    @JoinColumn(name = "midnight_snack_id")
    private Meal nightSnack;

    public Menu() {}

    public Menu(LocalDate date, FoodType foodType) {
        this.date = date;
        this.foodType = foodType;
    }

    // --- getters and setters ---
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Meal getBreakfast() {
        return breakfast;
    }
    public void setBreakfast(Meal breakfast) {
        this.breakfast = breakfast;
    }

    public Meal getLunch() {
        return lunch;
    }
    public void setLunch(Meal lunch) {
        this.lunch = lunch;
    }

    public Meal getAfternoonSnack() {
        return afternoonSnack;
    }
    public void setAfternoonSnack(Meal afternoonSnack) {
        this.afternoonSnack = afternoonSnack;
    }

    public Meal getDinner() {
        return dinner;
    }
    public void setDinner(Meal dinner) {
        this.dinner = dinner;
    }

    public Meal getNightSnack() {
        return nightSnack;
    }
    public void setNightSnack(Meal nightSnack) {
        this.nightSnack = nightSnack;
    }

    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public FoodType getFoodType() {
        return foodType;
    }
    public void setFoodType(FoodType foodType) {
        this.foodType = foodType;
    }

}
