package is.hi.matarpontun.service;

import is.hi.matarpontun.dto.DailyOrderSummaryDTO;
import is.hi.matarpontun.dto.OrderDTO;
import is.hi.matarpontun.dto.PatientConflictSummaryDTO;
import is.hi.matarpontun.dto.SlotConflictDTO;
import is.hi.matarpontun.model.*;
import is.hi.matarpontun.repository.DailyOrderRepository;
import is.hi.matarpontun.repository.FoodTypeRepository;
import is.hi.matarpontun.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DailyOrderService {

    private final DailyOrderRepository dailyOrderRepository;
    private final PatientRepository patientRepository;
    private final FoodTypeRepository foodTypeRepository;

    /** Wraps a saved order together with any slot conflicts found during restriction checking. */
    public record OrderResult(DailyOrder order, List<SlotConflictDTO> conflicts) {}

    /** Wraps the ward-level OrderDTO together with per-patient conflict summaries. */
    public record WardOrderResult(OrderDTO orderDTO, List<PatientConflictSummaryDTO> conflicts) {}

    /**
     * Constructs a new {@code DailyOrderService} with required repositories.
     *
     * @param dailyOrderRepository repository for persisting and querying
     *                             {@link DailyOrder} entities
     * @param patientRepository    repository for accessing {@link Patient} entities
     * @param foodTypeRepository   repository for accessing {@link FoodType}
     *                             entities
     */
    public DailyOrderService(DailyOrderRepository dailyOrderRepository,
            PatientRepository patientRepository,
            FoodTypeRepository foodTypeRepository) {
        this.dailyOrderRepository = dailyOrderRepository;
        this.patientRepository = patientRepository;
        this.foodTypeRepository = foodTypeRepository;
    }

    /**
     * UC1 - Manually orders a food type for one patient for today.
     * 
     * Ensures that a {@link DailyOrder} exists for the patient on the current date.
     *
     * @param patientId the patient's id
     * @return the saved {@link DailyOrder} after restriction checks.
     * @throws EntityNotFoundException if the patient does not exist or no menu of
     *                                 the day is assigned.
     * @throws IllegalStateException   if the patient has no assigned
     *                                 {@link FoodType}.
     */
    @Transactional
    public OrderResult orderFoodTypeForPatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        LocalDate today = LocalDate.now();

        // if the patient already has an order today, delete it first and assign
        // a new one in case food type/menu/restrictions changed
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

        // Save before restriction check so the entity is managed
        dailyOrderRepository.save(order);

        // Check restrictions — captures which slots conflicted and what was done
        List<SlotConflictDTO> conflicts = checkForRestrictions(order);

        // Save again to persist any meal swaps and updated status
        DailyOrder saved = dailyOrderRepository.save(order);
        return new OrderResult(saved, conflicts);
    }

    /**
     * Generates today's orders for all patients in a ward and maps the result into
     * a structured DTO grouped by rooms and patients.
     *
     * @param ward the ward to process
     * @return an {@link OrderDTO} containing patient orders based on rooms
     */
    public WardOrderResult generateOrdersForWard(Ward ward) {
        List<OrderDTO.RoomInfo> roomInfos = new ArrayList<>();
        List<PatientConflictSummaryDTO> allConflicts = new ArrayList<>();

        for (Room room : ward.getRooms()) {
            List<OrderDTO.PatientInfo> patientInfos = new ArrayList<>();

            for (Patient patient : room.getPatients()) {
                Long id = patient.getPatientID();
                if (id == null) continue;

                try {
                    OrderResult result = orderFoodTypeForPatient(id);
                    DailyOrder order = result.order();

                    // Collect conflicts for patients that had any
                    if (!result.conflicts().isEmpty()) {
                        allConflicts.add(new PatientConflictSummaryDTO(
                                patient.getName(), id, result.conflicts(), order.getStatus()));
                    }

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

            if (!patientInfos.isEmpty()) {
                roomInfos.add(new OrderDTO.RoomInfo(room.getRoomNumber(), patientInfos));
            }
        }

        return new WardOrderResult(new OrderDTO(ward.getWardName(), roomInfos), allConflicts);
    }

    /**
     * Checks a patient's order for conflicts against their restrictions and updates
     * the order's meals and status as needed.
     *
     * @param patientId the patient's id
     * @return the updated {@link DailyOrder}
     * @throws EntityNotFoundException if the patient or today's order does not
     *                                 exist.
     */
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

    /**
     * UC13 - Deletes today's order for a given patient.
     *
     * @param patientId the patient's id
     * @return {@code true} if an order existed and was deleted, {@code false}
     *         otherwise
     * @throws EntityNotFoundException if the patient does not exist
     */
    public boolean deleteTodaysOrderForPatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        LocalDate today = LocalDate.now();

        Optional<DailyOrder> existingOrderOpt = dailyOrderRepository.findByPatientAndOrderDate(patient, today);

        if (existingOrderOpt.isPresent()) {
            dailyOrderRepository.delete(existingOrderOpt.get());
            System.out.println("Deleted today's order for patient " + patient.getName());
            return true;
        } else {
            return false;
        }
    }

    // --- HELPER FUNCTIONS ---

    // Checks each meal slot for restriction conflicts, auto-replaces where possible,
    // updates the order's status, and returns a list of SlotConflictDTOs for UI display.
    private List<SlotConflictDTO> checkForRestrictions(DailyOrder order) {
        Patient patient = order.getPatient();
        List<SlotConflictDTO> conflicts = new ArrayList<>();

        String restrictionString = String.join(",", patient.getRestriction());
        List<String> restrictions = List.of(restrictionString.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .toList();

        boolean autoChanged = false;
        boolean needsManual = false;

        // Returns the first restriction that matches the meal's ingredients (word-boundary),
        // or null if no conflict is found.
        java.util.function.Function<Meal, String> findConflict = meal -> {
            if (meal == null || meal.getIngredients() == null) return null;
            String ingredients = meal.getIngredients().toLowerCase();
            for (String r : restrictions) {
                if (r == null || r.isBlank()) continue;
                String regex = "(?<![a-z0-9])" + java.util.regex.Pattern.quote(r) + "(?![a-z0-9])";
                if (java.util.regex.Pattern.compile(regex).matcher(ingredients).find()) {
                    return r;
                }
            }
            return null;
        };

        // Helper to process one slot: find conflict, attempt swap, record result.
        String matched;

        matched = findConflict.apply(order.getBreakfast());
        if (matched != null) {
            String original = order.getBreakfast().getName();
            Meal replacement = findSafeAlternative(order.getFoodType(), "breakfast", restrictions);
            if (replacement != null) { order.setBreakfast(replacement); autoChanged = true; }
            else needsManual = true;
            conflicts.add(new SlotConflictDTO("Breakfast", original, matched,
                    replacement != null ? replacement.getName() : null));
        }

        matched = findConflict.apply(order.getLunch());
        if (matched != null) {
            String original = order.getLunch().getName();
            Meal replacement = findSafeAlternative(order.getFoodType(), "lunch", restrictions);
            if (replacement != null) { order.setLunch(replacement); autoChanged = true; }
            else needsManual = true;
            conflicts.add(new SlotConflictDTO("Lunch", original, matched,
                    replacement != null ? replacement.getName() : null));
        }

        matched = findConflict.apply(order.getAfternoonSnack());
        if (matched != null) {
            String original = order.getAfternoonSnack().getName();
            Meal replacement = findSafeAlternative(order.getFoodType(), "afternoonsnack", restrictions);
            if (replacement != null) { order.setAfternoonSnack(replacement); autoChanged = true; }
            else needsManual = true;
            conflicts.add(new SlotConflictDTO("Afternoon Snack", original, matched,
                    replacement != null ? replacement.getName() : null));
        }

        matched = findConflict.apply(order.getDinner());
        if (matched != null) {
            String original = order.getDinner().getName();
            Meal replacement = findSafeAlternative(order.getFoodType(), "dinner", restrictions);
            if (replacement != null) { order.setDinner(replacement); autoChanged = true; }
            else needsManual = true;
            conflicts.add(new SlotConflictDTO("Dinner", original, matched,
                    replacement != null ? replacement.getName() : null));
        }

        matched = findConflict.apply(order.getNightSnack());
        if (matched != null) {
            String original = order.getNightSnack().getName();
            Meal replacement = findSafeAlternative(order.getFoodType(), "nightsnack", restrictions);
            if (replacement != null) { order.setNightSnack(replacement); autoChanged = true; }
            else needsManual = true;
            conflicts.add(new SlotConflictDTO("Night Snack", original, matched,
                    replacement != null ? replacement.getName() : null));
        }

        if (needsManual) {
            order.setStatus("NEEDS MANUAL CHANGE");
        } else if (autoChanged) {
            order.setStatus("AUTO CHANGED");
        } else {
            order.setStatus("SUBMITTED");
        }

        System.out.println("Checked restrictions for " + patient.getName()
                + " → " + order.getStatus() + " (" + conflicts.size() + " conflict(s))");
        return conflicts;
    }

    // Tries to find a safe alternative meal from the same food-type group.
    // Returns null if no conflict-free meal exists (manual change required).
    private Meal findSafeAlternative(FoodType currentFoodType, String category, List<String> restrictions) {
        List<List<String>> groups = List.of(
                List.of("A1", "A2", "A3", "OP", "RDS-KF", "RDS-G"),
                List.of("M1", "M2", "M3"),
                List.of("F1", "F1-S", "F1-M", "F2", "F3", "F4", "F4-S", "F5"));

        String currentTypeName = currentFoodType.getTypeName();

        Optional<List<String>> currentGroupOpt = groups.stream()
                .filter(g -> g.contains(currentTypeName))
                .findFirst();

        if (currentGroupOpt.isEmpty()) {
            System.out.println("[ALT] " + currentTypeName + " not in any known group — manual change required for " + category);
            return null;
        }

        List<String> currentGroup = currentGroupOpt.get();
        System.out.println("[ALT] Looking for safe " + category + " alternative for " + currentTypeName
                + " — searching group " + currentGroup + " with restrictions " + restrictions);

        List<FoodType> foodTypesInGroup = foodTypeRepository.findAll().stream()
                .filter(ft -> currentGroup.contains(ft.getTypeName()))
                .toList();

        for (FoodType ft : foodTypesInGroup) {
            Menu menu = ft.getMenuOfTheDay();
            if (menu == null) {
                System.out.println("[ALT]   " + ft.getTypeName() + " → no menuOfTheDay assigned, skipping");
                continue;
            }

            Meal candidate = switch (category.toLowerCase()) {
                case "breakfast"     -> menu.getBreakfast();
                case "lunch"         -> menu.getLunch();
                case "afternoonsnack"-> menu.getAfternoonSnack();
                case "dinner"        -> menu.getDinner();
                case "nightsnack"    -> menu.getNightSnack();
                default              -> null;
            };

            if (candidate == null) {
                System.out.println("[ALT]   " + ft.getTypeName() + " → no meal in slot '" + category + "', skipping");
                continue;
            }
            if (candidate.getIngredients() == null) {
                System.out.println("[ALT]   " + ft.getTypeName() + " → meal '" + candidate.getName()
                        + "' has no ingredients listed, skipping");
                continue;
            }

            String ingredients = candidate.getIngredients().toLowerCase();

            // Use the same word-boundary regex as checkForRestrictions so that e.g.
            // restriction "milk" does NOT falsely match "buttermilk"
            boolean hasConflict = restrictions.stream().anyMatch(token -> {
                String regex = "(?<![a-z0-9])" + java.util.regex.Pattern.quote(token) + "(?![a-z0-9])";
                boolean matched = java.util.regex.Pattern.compile(regex).matcher(ingredients).find();
                if (matched) {
                    System.out.println("[ALT]   " + ft.getTypeName() + " → meal '" + candidate.getName()
                            + "' rejected: restriction '" + token + "' matched in ingredients [" + ingredients + "]");
                }
                return matched;
            });

            if (!hasConflict) {
                System.out.println("[ALT]   " + ft.getTypeName() + " → meal '" + candidate.getName()
                        + "' is safe — using as replacement for " + currentTypeName + " " + category);
                return candidate;
            }
        }

        System.out.println("[ALT] No safe alternative found for " + currentTypeName + " " + category
                + " — manual change required");
        return null;
    }

    // Finds today's order for a patient, if any
    public DailyOrder findTodayOrderForPatient(Patient patient) {
        return dailyOrderRepository.findByPatientAndOrderDate(patient, LocalDate.now())
                .orElseGet(() -> {
                    DailyOrder emptyOrder = new DailyOrder();
                    emptyOrder.setPatient(patient);
                    emptyOrder.setOrderDate(LocalDate.now());
                    emptyOrder.setStatus("N/A");
                    return emptyOrder;
                });
    }

    // For UC10
    public List<DailyOrder> getFilteredOrders(LocalDate date, String foodType, String wardName, String status) {

        if (wardName != null && date != null && foodType != null && status != null) {
            return dailyOrderRepository.findByWardNameAndOrderDateAndFoodType_TypeNameAndStatus(wardName, date,
                    foodType, status);
        } else if (wardName != null && date != null && status != null) {
            return dailyOrderRepository.findByWardNameAndOrderDateAndStatus(wardName, date, status);
        } else if (wardName != null && foodType != null && status != null) {
            return dailyOrderRepository.findByWardNameAndFoodType_TypeNameAndStatus(wardName, foodType, status);
        } else if (wardName != null && status != null) {
            return dailyOrderRepository.findByWardNameAndStatus(wardName, status);
        } else if (wardName != null && date != null && foodType != null) {
            return dailyOrderRepository.findByWardNameAndOrderDateAndFoodType_TypeName(wardName, date, foodType);
        } else if (wardName != null && date != null) {
            return dailyOrderRepository.findByWardNameAndOrderDate(wardName, date);
        } else if (wardName != null && foodType != null) {
            return dailyOrderRepository.findByWardNameAndFoodType_TypeName(wardName, foodType);
        } else if (wardName != null) {
            return dailyOrderRepository.findByWardName(wardName);
        } else {
            return dailyOrderRepository.findAll();
        }
    }

    public List<DailyOrderSummaryDTO> getFilteredOrdersDTO(LocalDate date, String foodType, String wardName,
            String status) {
        List<DailyOrder> orders = getFilteredOrders(date, foodType, wardName, status);

        return orders.stream()
                .map(order -> new DailyOrderSummaryDTO(
                        order.getId(),
                        order.getOrderDate(),
                        order.getWardName(),
                        order.getRoomNumber(),
                        order.getPatient() != null ? order.getPatient().getName() : "Unknown",
                        order.getFoodType() != null ? order.getFoodType().getTypeName() : "N/A",
                        order.getStatus()))
                .toList();
    }
}