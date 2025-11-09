package is.hi.matarpontun.service;

import is.hi.matarpontun.model.FoodType;
import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.model.Room;
import is.hi.matarpontun.repository.FoodTypeRepository;
import is.hi.matarpontun.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Service responsible for managing {@link Patient} entities..
 */
@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final FoodTypeRepository foodTypeRepository;

    /**
     * Constructs a new {@code PatientService} with required repositories.
     *
     * @param patientRepository   the repository responsible for storing and retrieving {@link Patient} entities
     * @param foodTypeRepository  the repository responsible for accessing {@link FoodType} entities
     */
    public PatientService(PatientRepository patientRepository, FoodTypeRepository foodTypeRepository) {
        this.patientRepository = patientRepository;
        this.foodTypeRepository = foodTypeRepository;
    }

    /**
     * Checks whether a given {@link Meal} conflicts with a patient's dietary
     * restrictions or allergies.
     * 
     * This method compares each restriction and allergy term against the meal’s ingredient list.
     *
     * @param meal    the meal to check
     * @param patient the patient whose restrictions and allergies should be validated
     * @return {@code true} if a conflict is found; {@code false} otherwise
     */
    // seinna til að bæta: when Meal.ingredients becomes a List<String>, this can
    // loop directly over ingredients instead of string matching.
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

    }

    /**
     * Finds a {@link Patient} by id.
     *
     * @param patientID the patient’s id
     * @return an {@link Optional} containing the patient if found or empty if not
     */
    public Optional<Patient> findById(Long patientID) {
        return patientRepository.findById(patientID);
    }

    /**
     * Adds a new restriction to the patient’s restriction list if it does not
     * already exist.
     *
     * @param patientID   the patient’s id
     * @param restriction the restriction to add
     * @return the updated {@link Patient}
     * @throws EntityNotFoundException if the patient does not exist
     */
    public Patient addRestriction(Long patientID, String restriction) {
        Patient patient = patientRepository.findById(patientID)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        if (!patient.getRestriction().contains(restriction)) {
            patient.getRestriction().add(restriction);
        }
        return patientRepository.save(patient);
    }

    /**
     * Removes one or more restrictions from a patient’s restriction list.
     *
     * @param patientID the patient’s id
     * @param toRemove  list of restrictions to remove
     * @return the updated {@link Patient}
     * @throws EntityNotFoundException if the patient does not exist
     */
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

    /**
     * Clears all restrictions from a patient’s record.
     *
     * @param patientID the patient’s id
     * @return the updated {@link Patient}
     * @throws EntityNotFoundException if the patient does not exist
     */
    public Patient clearAllRestrictions(Long patientID) {
        Patient patient = patientRepository.findById(patientID)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        patient.getRestriction().clear();
        return patientRepository.save(patient);
    }

    /**
     * Adds a new allergy to the patient’s allergy list if it does not already exist.
     *
     * @param patientID the patient’s id
     * @param allergy   the allergy to add
     * @return the updated {@link Patient}
     * @throws EntityNotFoundException if the patient does not exist
     */
    public Patient addAllergy(Long patientID, String allergy) {
        Patient patient = patientRepository.findById(patientID)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        if (!patient.getAllergies().contains(allergy)) {
            patient.getAllergies().add(allergy);
        }
        return patientRepository.save(patient);
    }

    /**
     * Removes one or more allergies from a patient’s allergy list.
     *
     * @param patientID the patient’s id
     * @param toRemove  list of allergies to remove
     * @return the updated {@link Patient}
     * @throws EntityNotFoundException if the patient does not exist
     */
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

    /**
     * Clears all allergies from a patient’s record.
     *
     * @param patientID the patient’s id
     * @return the updated {@link Patient}
     * @throws EntityNotFoundException if the patient does not exist
     */
    public Patient clearAllAllergies(Long patientID) {
        Patient patient = patientRepository.findById(patientID)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        patient.getAllergies().clear();
        return patientRepository.save(patient);
    }

    /**
     * Creates a new patient with random demographic data and assigns them to the
     * specified {@link Room}.
     *
     * @param room the {@link Room} the patient belongs to
     * @return the newly created {@link Patient}
     * @throws EntityNotFoundException if the default {@link FoodType} is missing
     */
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

    /**
     * Updates a patient’s assigned {@link FoodType} based on a given food type name.
     *
     * @param patientId    the patient’s id
     * @param foodTypeName the name of the new food type
     * @return the updated {@link Patient}
     * @throws EntityNotFoundException if the patient or food type is not found
     */
    public Patient updatePatientFoodType(Long patientId, String foodTypeName) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        FoodType newFoodType = foodTypeRepository.findByTypeNameIgnoreCase(foodTypeName)
                .orElseThrow(() -> new EntityNotFoundException("Food type '" + foodTypeName + "' not found"));

        patient.setFoodType(newFoodType);
        return patientRepository.save(patient);
    }
}
