package is.hi.matarpontun.service;

import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.model.Restriction;
import is.hi.matarpontun.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
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

    // Adds a single restriction string to the patient's restriction list. If the patient has no restriction yet, one is created automatically.
    public Patient addSingleRestriction(Long patientID, String restrictionText) {
        Patient patient = patientRepository.findById(patientID)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        Restriction restriction = patient.getRestriction();

        if (restriction == null) {
            restriction = new Restriction();
        }

        restriction.getRestrictions().add(restrictionText);
        patient.setRestriction(restriction);

        Patient updated = patientRepository.save(patient);
        updated.getRestriction().getRestrictions().size(); // load list
        return updated;
    }


}
