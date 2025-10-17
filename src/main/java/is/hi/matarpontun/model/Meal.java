package is.hi.matarpontun.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;


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
    @JsonIgnore
    @JoinColumn(name = "food_type_id")
    private FoodType foodType;

    public Meal() {
    }

    public Meal(String name, String ingredients, String category, FoodType foodType) {
        this.name = name;
        this.ingredients = ingredients;
        this.category = category;
        this.foodType = foodType;
    }

    //------ getters and setters ------//
    
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

    public FoodType getFoodType() {
        return foodType;
    }
    public void setFoodType(FoodType foodType) {
        this.foodType = foodType;
    }
}