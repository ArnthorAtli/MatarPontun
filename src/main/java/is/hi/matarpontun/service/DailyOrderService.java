package is.hi.matarpontun.service;

import is.hi.matarpontun.dto.OrderDTO;
import is.hi.matarpontun.model.*;
import is.hi.matarpontun.repository.DailyOrderRepository;
import is.hi.matarpontun.repository.FoodTypeRepository;
import is.hi.matarpontun.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DailyOrderService {

    private final DailyOrderRepository dailyOrderRepository;
    private final PatientRepository patientRepository;
    private final FoodTypeRepository foodTypeRepository;

    public DailyOrderService(DailyOrderRepository dailyOrderRepository,
            PatientRepository patientRepository,
            FoodTypeRepository foodTypeRepository) {
        this.dailyOrderRepository = dailyOrderRepository;
        this.patientRepository = patientRepository;
        this.foodTypeRepository = foodTypeRepository;
    }

    /**
     * UC1 – Ward staff manually orders a food type for one patient
     * Ensures a DailyOrder exists for the patient for the current day.
     */
    public DailyOrder orderFoodTypeForPatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        LocalDate today = LocalDate.now();

        // if the patient already has an order today, delete it first and assign a new
        // one in case food type/menu/restrictions changed
        Optional<DailyOrder> existingOrderOpt = dailyOrderRepository.findByPatientAndOrderDate(patient, today);
        if (existingOrderOpt.isPresent()) {
            DailyOrder existingOrder = existingOrderOpt.get();
            System.out
                    .println("Existing DailyOrder found for " + patient.getName() + " on " + today + " — deleting it.");
            dailyOrderRepository.delete(existingOrder);
        }

        FoodType foodType = patient.getFoodType();
        if (foodType == null) {
            throw new IllegalStateException("Patient has no assigned food type");
        }

        Menu menuOfTheDay = foodType.getMenuOfTheDay();
        if (menuOfTheDay == null) {
            throw new EntityNotFoundException(
                    "No menu of the day assigned for food type '" + foodType.getTypeName() + "'");
        }

        // Create a new DailyOrder
        DailyOrder order = new DailyOrder();
        order.setPatient(patient);
        order.setOrderDate(LocalDate.now());
        order.setMenu(menuOfTheDay);
        order.setFoodType(foodType);
        order.setBreakfast(menuOfTheDay.getBreakfast());
        order.setLunch(menuOfTheDay.getLunch());
        order.setAfternoonSnack(menuOfTheDay.getAfternoonSnack());
        order.setDinner(menuOfTheDay.getDinner());
        order.setNightSnack(menuOfTheDay.getNightSnack());
        order.setWardName(patient.getWard() != null ? patient.getWard().getWardName() : null);
        order.setRoomNumber(patient.getRoom() != null ? patient.getRoom().getRoomNumber() : null);
        order.setStatus("SUBMITTED");

        // Save it before applying restrictions
        dailyOrderRepository.save(order);

        // Check restrictions (stubbed for now)
        checkForRestrictions(order);

        // Save again if restrictions modified meals/status
        return dailyOrderRepository.save(order);
    }

    public OrderDTO generateOrdersForWard(Ward ward) {
        List<OrderDTO.RoomInfo> roomInfos = new ArrayList<>();

        // Loop through each room in the ward
        for (Room room : ward.getRooms()) {
            List<OrderDTO.PatientInfo> patientInfos = new ArrayList<>();

            // Loop through each patient in the room
            for (Patient patient : room.getPatients()) {
                Long id = patient.getPatientID();
                if (id == null) {
                    continue;
                }

                try {
                    DailyOrder order = orderFoodTypeForPatient(id);

                    // Map to DTO info
                    patientInfos.add(new OrderDTO.PatientInfo(
                            patient.getName(),
                            order.getFoodType() != null ? order.getFoodType().getTypeName() : "",
                            new OrderDTO.MealPlan(
                                    mealName(order.getBreakfast()),
                                    mealName(order.getLunch()),
                                    mealName(order.getAfternoonSnack()),
                                    mealName(order.getDinner()),
                                    mealName(order.getNightSnack()))));

                } catch (Exception e) {
                    System.err.println(
                            "Could not generate order for patient " + patient.getName() + ": " + e.getMessage());
                }
            }

            // Add room info if there are any patients
            if (!patientInfos.isEmpty()) {
                roomInfos.add(new OrderDTO.RoomInfo(room.getRoomNumber(), patientInfos));
            }
        }

        return new OrderDTO(ward.getWardName(), roomInfos);
    }

    public DailyOrder checkForConflicts(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        LocalDate today = LocalDate.now();

        DailyOrder order = dailyOrderRepository.findByPatientAndOrderDate(patient, today)
                .orElseThrow(() -> new EntityNotFoundException("No daily order found for today"));

        checkForRestrictions(order);
        return dailyOrderRepository.save(order);
    }

    private String mealName(Meal meal) {
        return meal != null ? meal.getName() : "N/A";
    }

    // -----------------------HELPER FUNCTIONS-------------------------

    // Checks if any of the meals in the order conflict with patient's restrictions
    private void checkForRestrictions(DailyOrder order) {
        Patient patient = order.getPatient();

        // Parse restriction string (e.g. "milk, nuts, gluten")
        String restrictionString = String.join(",", patient.getRestriction());
        if (restrictionString == null || restrictionString.isBlank()) {
            return; // No restrictions, keep as SUBMITTED
        }

        List<String> restrictions = List.of(restrictionString.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .toList();

        boolean autoChanged = false;
        boolean needsManual = false;

        // Helper to detect conflicts
        java.util.function.Function<Meal, Boolean> hasConflict = meal -> {
            if (meal == null || meal.getIngredients() == null)
                return false;
            String ingredients = meal.getIngredients().toLowerCase();
            return restrictions.stream().anyMatch(ingredients::contains);
        };

        // Check each meal and replace if necessary
        if (hasConflict.apply(order.getBreakfast())) {
            Meal replacement = findSafeAlternative(order.getFoodType(), "breakfast", restrictions);
            if (replacement != null) {
                order.setBreakfast(replacement);
                autoChanged = true;
            } else {
                needsManual = true;
            }
        }

        if (hasConflict.apply(order.getLunch())) {
            Meal replacement = findSafeAlternative(order.getFoodType(), "lunch", restrictions);
            if (replacement != null) {
                order.setLunch(replacement);
                autoChanged = true;
            } else {
                needsManual = true;
            }
        }

        if (hasConflict.apply(order.getAfternoonSnack())) {
            Meal replacement = findSafeAlternative(order.getFoodType(), "afternoonsnack", restrictions);
            if (replacement != null) {
                order.setAfternoonSnack(replacement);
                autoChanged = true;
            } else {
                needsManual = true;
            }
        }

        if (hasConflict.apply(order.getDinner())) {
            Meal replacement = findSafeAlternative(order.getFoodType(), "dinner", restrictions);
            if (replacement != null) {
                order.setDinner(replacement);
                autoChanged = true;
            } else {
                needsManual = true;
            }
        }

        if (hasConflict.apply(order.getNightSnack())) {
            Meal replacement = findSafeAlternative(order.getFoodType(), "nightsnack", restrictions);
            if (replacement != null) {
                order.setNightSnack(replacement);
                autoChanged = true;
            } else {
                needsManual = true;
            }
        }

        // Set final status
        if (needsManual) {
            order.setStatus("NEEDS MANUAL CHANGE");
        } else if (autoChanged) {
            order.setStatus("AUTO CHANGED");
        } else {
            order.setStatus("SUBMITTED");
        }

        System.out.println("Checked restrictions for " + patient.getName()
                + " → " + order.getStatus());
    }

    // Reynir að finna máltíð sem passar fyrir takmarkanir, annars skilar null

    private Meal findSafeAlternative(FoodType currentFoodType, String category, List<String> restrictions) {
        // --- Define food type groups ---
        List<List<String>> groups = List.of(
                List.of("A1", "A2", "A3", "OP", "RDS-KF", "RDS-G"),
                List.of("M1", "M2", "M3"),
                List.of("F1", "F1-S", "F1-M", "F2", "F3", "F4", "F4-S", "F5"));

        String currentTypeName = currentFoodType.getTypeName();
        Optional<List<String>> currentGroupOpt = groups.stream()
                .filter(g -> g.contains(currentTypeName))
                .findFirst();

        if (currentGroupOpt.isEmpty()) {
            // Needs manual change
            return null;
        }

        List<String> currentGroup = currentGroupOpt.get();

        List<FoodType> foodTypesInGroup = foodTypeRepository.findAll().stream()
                .filter(ft -> currentGroup.contains(ft.getTypeName()))
                .toList();

        // --- Check menus for same group ---
        for (FoodType ft : foodTypesInGroup) {
            Menu menu = ft.getMenuOfTheDay();
            if (menu == null)
                continue;

            Meal candidate = switch (category.toLowerCase()) {
                case "breakfast" -> menu.getBreakfast();
                case "lunch" -> menu.getLunch();
                case "afternoonsnack" -> menu.getAfternoonSnack();
                case "dinner" -> menu.getDinner();
                case "nightsnack" -> menu.getNightSnack();
                default -> null;
            };

            if (candidate == null || candidate.getIngredients() == null)
                continue;

            String ingredients = candidate.getIngredients().toLowerCase();
            boolean hasConflict = restrictions.stream().anyMatch(ingredients::contains);

            if (!hasConflict) {
                System.out.println("Auto-changed " + category + " for " + currentTypeName +
                        " → using " + ft.getTypeName() + " menu of the day meal: " + candidate.getName());
                return candidate;
            }
        }

        // --- No safe match found ---
        System.out.println(
                "No safe alternative found for " + currentTypeName + " " + category + " → manual change required.");
        return null;
    }

    // Finds today's order for a patient, if any
    public DailyOrder findTodayOrderForPatient(Patient patient) {
        return dailyOrderRepository.findByPatientAndOrderDate(patient, LocalDate.now()).orElseGet(() -> orderFoodTypeForPatient(patient.getPatientID()));
    }

}