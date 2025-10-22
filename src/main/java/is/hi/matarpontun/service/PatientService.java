package is.hi.matarpontun.service;

import is.hi.matarpontun.dto.MenuOfTheDayDTO;
import is.hi.matarpontun.dto.PatientMealDTO;
import is.hi.matarpontun.dto.RestrictionUpdateResultDTO;
import is.hi.matarpontun.model.FoodType;
import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.model.Menu;
import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.repository.FoodTypeRepository;
import is.hi.matarpontun.repository.MenuRepository;
import is.hi.matarpontun.repository.PatientRepository;
import is.hi.matarpontun.util.MealPeriod;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final FoodTypeRepository foodTypeRepository;
    private final MenuRepository menuRepository;


    public PatientService(PatientRepository patientRepository, FoodTypeRepository foodTypeRepository, MenuRepository menuRepository) {
        this.patientRepository = patientRepository;
        this.foodTypeRepository = foodTypeRepository;
        this.menuRepository = menuRepository;
    }

    // Adds a restriction to a patient and checks if their next meal is still suitable. If not, attempts to reassign a new food type.
    // seinna til að bæta: The meal suitability logic could later be factored into a small helper or utility (e.g. DietCompatibilityService) so it can also be reused by UC2
    public RestrictionUpdateResultDTO addRestrictionAndReassignFoodType(Long patientId, String restriction) {
        // Step 1: Find the patient and add the new restriction.
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient with ID " + patientId + " not found."));

        if (restriction != null && !restriction.isBlank() && !patient.getRestriction().contains(restriction)) {
            patient.getRestriction().add(restriction);
        }


        // Step 2: Determine the patient's next scheduled meal based on their current diet.
        MealPeriod currentPeriod = MealPeriod.current(LocalTime.now());
        Meal nextMeal = null;
        if (patient.getFoodType() != null) {
            Optional<Menu> currentMenuOpt = menuRepository.findByFoodTypeAndDate(patient.getFoodType(), LocalDate.now());
            if (currentMenuOpt.isPresent()) {
                nextMeal = currentPeriod.getMealFromMenu(currentMenuOpt.get());
            }
        }

        // Step 3: Check if the new restriction creates a conflict with the next meal.
        boolean hasConflict = checkMealForConflicts(nextMeal, patient);

        // Case A: No conflict exists. Save the patient and return a success message.
        if (nextMeal == null || !hasConflict) {
            patientRepository.save(patient);
            String mealName = (nextMeal != null) ? nextMeal.getName() : "No scheduled meal";
            String mealIngredients = (nextMeal != null) ? nextMeal.getIngredients() : "N/A";
            return new RestrictionUpdateResultDTO(
                    "Restriction '" + restriction + "' added successfully. The patient's next meal is still suitable.",
                    patient.getFoodType() != null ? patient.getFoodType().getTypeName() : "N/A",
                    mealName,
                    mealIngredients
            );
        }

        // Case B: A conflict exists. Loop through all food types to find a suitable alternative.
        List<FoodType> allFoodTypes = foodTypeRepository.findAll();
        for (FoodType potentialFoodType : allFoodTypes) {
            Optional<Menu> potentialMenuOpt = menuRepository.findByFoodTypeAndDate(potentialFoodType, LocalDate.now());
            if (potentialMenuOpt.isPresent()) {
                Meal potentialMeal = currentPeriod.getMealFromMenu(potentialMenuOpt.get());
                if (potentialMeal != null && !checkMealForConflicts(potentialMeal, patient)) {
                    // Alternative found! Update the patient's diet.
                    patient.setFoodType(potentialFoodType);
                    patientRepository.save(patient);

                    return new RestrictionUpdateResultDTO(
                            "Conflict detected! Patient has been successfully reassigned to a new diet.",
                            potentialFoodType.getTypeName(),
                            potentialMeal.getName(),
                            potentialMeal.getIngredients()
                    );
                }
            }
        }

        // Case C: Conflict exists, but no alternative could be found.
        patientRepository.save(patient); // Save the patient with the new restriction anyway.
        return new RestrictionUpdateResultDTO(
                "Restriction '" + restriction + "' added, but a conflict was detected and NO suitable alternative food type could be found. Manual review required.",
                patient.getFoodType().getTypeName(),
                nextMeal.getName(),
                nextMeal.getIngredients()
        );
    }

    // Helper method to check a meal against all of a patient's restrictions and allergies.
    // seinna til að bæta: when Meal.ingredients becomes a List<String>, this can loop directly over ingredients instead of string matching.
    //  Shared dietary conflict logic (UC2 + UC3)
    public boolean checkMealForConflicts(Meal meal, Patient patient) {
        if (meal == null || meal.getIngredients() == null) return false;
        String ingredients = meal.getIngredients().toLowerCase();

        return Stream.concat(patient.getRestriction().stream(), patient.getAllergies().stream())
                .map(String::toLowerCase)
                .anyMatch(ingredients::contains);
    }

    public Optional<Patient> findById(Long patientID) {
        return patientRepository.findById(patientID);
    }

    // Adds a restriction string to the patient's restriction list. If the patient has no restriction yet, one is created automatically.
    public Patient addRestriction(Long patientID, String restriction) {
        Patient patient = patientRepository.findById(patientID)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        if (!patient.getRestriction().contains(restriction)) {
            patient.getRestriction().add(restriction);
        }
        return patientRepository.save(patient);
    }

    // Remove one or more restrictions
    public Patient removeRestrictions(Long patientID, java.util.List<String> toRemove) {
        Patient patient = patientRepository.findById(patientID)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        if (toRemove != null && !toRemove.isEmpty()) {
            java.util.Set<String> removeSet = new java.util.HashSet<>();
            for (String r : toRemove) {
                if (r != null) removeSet.add(r.trim());
            }
            patient.getRestriction().removeIf(r -> removeSet.contains(r.trim()));
        }
        return patientRepository.save(patient);
    }

    // Remove all restrictions
    public Patient clearAllRestrictions(Long patientID) {
        Patient patient = patientRepository.findById(patientID)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        patient.getRestriction().clear();
        return patientRepository.save(patient);
    }

    // Adds an allergy string to the patient's allergy list. If the patient has no allergies yet, one is created automatically.
    public Patient addAllergy(Long patientID, String allergy) {
        Patient patient = patientRepository.findById(patientID)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        if (!patient.getAllergies().contains(allergy)) {
            patient.getAllergies().add(allergy);
        }
        return patientRepository.save(patient);
    }

    // Remove one or more allergy
    public Patient removeAllergies(Long patientID, java.util.List<String> toRemove) {
        Patient patient = patientRepository.findById(patientID)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        if (toRemove != null && !toRemove.isEmpty()) {
            java.util.Set<String> removeSet = new java.util.HashSet<>();
            for (String r : toRemove) {
                if (r != null) removeSet.add(r.trim());
            }
            patient.getAllergies().removeIf(r -> removeSet.contains(r.trim()));
        }
        return patientRepository.save(patient);
    }

    // Remove all restrictions
    public Patient clearAllAllergies(Long patientID) {
        Patient patient = patientRepository.findById(patientID)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        patient.getAllergies().clear();
        return patientRepository.save(patient);
    }

    // tekur inn patient og nær í FoodType fyrir hann. Núllstillir menum ef hann hefur FoodType skráða og finnur skráðan matseðil fyrir þann dag og náum í næstu máltíð.
    // skilar svo Patient með matseðli dagsins í DTO
    public PatientMealDTO mapToPatientMealDTO(Patient patient) {
        var foodType = patient.getFoodType();
        Menu menu = (foodType != null)
                ? menuRepository.findByFoodTypeAndDate(foodType, LocalDate.now()).orElse(null)
                : null;

        Meal nextMeal = (menu != null)
                ? MealPeriod.current(LocalTime.now()).getMealFromMenu(menu)
                : null;

        MenuOfTheDayDTO menuDTO = (menu != null) ? mapToMenuOfTheDayDTO(menu) : null;

        return new PatientMealDTO(
                patient.getPatientID(),
                patient.getName(),
                patient.getAge(),
                patient.getRoom().getRoomNumber(),
                patient.getBedNumber(),
                (foodType != null) ? foodType.getTypeName() : null,
                nextMeal,
                menuDTO,
                patient.getRestriction(),
                patient.getAllergies()
        );
    }

    private MenuOfTheDayDTO mapToMenuOfTheDayDTO(Menu menu) {
        return new MenuOfTheDayDTO(
                menu.getDate(),
                menu.getBreakfast() != null ? menu.getBreakfast().getName() : null,
                menu.getLunch() != null ? menu.getLunch().getName() : null,
                menu.getAfternoonSnack() != null ? menu.getAfternoonSnack().getName() : null,
                menu.getDinner() != null ? menu.getDinner().getName() : null,
                menu.getNightSnack() != null ? menu.getNightSnack().getName() : null
        );
    }
}



