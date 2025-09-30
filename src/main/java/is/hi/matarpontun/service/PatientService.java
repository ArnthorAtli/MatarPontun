package is.hi.matarpontun.service;

import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Optional<Patient> findById(Long patientID) {
        return patientRepository.findById(patientID);
    }
}
