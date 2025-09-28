package is.hi.matarpontun.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "menus")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.DATE) //timestamp?
    private Date date;

    @OneToOne(mappedBy = "menuOfTheDay")
    private FoodType foodType;

    //sleppa?
    /*@ManyToOne
    @JoinColumn(name = "meal_id")
    private Meal meal;
     */

    @ManyToOne
    @JoinColumn(name = "breakfast_id")
    private Meal breakfast;

    @ManyToOne
    @JoinColumn(name = "lunch_id")
    private Meal lunch;

    @ManyToOne
    @JoinColumn(name = "afternoonSnack_id")
    private Meal afternoonSnack;

    @ManyToOne
    @JoinColumn(name = "dinner_id")
    private Meal dinner;

    @ManyToOne
    @JoinColumn(name = "midnightSnack_id")
    private Meal nightSnack;

    //Getters and setter:-------------------
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    /*public Meal getMeal() {
        return meal;
    }
    public void setMeal(Meal meal) {
        this.meal = meal;
    }*/
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

}
