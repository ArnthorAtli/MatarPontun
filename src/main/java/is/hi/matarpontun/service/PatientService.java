package is.hi.matarpontun.service;

import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    // Adds a single restriction string to the patient's restriction list. If the patient has no restriction yet, one is created automatically.
    public Patient addRestriction(Long patientID, String restriction) {
        Patient patient = patientRepository.findById(patientID)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));
        // Only add if it doesn't already exist
        if (!patient.getRestriction().contains(restriction)) {
            patient.getRestriction().add(restriction);
        }

        return patientRepository.save(patient);
    }
}
