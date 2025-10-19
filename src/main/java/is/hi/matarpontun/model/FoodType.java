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

    private String typeName; // e.g. "A1"
    private String description; // e.g. "Almennt fæði"

    // One food type can have many menus (e.g. one per day)
    @OneToMany(mappedBy = "foodType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Menu> menus = new ArrayList<Menu>();

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

    public Menu getMenuOfTheDay() { return menuOfTheDay; }
    public void setMenuOfTheDay(Menu menuOfTheDay) { this.menuOfTheDay = menuOfTheDay; }

    public List<Menu> getMenus() { return menus; }
    public void setMenus(List<Menu> menus) { this.menus = menus; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
