package is.hi.matarpontun.service;

import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.model.Restriction;
import is.hi.matarpontun.repository.PatientRepository;
import is.hi.matarpontun.repository.RestrictionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

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

    public Patient addRestriction(Long patientID, Restriction restriction) {
        Patient patient = patientRepository.findById(patientID)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        // Because of cascade = CascadeType.ALL, this will automatically save both
        patient.setRestriction(restriction);
        return patientRepository.save(patient);
    }
}
