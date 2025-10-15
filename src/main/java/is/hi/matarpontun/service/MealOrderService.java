package is.hi.matarpontun.service;

import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.model.MealOrder;
import is.hi.matarpontun.model.Menu;
import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.repository.MealOrderRepository;
import is.hi.matarpontun.repository.MenuRepository;
import is.hi.matarpontun.repository.PatientRepository;
import is.hi.matarpontun.util.MealPeriod;
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
    private final PatientRepository patientRepository;
    private final MenuRepository menuRepository;

    public MealOrderService(MealOrderRepository mealOrderRepository, PatientRepository patientRepository, MenuRepository menuRepository) {
        this.mealOrderRepository = mealOrderRepository;
        this.patientRepository = patientRepository;
        this.menuRepository = menuRepository;
    }

    /**
     * UC2 - Generate Meal Orders for a list of patients (used manually or automatically)
     * This will be sent to the kitchen i think
     */
    public List<MealOrder> generateOrdersForPatients(List<Patient> patients) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        List<MealOrder> createdOrders = new ArrayList<>();

        for (Patient patient : patients) {
            if (patient.getFoodType() == null) continue;

            var foodType = patient.getFoodType();
            Menu menu = menuRepository.findByFoodTypeAndDate(foodType, today).orElse(null);
            if (menu == null) continue;

            // Use the shared enum logic
            MealPeriod period = MealPeriod.current(LocalTime.now());
            Meal meal = period.getMealFromMenu(menu);
            if (meal == null) continue;

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

    // þetta keyrir sjálfkrafa á sceduled tímum - SKOÐA TÍMA hvenær eldhúið vill fá miðana
    @Scheduled(cron = "0 0 0,10,13,17,21 * * *")
    public void generateMealOrdersForAllPatients() {
        List<Patient> allPatients = patientRepository.findAll();
        generateOrdersForPatients(allPatients);
        System.out.println("Automatically generated orders for all wards");
    }
}