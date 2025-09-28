package is.hi.matarpontun.repository;

import is.hi.matarpontun.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    /*
    Patient save(Patient patient);
    Patient findAll(Patient patient);
    Patient findByPatientID(Patient patient);
    Patient findByName(String patientName);
     */
}
