package is.hi.matarpontun.service;

import is.hi.matarpontun.model.FoodType;
import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.model.MealOrder;
import is.hi.matarpontun.model.Menu;
import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.repository.FoodTypeRepository;
import is.hi.matarpontun.repository.MealOrderRepository;
import is.hi.matarpontun.repository.MenuRepository;
import is.hi.matarpontun.repository.PatientRepository;
import is.hi.matarpontun.util.MealPeriod;
import jakarta.persistence.EntityNotFoundException;

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
    private final FoodTypeRepository foodTypeRepository;

    public MealOrderService(MealOrderRepository mealOrderRepository, PatientRepository patientRepository, MenuRepository menuRepository, FoodTypeRepository foodTypeRepository) {
        this.mealOrderRepository = mealOrderRepository;
        this.patientRepository = patientRepository;
        this.menuRepository = menuRepository;
        this.foodTypeRepository = foodTypeRepository;
    }

    /**
     * UC1 – Ward staff manually orders a food type for one patient
     */
    public MealOrder orderFoodTypeForPatient(Long patientId, String foodTypeName) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        FoodType assigned = patient.getFoodType();
        if (assigned == null) {
            throw new IllegalStateException("Patient has no assigned food type");
        }

        // ✅ Restrict ordering to the patient's assigned food type only
        if (!assigned.getTypeName().equalsIgnoreCase(foodTypeName)) {
            throw new IllegalArgumentException(
                    "Patient " + patient.getName() +
                            " is assigned to '" + assigned.getTypeName() +
                            "' and cannot order '" + foodTypeName + "'");
        }

        return createAndSaveOrder(patient, assigned, "SENT_TO_KITCHEN");
    }


    /**
     * UC2 – Automatically generate meal orders for multiple patients
     */
    public List<MealOrder> generateOrdersForPatients(List<Patient> patients) {
        LocalDateTime now = LocalDateTime.now();
        List<MealOrder> createdOrders = new ArrayList<>();

        for (Patient patient : patients) {
            FoodType foodType = patient.getFoodType();
            if (foodType == null) continue; // skip patients with no diet

            MealOrder order = createAndSaveOrder(patient, foodType, "PENDING", now);
            if (order != null) createdOrders.add(order);
        }

        return createdOrders;
    }

    /**
     * 🔒 Private helper: central logic for creating MealOrder entries
     */
    private MealOrder createAndSaveOrder(Patient patient, FoodType foodType, String status) {
        return createAndSaveOrder(patient, foodType, status, LocalDateTime.now());
    }

    private MealOrder createAndSaveOrder(Patient patient, FoodType foodType, String status, LocalDateTime orderTime) {
        LocalDate today = LocalDate.now();
        Menu menu = menuRepository.findByFoodTypeAndDate(foodType, today)
                .orElse(null);
        if (menu == null) return null;

        MealPeriod currentPeriod = MealPeriod.current(LocalTime.now());
        Meal meal = currentPeriod.getMealFromMenu(menu);
        if (meal == null) return null;

        MealOrder order = new MealOrder();
        order.setOrderTime(orderTime);
        order.setMealType(meal.getCategory());
        order.setMeal(meal);
        order.setPatient(patient);
        order.setMenu(menu);
        order.setFoodType(foodType);
        order.setStatus(status);

        return mealOrderRepository.save(order);
    }


    // þetta keyrir sjálfkrafa á sceduled tímum - SKOÐA TÍMA hvenær eldhúið vill fá miðana
    @Scheduled(cron = "0 0 0,10,13,17,21 * * *")
    public void generateMealOrdersForAllPatients() { // aldrei kallað á
        List<Patient> allPatients = patientRepository.findAll();
        generateOrdersForPatients(allPatients);
        System.out.println("Automatically generated orders for all wards");
    }

    //UC3 - Manually change the next meal's food type for a patient
    public String manuallyChangeNextMeal(Long patientId, String newFoodTypeName) {
        // Step 1: Find the patient and the new food type.
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient with ID " + patientId + " not found."));
        FoodType newFoodType = foodTypeRepository.findByTypeName(newFoodTypeName)
                .orElseThrow(() -> new EntityNotFoundException("FoodType '" + newFoodTypeName + "' not found."));

        // Step 2: Determine the current meal period.
        MealPeriod currentPeriod = MealPeriod.current(LocalTime.now());
        String currentMealType = currentPeriod.getMealCategory();

        // Step 3: Try to find an existing pending order.
        var nextOrderOpt = mealOrderRepository.findNextPendingOrder(patient, "PENDING", currentMealType);

        // SCENARIO A: A pending order was found. Update it directly.
        if (nextOrderOpt.isPresent()) {
            MealOrder orderToUpdate = nextOrderOpt.get();
            Menu newMenu = menuRepository.findByFoodTypeAndDate(newFoodType, LocalDate.now())
                    .orElseThrow(() -> new EntityNotFoundException("No menu found for food type '" + newFoodTypeName + "' for today."));
            Meal newMeal = currentPeriod.getMealFromMenu(newMenu);
            if (newMeal == null) {
                throw new RuntimeException("No '" + currentMealType + "' meal is available in the menu for food type '" + newFoodTypeName + "'.");
            }

            orderToUpdate.setFoodType(newFoodType);
            orderToUpdate.setMeal(newMeal);
            orderToUpdate.setStatus("MANUALLY_CHANGED");
            mealOrderRepository.save(orderToUpdate);
            return "Successfully updated existing order #" + orderToUpdate.getId() + " to food type '" + newFoodTypeName + "'.";
        }
        
        // SCENARIO B: No pending order was found. Update the patient's default diet instead.
        else {
            patient.setFoodType(newFoodType);
            patientRepository.save(patient);
            return "No pending order found. Updated patient's default diet to '" + newFoodTypeName + "'.";
        }
    }
    /* // til að eiga óbreytta aðferð:
    public List<MealOrder> generateOrdersForPatients(List<Patient> patients) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        List<MealOrder> createdOrders = new ArrayList<>();

        // held ég vilji hér kalla á aðferðina
        for (Patient patient : patients) {
            if (patient.getFoodType() == null) continue; // ef ekki skráð FoodType -> ekkert gert

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

     */
}