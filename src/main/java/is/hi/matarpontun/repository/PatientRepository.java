package is.hi.matarpontun.repository;

import is.hi.matarpontun.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByPatientID(Long patientID);
}
