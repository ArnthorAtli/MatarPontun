package is.hi.matarpontun.service;

import is.hi.matarpontun.dto.MenuOfTheDayDTO;
import is.hi.matarpontun.dto.PatientMealDTO;
import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.model.Menu;
import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.repository.MealRepository;
import is.hi.matarpontun.repository.MenuRepository;
import is.hi.matarpontun.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final MenuRepository menuRepository;

    public PatientService(PatientRepository patientRepository, MenuRepository menuRepository) {
        this.patientRepository = patientRepository;
        this.menuRepository = menuRepository;
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

    public PatientMealDTO mapToPatientMealDTO(Patient patient) {
        var foodType = patient.getFoodType();

        Menu menu = null;
        if (foodType != null) {
            menu = menuRepository.findByFoodTypeAndDate(foodType, LocalDate.now()).orElse(null);
        }

        Meal nextMeal = (menu != null) ? getNextMeal(menu) : null;
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

    private Meal getNextMeal(Menu menu) {
        var now = LocalTime.now();

        if (now.isBefore(LocalTime.of(9, 0))) return menu.getBreakfast();
        else if (now.isBefore(LocalTime.of(12, 0))) return menu.getLunch();
        else if (now.isBefore(LocalTime.of(15, 0))) return menu.getAfternoonSnack();
        else if (now.isBefore(LocalTime.of(19, 0))) return menu.getDinner();
        else if (now.isBefore(LocalTime.of(22, 0))) return menu.getNightSnack();
        else return menu.getBreakfast(); // after 22:00 → assume next day breakfast
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


