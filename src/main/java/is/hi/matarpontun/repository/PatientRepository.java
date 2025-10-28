package is.hi.matarpontun.repository;

import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.model.Room;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByPatientID(Long patientID);
    List<Patient> findByRoom(Room room);
    long countByWard_Id(Long wardId);
}
