package is.hi.matarpontun.service;

import is.hi.matarpontun.dto.MenuOfTheDayDTO;
import is.hi.matarpontun.dto.PatientMealDTO;
import is.hi.matarpontun.model.FoodType;
import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.model.Menu;
import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.model.Room;
import is.hi.matarpontun.repository.FoodTypeRepository;
import is.hi.matarpontun.repository.MenuRepository;
import is.hi.matarpontun.repository.PatientRepository;
import is.hi.matarpontun.util.MealPeriod;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final FoodTypeRepository foodTypeRepository;
    private final MenuRepository menuRepository;
    

    public PatientService(PatientRepository patientRepository, FoodTypeRepository foodTypeRepository,
            MenuRepository menuRepository) {
        this.patientRepository = patientRepository;
        this.foodTypeRepository = foodTypeRepository;
        this.menuRepository = menuRepository;
        
    }

    // Helper method to check a meal against all of a patient's restrictions and
    // allergies.
    // seinna til að bæta: when Meal.ingredients becomes a List<String>, this can
    // loop directly over ingredients instead of string matching.
    // Shared dietary conflict logic (UC2 + UC3)
    public boolean checkMealForConflicts(Meal meal, Patient patient) {
        if (meal == null || meal.getIngredients() == null)
            return false;

        // Split ingredients into tokens (remove spaces)
        List<String> mealIngredients = Arrays.stream(meal.getIngredients().split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();

        for (String r : patient.getRestriction()) {
            if (mealIngredients.contains(r.toLowerCase().trim())) {
                return true;
            }
        }
        for (String a : patient.getAllergies()) {
            if (mealIngredients.contains(a.toLowerCase().trim())) {
                return true;
            }
        }
        return false;

        /*
         * String ingredients = meal.getIngredients().toLowerCase();
         * 
         * return Stream.concat(patient.getRestriction().stream(),
         * patient.getAllergies().stream())
         * .map(String::toLowerCase)
         * .anyMatch(ingredients::contains);
         * 
         */
    }

    public Optional<Patient> findById(Long patientID) {
        return patientRepository.findById(patientID);
    }

    // Adds a restriction string to the patient's restriction list. If the patient
    // has no restriction yet, one is created automatically.
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
                if (r != null)
                    removeSet.add(r.trim());
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

    // Adds an allergy string to the patient's allergy list. If the patient has no
    // allergies yet, one is created automatically.
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
                if (r != null)
                    removeSet.add(r.trim());
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

    // tekur inn patient og nær í FoodType fyrir hann. Núllstillir menum ef hann
    // hefur FoodType skráða og finnur skráðan matseðil fyrir þann dag og náum í
    // næstu máltíð.
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
                patient.getAllergies());
    }

    private MenuOfTheDayDTO mapToMenuOfTheDayDTO(Menu menu) {
        return new MenuOfTheDayDTO(
                menu.getDate(),
                menu.getBreakfast() != null ? menu.getBreakfast().getName() : null,
                menu.getLunch() != null ? menu.getLunch().getName() : null,
                menu.getAfternoonSnack() != null ? menu.getAfternoonSnack().getName() : null,
                menu.getDinner() != null ? menu.getDinner().getName() : null,
                menu.getNightSnack() != null ? menu.getNightSnack().getName() : null);
    }

    public Patient createRandomPatient(Room room) {
        Random random = new Random();

        // Generate name like "Patient_1234"
        String name = "Patient_" + (1000 + random.nextInt(9000));

        // Generate random age between 10–70
        int age = 10 + random.nextInt(61);

        // Determine bed number
        int bedNumber = 1;
        if (room.getPatients() != null && !room.getPatients().isEmpty()) {
            bedNumber = room.getPatients().size() + 1;
        }

        // Create patient
        Patient patient = new Patient();
        patient.setName(name);
        patient.setAge(age);
        patient.setBedNumber(bedNumber);
        patient.setRoom(room);
        patient.setWard(room.getWard());
        FoodType food = foodTypeRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("Default FoodType with ID 1 not found."));
        patient.setFoodType(food);

        // Save and return
        return patientRepository.save(patient);
    }

    public Patient updatePatientFoodType(Long patientId, String foodTypeName) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        FoodType newFoodType = foodTypeRepository.findByTypeNameIgnoreCase(foodTypeName)
                .orElseThrow(() -> new EntityNotFoundException("Food type '" + foodTypeName + "' not found"));

        patient.setFoodType(newFoodType);
        return patientRepository.save(patient);
    }
}
