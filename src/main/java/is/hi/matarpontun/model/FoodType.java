package is.hi.matarpontun.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "food_types")
public class FoodType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String typeName; // e.g. "A1" -> vil rename to "lable"
    private String description; // e.g. "Almennt fæði"

    @OneToMany(mappedBy = "foodType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Menu> menus = new ArrayList<>();

    // To link a food type to a menu of the day
    // If you later plan to automatically rotate menus, it may be better to compute the “menu of the day” dynamically rather than storing it as a fixed DB relation
    // — but for UC2’s current stage, this design is totally fine and pragmatic.
    @OneToOne
    @JoinColumn(name = "menu_id") //
    private Menu menuOfTheDay;

    public FoodType() {}

    public FoodType(String typeName, String description) {
        this.typeName = typeName;
        this.description = description;
    }

    // --- getters and setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public Menu getMenuOfTheDay() { return menuOfTheDay; }
    public void setMenuOfTheDay(Menu menuOfTheDay) { this.menuOfTheDay = menuOfTheDay; }

    public List<Menu> getMenus() { return menus; }
    public void setMenus(List<Menu> menus) { this.menus = menus; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
