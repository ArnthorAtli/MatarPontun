package is.hi.matarpontun.service;

import is.hi.matarpontun.dto.OrderDTO;
import is.hi.matarpontun.model.*;
import is.hi.matarpontun.repository.FoodTypeRepository;
import is.hi.matarpontun.repository.MealOrderRepository;
import is.hi.matarpontun.repository.MenuRepository;
import is.hi.matarpontun.repository.PatientRepository;
import is.hi.matarpontun.service.PatientService;
import is.hi.matarpontun.util.MealPeriod;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MealOrderService {

    private final MealOrderRepository mealOrderRepository;
    private final PatientRepository patientRepository;
    private final MenuRepository menuRepository;
    private final FoodTypeRepository foodTypeRepository;
    private final PatientService patientService;

    public MealOrderService(MealOrderRepository mealOrderRepository,
                            PatientRepository patientRepository,
                            MenuRepository menuRepository,
                            FoodTypeRepository foodTypeRepository,
                            PatientService patientService) {
        this.mealOrderRepository = mealOrderRepository;
        this.patientRepository = patientRepository;
        this.menuRepository = menuRepository;
        this.foodTypeRepository = foodTypeRepository;
        this.patientService = patientService;
    }

    /**
     * UC1 – Ward staff manually orders a food type for one patient
     */
    // viljum bæta við user-friendly ResponseEntity skilaboð frá controller level seinna
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
        return buildMealOrder(patient, assigned, LocalDateTime.now());
    }


    /**
     * UC2 – Automatically generate meal orders for multiple patients
     */
    /*
    // viljum hafa DTO output
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


    public OrderDTO generateOrdersForWard(Ward ward) {
        LocalDate today = LocalDate.now();
        MealPeriod period = MealPeriod.current(LocalTime.now());

        // Cache today’s menus per food type
        Map<FoodType, Menu> menus = menuRepository.findAllByDate(today)
                .stream()
                .collect(Collectors.toMap(Menu::getFoodType, m -> m));

        // Group patients by room
        Map<String, List<Patient>> groupedByRoom =
                ward.getPatients().stream()
                        .collect(Collectors.groupingBy(p -> p.getRoom().getRoomNumber()));

        List<OrderDTO.RoomInfo> roomInfos = new ArrayList<>();

        // Iterate by room
        for (var entry : groupedByRoom.entrySet()) {
            List<OrderDTO.PatientInfo> patientInfos = new ArrayList<>();

            for (Patient patient : entry.getValue()) {
                FoodType type = patient.getFoodType();
                Menu menu = menus.get(type);
                if (type == null || menu == null) continue;

                Meal meal = period.getMealFromMenu(menu);
                if (meal == null) continue;

                // Reuse conflict check from PatientService
                if (patientService.checkMealForConflicts(meal, patient)) continue; // skip unsuitable meals

                MealOrder order = buildMealOrder(patient, type, menu, meal, period);
                mealOrderRepository.save(order);

                patientInfos.add(new OrderDTO.PatientInfo(
                        patient.getName(), type.getTypeName(), meal.getName()
                ));
            }

            roomInfos.add(new OrderDTO.RoomInfo(entry.getKey(), patientInfos));
        }

        return new OrderDTO(ward.getWardName(), roomInfos);
    }

    // þetta keyrir sjálfkrafa á sceduled tímum - SKOÐA TÍMA hvenær eldhúið vill fá miðana
    // bæta við simple log, t.d "Generated 45 orders at 13:00"
    @Scheduled(cron = "0 0 0,10,13,17,21 * * *")
    public void generateMealOrdersForAllPatients() { // aldrei kallað á
        List<Patient> allPatients = patientRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (Patient p : allPatients) {
            if (p.getFoodType() != null)
                buildMealOrder(p, p.getFoodType(), now);
        }
        System.out.println("Automatically generated orders for all wards at " + now);
    }

    // UC3 - Manually change the next meal's food type for a patient
    // erum hérna smá að mixa responsibilities?
    public String manuallyChangeNextMeal(Long patientId, String newFoodTypeName) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        FoodType newFoodType = foodTypeRepository.findByTypeNameIgnoreCase(newFoodTypeName)
                .orElseThrow(() -> new EntityNotFoundException("FoodType not found"));

        MealPeriod currentPeriod = MealPeriod.current(LocalTime.now());
        String currentMealType = currentPeriod.getMealCategory();

        // Try to find a pending order for the current period
        var nextOrderOpt = mealOrderRepository
                .findFirstByPatientAndStatusAndMealTypeOrderByOrderTimeDesc(
                        patient, "PENDING", currentMealType);

        if (nextOrderOpt.isPresent()) {
            MealOrder orderToUpdate = nextOrderOpt.get();

            // Replace with new foodType using the shared helper for consistency
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
        MealOrder newOrder = buildMealOrder(patient, newFoodType, LocalDateTime.now());
        mealOrderRepository.save(newOrder);
        return "No pending order found. Created new order for '" + newFoodTypeName + "'.";
    }

    // --- Helper for creating and saving orders ---
    private MealOrder buildMealOrder(Patient p, FoodType ft, Menu m, Meal meal, MealPeriod period) {
        MealOrder order = new MealOrder();
        order.setOrderTime(LocalDateTime.now());
        order.setMealType(period.getMealCategory());
        order.setPatient(p);
        order.setMeal(meal);
        order.setMenu(m);
        order.setFoodType(ft);
        order.setWardName(p.getWard().getWardName());
        order.setRoomNumber(p.getRoom().getRoomNumber());
        order.setStatus("PENDING");
        return mealOrderRepository.save(order);
    }

    // Overload for use when Menu & Meal aren’t yet known
    private MealOrder buildMealOrder(Patient p, FoodType ft, LocalDateTime time) {
        LocalDate today = LocalDate.now();
        Menu menu = menuRepository.findByFoodTypeAndDate(ft, today).orElse(null);
        if (menu == null) return null;

        MealPeriod period = MealPeriod.current(LocalTime.now());
        Meal meal = period.getMealFromMenu(menu);
        if (meal == null) return null;

        return buildMealOrder(p, ft, menu, meal, period);
    }

    /**
     * UC2 – Generate and return meal orders for one ward (grouped by room)
     */
     /*
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
    */

    /*
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
     */

}