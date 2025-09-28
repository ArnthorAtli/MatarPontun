package is.hi.matarpontun.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "food_types")
public class FoodType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String typeName; // e.g. "A1"
    private String description; // e.g. "Almennt fæði"

    // One food type can be linked to many meals
    @OneToMany(mappedBy = "foodType")
    private List<Meal> meals;

    // To link a food type to a menu of the day
    @OneToOne
    @JoinColumn(name = "menu_id")
    private Menu menuOfTheDay;

    public FoodType() {}

    public FoodType(String typeName, String description) { // String description -> viljum við hafa?
        this.typeName = typeName;
        this.description = description;
    }

    // --- getters and setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public List<Meal> getMeals() { return meals; }
    public void setMeals(List<Meal> meals) { this.meals = meals; }

    public Menu getMenuOfTheDay() { return menuOfTheDay; }
    public void setMenuOfTheDay(Menu menuOfTheDay) { this.menuOfTheDay = menuOfTheDay; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
