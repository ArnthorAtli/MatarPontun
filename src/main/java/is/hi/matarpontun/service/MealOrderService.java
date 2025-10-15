package is.hi.matarpontun.service;

import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.model.MealOrder;
import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.repository.MealOrderRepository;
import is.hi.matarpontun.repository.PatientRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MealOrderService {

    private final MealOrderRepository mealOrderRepository;
    private final MealService mealService;
    private final PatientRepository patientRepository;

    public MealOrderService(MealOrderRepository mealOrderRepository, MealService mealService, PatientRepository patientRepository) {
        this.mealOrderRepository = mealOrderRepository;
        this.mealService = mealService;
        this.patientRepository = patientRepository;
    }

    /**
     * Shared helper — generates MealOrder objects for given patients.
     * Used both by WardService (manual trigger) and scheduled jobs.
     */
    public List<MealOrder> generateOrdersForPatients(List<Patient> patients) {
        LocalDateTime now = LocalDateTime.now();
        List<MealOrder> createdOrders = new ArrayList<>();

        for (Patient patient : patients) {
            Meal meal = mealService.selectMealForPatient(patient);
            if (meal == null) continue;

            MealOrder order = new MealOrder();
            order.setOrderTime(now);
            order.setMealType(meal.getCategory());
            order.setMeal(meal);
            order.setPatient(patient);
            order.setMenu(meal.getFoodType().getMenuOfTheDay());
            order.setFoodType(meal.getFoodType());
            order.setStatus("PENDING");

            mealOrderRepository.save(order);
            createdOrders.add(order);
        }

        return createdOrders;
    }

    // þetta keyrir sjálfkrafa á sceduled tímum
    @Scheduled(cron = "0 0 7,12,18 * * *")
    public void generateMealOrdersForAllPatients() {
        List<Patient> allPatients = patientRepository.findAll();
        generateOrdersForPatients(allPatients);
        System.out.println("✅ Automatically generated orders for all wards");
    }
}