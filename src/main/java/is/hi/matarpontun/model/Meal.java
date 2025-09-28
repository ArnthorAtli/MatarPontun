package is.hi.matarpontun.model;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "meals")
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String ingredients;
    private String category;

    @ManyToOne
    @JoinColumn(name = "food_type_id")
    private FoodType foodType;

    private Meal getNextMeal(Menu menu) {
        LocalTime now = LocalTime.now();

        if (now.isBefore(LocalTime.of(9, 0))) {
            return menu.getBreakfast();
        } else if (now.isBefore(LocalTime.of(12, 0))) {
            return menu.getLunch();
        } else if (now.isBefore(LocalTime.of(15, 0))) {
            return menu.getAfternoonSnack();
        } else if (now.isBefore(LocalTime.of(19, 0))) {
            return menu.getDinner();
        } else if (now.isBefore(LocalTime.of(21, 30))) {
            return menu.getNightSnack();
        } else {
            // day ended → return tomorrow’s breakfast, or null for now
            return menu.getBreakfast();
        }
    }

    // Constructors
    public Meal() {
    }

    public Meal(String name, String ingredients, String category) {
        this.name = name;
        this.ingredients = ingredients;
        this.category = category;
    }
    
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String description) {
        this.ingredients = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}