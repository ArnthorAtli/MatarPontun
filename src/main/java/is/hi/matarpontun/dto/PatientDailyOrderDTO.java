package is.hi.matarpontun.dto;

import java.time.LocalDate;
import java.util.List;


public class PatientDailyOrderDTO {
    private Long patientId;
    private String name;
    private int age;
    private String wardName;
    private String roomNumber;
    private int bedNumber;
    private String foodType;
    private List<String> restrictions;
    private List<String> allergies;
    private LocalDate orderDate;
    private String status;
    private MealDTO meals;

    public PatientDailyOrderDTO() {
        // Default constructor for JSON serialization
    }

    public PatientDailyOrderDTO(
            Long patientId, String name, int age, String wardName, String roomNumber, int bedNumber,
            String foodType, List<String> restrictions, List<String> allergies,
            LocalDate orderDate, String status, MealDTO meals) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.wardName = wardName;
        this.roomNumber = roomNumber;
        this.bedNumber = bedNumber;
        this.foodType = foodType;
        this.restrictions = restrictions;
        this.allergies = allergies;
        this.orderDate = orderDate;
        this.status = status;
        this.meals = meals;
    }

    // Getters
    public Long getPatientId() { return patientId; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getWardName() { return wardName; }
    public String getRoomNumber() { return roomNumber; }
    public int getBedNumber() { return bedNumber; }
    public String getFoodType() { return foodType; }
    public List<String> getRestrictions() { return restrictions; }
    public List<String> getAllergies() { return allergies; }
    public LocalDate getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    public MealDTO getMeals() { return meals; }

    public static class MealDTO {
        private String breakfastName;
        private String breakfastIngredients;
        private String lunchName;
        private String lunchIngredients;
        private String afternoonSnackName;
        private String afternoonSnackIngredients;
        private String dinnerName;
        private String dinnerIngredients;
        private String nightSnackName;
        private String nightSnackIngredients;

        public MealDTO() {}

        public MealDTO(String breakfastName, String breakfastIngredients,
                       String lunchName, String lunchIngredients,
                       String afternoonSnackName, String afternoonSnackIngredients,
                       String dinnerName, String dinnerIngredients,
                       String nightSnackName, String nightSnackIngredients) {
            this.breakfastName = breakfastName;
            this.breakfastIngredients = breakfastIngredients;
            this.lunchName = lunchName;
            this.lunchIngredients = lunchIngredients;
            this.afternoonSnackName = afternoonSnackName;
            this.afternoonSnackIngredients = afternoonSnackIngredients;
            this.dinnerName = dinnerName;
            this.dinnerIngredients = dinnerIngredients;
            this.nightSnackName = nightSnackName;
            this.nightSnackIngredients = nightSnackIngredients;
        }

        // --- Getters ---
        public String getBreakfastName() { return breakfastName; }
        public String getBreakfastIngredients() { return breakfastIngredients; }
        public String getLunchName() { return lunchName; }
        public String getLunchIngredients() { return lunchIngredients; }
        public String getAfternoonSnackName() { return afternoonSnackName; }
        public String getAfternoonSnackIngredients() { return afternoonSnackIngredients; }
        public String getDinnerName() { return dinnerName; }
        public String getDinnerIngredients() { return dinnerIngredients; }
        public String getNightSnackName() { return nightSnackName; }
        public String getNightSnackIngredients() { return nightSnackIngredients; }
    }
}
