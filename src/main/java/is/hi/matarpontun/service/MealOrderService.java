package is.hi.matarpontun.service;

import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.model.MealOrder;
import is.hi.matarpontun.model.Menu;
import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.repository.MealOrderRepository;
import is.hi.matarpontun.repository.MenuRepository;
import is.hi.matarpontun.repository.PatientRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MealOrderService {

    private final MealOrderRepository mealOrderRepository;
    private final MealService mealService;
    private final PatientRepository patientRepository;
    private final MenuRepository menuRepository;

    public MealOrderService(MealOrderRepository mealOrderRepository, MealService mealService, PatientRepository patientRepository, MenuRepository menuRepository) {
        this.mealOrderRepository = mealOrderRepository;
        this.mealService = mealService;
        this.patientRepository = patientRepository;
        this.menuRepository = menuRepository;
    }

    /**
     * UC2 - Generate Meal Orders for a list of patients (used manually or automatically)
     */
    public List<MealOrder> generateOrdersForPatients(List<Patient> patients) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        List<MealOrder> createdOrders = new ArrayList<>();

        for (Patient patient : patients) {
            if (patient.getFoodType() == null) continue;

            // 🔹 Find today’s menu for the patient’s food type
            var foodType = patient.getFoodType();
            var menu = menuRepository.findByFoodTypeAndDate(foodType, today).orElse(null);
            if (menu == null) continue;

            // 🔹 Select the correct meal based on current time
            Meal meal = selectMealFromMenu(menu);
            if (meal == null) continue;

            // 🔹 Create the MealOrder
            MealOrder order = new MealOrder();
            order.setOrderTime(now);
            order.setMealType(meal.getCategory());
            order.setMeal(meal);
            order.setPatient(patient);
            order.setMenu(menu);
            order.setFoodType(foodType);
            order.setStatus("PENDING");

            mealOrderRepository.save(order);
            createdOrders.add(order);
        }

        return createdOrders;
    }

    /**
     * Determine which meal to serve based on the current time of day.
     */
    private Meal selectMealFromMenu(Menu menu) {
        LocalTime now = LocalTime.now();

        if (now.isBefore(LocalTime.of(10, 0))) return menu.getBreakfast();
        if (now.isBefore(LocalTime.of(13, 0))) return menu.getLunch();
        if (now.isBefore(LocalTime.of(17, 0))) return menu.getAfternoonSnack();
        if (now.isBefore(LocalTime.of(21, 0))) return menu.getDinner();
        return menu.getNightSnack();
    }

    // þetta keyrir sjálfkrafa á sceduled tímum
    @Scheduled(cron = "0 0 7,12,18 * * *")
    public void generateMealOrdersForAllPatients() {
        List<Patient> allPatients = patientRepository.findAll();
        generateOrdersForPatients(allPatients);
        System.out.println("✅ Automatically generated orders for all wards");
    }
}