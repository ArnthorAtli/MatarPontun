package is.hi.matarpontun.service;

import is.hi.matarpontun.dto.PatientMealDTO;
import is.hi.matarpontun.dto.WardFullDTO;
import is.hi.matarpontun.dto.WardUpdateDTO;
import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.model.Menu;
import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.model.Ward;
import is.hi.matarpontun.repository.MenuRepository;
import is.hi.matarpontun.repository.WardRepository;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class WardService {

    private final WardRepository wardRepository;
    private final MenuRepository menuRepository;

    public WardService(WardRepository wardRepository, MenuRepository menuRepository) {
        this.wardRepository = wardRepository;
        this.menuRepository = menuRepository;
    }

    public Ward createWard(Ward ward) {
        // Check if a ward with the given name already exists
        if (wardRepository.findByWardName(ward.getWardName()).isPresent()) {
            // If it exists, throw an exception with a clear message
            throw new IllegalArgumentException("A ward with the name '" + ward.getWardName() + "' already exists.");
        }
        
        return wardRepository.save(ward);
    }

    public List<Ward> findAllWards() {
        return wardRepository.findAll();
    }

    // UC8: Ward login + fetch all patient data
    public Optional<WardFullDTO> signInAndGetData(String wardName, String password) {
        return wardRepository.findByWardNameAndPassword(wardName, password)
                .map(this::mapToWardFullDTO);
    }

    // UC9: Ward login + fetch single patient by ID
    public Optional<PatientMealDTO> signInAndGetPatientData(String wardName, String password, Long patientId) {
        return wardRepository.findByWardNameAndPassword(wardName, password)
                .flatMap(ward -> ward.getPatients().stream()
                        .filter(p -> p.getPatientID().equals(patientId))
                        .findFirst()
                        .map(this::mapToPatientMealDTO));
    }

    public Ward updateWard(Long id, WardUpdateDTO req) {
        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ward not found"));

        if (req.wardName() == null || req.wardName().isBlank()) {
            throw new IllegalArgumentException("Ward name cannot be empty.");
        }
        if (wardRepository.existsByWardNameAndIdNot(req.wardName(), id)) {
            throw new IllegalArgumentException(
                    "A ward with the name '" + req.wardName() + "' already exists.");
        }
        ward.setWardName(req.wardName());

        if (req.password() == null || req.password().isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        ward.setPassword(req.password());
        return wardRepository.save(ward);
    }
    // --------------------- private helpers ---------------------

    private WardFullDTO mapToWardFullDTO(Ward ward) {
        var patientDTOs = ward.getPatients().stream()
                .map(this::mapToPatientMealDTO)
                .toList();

        return new WardFullDTO(ward.getWardName(), patientDTOs);
    }

    private PatientMealDTO mapToPatientMealDTO(Patient patient) {
        var foodType = patient.getFoodType();

        // Fetch today’s menu for this patient’s food type
        Menu menu = null;
        if (foodType != null) {
            menu = menuRepository
                    .findByFoodTypeAndDate(foodType, LocalDate.now())
                    .orElse(null);
        }

        Meal nextMeal = (menu != null) ? getNextMeal(menu) : null;

        return new PatientMealDTO(
                patient.getPatientID(),
                patient.getName(),
                patient.getAge(),
                patient.getBedNumber(),
                (foodType != null) ? foodType.getTypeName() : null,
                nextMeal,
                menu,
                patient.getRestriction(),
                patient.getAllergies()
        );
    }

    private Meal getNextMeal(Menu menu) {
        var now = LocalTime.now();

        if (now.isBefore(LocalTime.of(9, 0))) {
            return menu.getBreakfast();
        } else if (now.isBefore(LocalTime.of(12, 0))) {
            return menu.getLunch();
        } else if (now.isBefore(LocalTime.of(15, 0))) {
            return menu.getAfternoonSnack();
        } else if (now.isBefore(LocalTime.of(19, 0))) {
            return menu.getDinner();
        } else if (now.isBefore(LocalTime.of(22, 0))) {
            return menu.getNightSnack();
        } else {
            return menu.getBreakfast(); // after 22:00 → assume next day breakfast
        }
    }
}
