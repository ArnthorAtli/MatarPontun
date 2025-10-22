package is.hi.matarpontun.service;

import is.hi.matarpontun.dto.OrderDTO;
import is.hi.matarpontun.model.*;
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
import java.util.stream.Collectors;

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

        // Restrict ordering to the patient's assigned food type only
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
    /*
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
     */
    public OrderDTO orderFoodForWard(Ward ward) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        MealPeriod currentPeriod = MealPeriod.current(now);

        List<Patient> patients = ward.getPatients();
        List<OrderDTO.RoomInfo> roomDTOs = new ArrayList<>();

        // Group patients by room number
        var groupedByRoom = patients.stream()
                .collect(Collectors.groupingBy(p -> p.getRoom().getRoomNumber()));

        for (var entry : groupedByRoom.entrySet()) {
            String roomNumber = entry.getKey();
            List<Patient> roomPatients = entry.getValue();

            List<OrderDTO.PatientInfo> patientInfos = new ArrayList<>();

            for (Patient patient : roomPatients) {
                FoodType foodType = patient.getFoodType();
                if (foodType == null) continue;

                // Find today’s menu for the patient’s food type
                Menu menu = menuRepository.findByFoodTypeAndDate(foodType, today).orElse(null);
                if (menu == null) continue;

                Meal meal = currentPeriod.getMealFromMenu(menu);
                if (meal == null) continue;

                // Create and save MealOrder using your shared helper
                MealOrder order = createAndSaveOrder(patient, foodType, "PENDING");
                if (order == null) continue;

                // Add to patient summary
                patientInfos.add(new OrderDTO.PatientInfo(
                        patient.getName(),
                        foodType.getTypeName(),
                        meal.getName()
                ));
            }

            roomDTOs.add(new OrderDTO.RoomInfo(roomNumber, patientInfos));
        }

        return new OrderDTO(ward.getWardName(), roomDTOs);
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

/*
    // þetta keyrir sjálfkrafa á sceduled tímum - SKOÐA TÍMA hvenær eldhúið vill fá miðana
    @Scheduled(cron = "0 0 0,10,13,17,21 * * *")
    public void generateMealOrdersForAllPatients() { // aldrei kallað á
        List<Patient> allPatients = patientRepository.findAll();
        generateOrdersForPatients(allPatients);
        System.out.println("Automatically generated orders for all wards");
    }*/

    //UC3 - Manually change the next meal's food type for a patient
    public String manuallyChangeNextMeal(Long patientId, String newFoodTypeName) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        FoodType newFoodType = foodTypeRepository.findByTypeNameIgnoreCase(newFoodTypeName)
                .orElseThrow(() -> new EntityNotFoundException("FoodType not found"));

        MealPeriod currentPeriod = MealPeriod.current(LocalTime.now());
        String currentMealType = currentPeriod.getMealCategory();

        // Try to find a pending order for the current period
        var nextOrderOpt = mealOrderRepository
                .findFirstByPatientAndStatusAndMealTypeOrderByOrderTimeDesc(patient, "PENDING", currentMealType);

        if (nextOrderOpt.isPresent()) {
            MealOrder orderToUpdate = nextOrderOpt.get();

            // ✅ Replace with new foodType using the shared helper for consistency
            Menu newMenu = menuRepository.findByFoodTypeAndDate(newFoodType, LocalDate.now())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No menu found for " + newFoodTypeName + " today"));

            Meal newMeal = currentPeriod.getMealFromMenu(newMenu);
            if (newMeal == null) {
                throw new RuntimeException("No '" + currentMealType + "' meal available for " + newFoodTypeName);
            }

            orderToUpdate.setFoodType(newFoodType);
            orderToUpdate.setMeal(newMeal);
            orderToUpdate.setStatus("MANUALLY_CHANGED");
            mealOrderRepository.save(orderToUpdate);

            return "Updated existing order #" + orderToUpdate.getId() +
                    " to food type '" + newFoodTypeName + "'.";
        }

        // No pending order found → create a new order instead
        MealOrder newOrder = createAndSaveOrder(patient, newFoodType, "MANUALLY_CREATED");
        if (newOrder != null) {
            return "No pending order found. Created new order for '" + newFoodTypeName + "'.";
        }

        // As a fallback, just update the patient’s default type
        patient.setFoodType(newFoodType);
        patientRepository.save(patient);
        return "No pending order found. Updated patient's default food type to '" + newFoodTypeName + "'.";
    }

    /**
     * UC2 – Generate and return meal orders for one ward (grouped by room)
     */
    public OrderDTO orderFoodForWard(Ward ward) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        MealPeriod currentPeriod = MealPeriod.current(now);

        List<Patient> patients = ward.getPatients();
        List<OrderDTO.RoomInfo> roomDTOs = new ArrayList<>();

        // Group patients by room number
        var groupedByRoom = patients.stream()
                .collect(Collectors.groupingBy(p -> p.getRoom().getRoomNumber()));

        for (var entry : groupedByRoom.entrySet()) {
            String roomNumber = entry.getKey();
            List<Patient> roomPatients = entry.getValue();
            List<OrderDTO.PatientInfo> patientInfos = new ArrayList<>();

            for (Patient patient : roomPatients) {
                FoodType foodType = patient.getFoodType();
                if (foodType == null) continue;

                // Find today's menu for the patient's food type
                Menu menu = menuRepository.findByFoodTypeAndDate(foodType, today).orElse(null);
                if (menu == null) continue;

                Meal meal = currentPeriod.getMealFromMenu(menu);
                if (meal == null) continue;

                // Create and save a MealOrder (using your existing helper)
                MealOrder order = createAndSaveOrder(patient, foodType, "PENDING");
                if (order == null) continue;

                patientInfos.add(new OrderDTO.PatientInfo(
                        patient.getName(),
                        foodType.getTypeName(),
                        meal.getName()
                ));
            }

            roomDTOs.add(new OrderDTO.RoomInfo(roomNumber, patientInfos));
        }

        return new OrderDTO(ward.getWardName(), roomDTOs);
    }

}