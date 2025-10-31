package is.hi.matarpontun.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "daily_orders",
        uniqueConstraints = @UniqueConstraint(columnNames = {"patient_id", "order_date"})
)
public class DailyOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    private String status = "SUBMITTED";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_type_id")
    private FoodType foodType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breakfast_meal_id")
    private Meal breakfast;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lunch_meal_id")
    private Meal lunch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "afternoon_snack_meal_id")
    private Meal afternoonSnack;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dinner_meal_id")
    private Meal dinner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "night_snack_meal_id")
    private Meal nightSnack;

    private String wardName;
    private String roomNumber;

    public DailyOrder() {
    }

    public DailyOrder(LocalDate orderDate,
                      Patient patient,
                      Menu menu,
                      FoodType foodType,
                      Meal breakfast,
                      Meal lunch,
                      Meal afternoonSnack,
                      Meal dinner,
                      Meal nightSnack) {
        this.orderDate = orderDate;
        this.patient = patient;
        this.menu = menu;
        this.foodType = foodType;
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.afternoonSnack = afternoonSnack;
        this.dinner = dinner;
        this.nightSnack = nightSnack;
        if (patient.getWard() != null) {
            this.wardName = patient.getWard().getWardName();
        }
        if (patient.getRoom() != null) {
            this.roomNumber = patient.getRoom().getRoomNumber();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public FoodType getFoodType() {
        return foodType;
    }

    public void setFoodType(FoodType foodType) {
        this.foodType = foodType;
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

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
}