package is.hi.matarpontun.service;

import is.hi.matarpontun.dto.PatientMealDTO;
import is.hi.matarpontun.dto.WardDTO;
import is.hi.matarpontun.dto.WardFullDTO;
import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.model.Menu;
import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.model.Ward;
import is.hi.matarpontun.repository.WardRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class WardService {

    private final WardRepository wardRepository;

    public WardService(WardRepository wardRepository) {
        this.wardRepository = wardRepository;
    }

    public Ward createWard(Ward ward) {
        return wardRepository.save(ward);
    }

    public List<Ward> findAllWards() {
        return wardRepository.findAll();
    }

    // when ward is signed in and wants to fetch data for all the corresponding patients
    public Optional<WardFullDTO> signInAndGetData(String wardName, String password) {
        return wardRepository.findByWardNameAndPassword(wardName, password)
                .map(this::mapToWardFullDTO);
    }

    // when ward is signed in and wants to fetch data for a specific the corresponding patients
    public Optional<PatientMealDTO> signInAndGetPatientData(String wardName, String password, Long patientId) {
        return wardRepository.findByWardNameAndPassword(wardName, password)
                .flatMap(ward -> ward.getPatients().stream()
                        .filter(p -> p.getPatientID().equals(patientId))
                        .findFirst()
                        .map(this::mapToPatientMealDTO));
    }


    private WardFullDTO mapToWardFullDTO(Ward ward) {
        var patientDTOs = ward.getPatients().stream()
                .map(this::mapToPatientMealDTO)
                .toList();

        return new WardFullDTO(ward.getWardName(), patientDTOs);
    }

    private PatientMealDTO mapToPatientMealDTO(Patient patient) {
        var foodType = patient.getFoodType();
        Menu menu = (foodType != null) ? foodType.getMenuOfTheDay() : null;

        Meal nextMeal = (menu != null) ? getNextMeal(menu) : null;

        return new PatientMealDTO(
                patient.getPatientID(),
                patient.getName(),
                patient.getAge(),
                patient.getBedNumber(),
                (foodType != null) ? foodType.getTypeName() : null,
                nextMeal,
                menu
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
            return menu.getBreakfast(); // after 22:00 → tomorrow’s breakfast
        }
    }

    //-------------------------------------------------
    /*
    public Optional<Ward> signIn(String wardName, String password) {
        Optional<Ward> optionalWard = wardRepository.findByWardName(wardName);

        if (optionalWard.isPresent()) {
            Ward ward = optionalWard.get();
            //kanski bæta við hashing í framtíðinni...
            if (ward.getPassword().equals(password)) {
                return Optional.of(ward);
            }
        }
        return Optional.empty();
    }
     */

}