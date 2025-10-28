package is.hi.matarpontun.service;

import is.hi.matarpontun.model.Room;
import is.hi.matarpontun.model.Ward;
import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.repository.RoomRepository;
import is.hi.matarpontun.repository.WardRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import is.hi.matarpontun.repository.PatientRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final WardRepository wardRepository;
    private final PatientService patientService;
    private final PatientRepository patientRepository;

    public RoomService(RoomRepository roomRepository, WardRepository wardRepository, PatientService patientService, PatientRepository patientRepository) {
        this.roomRepository = roomRepository;
        this.wardRepository = wardRepository;
        this.patientService = patientService;
        this.patientRepository = patientRepository;
    }

    public Room createRoomForWard(Long wardId, String roomNumber) {
        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new EntityNotFoundException("Ward with ID " + wardId + " not found."));
        Room room = new Room(roomNumber, ward);
        return roomRepository.save(room);
    }

    // create room and fill it with random patients
    @Transactional
    public Room createRoomAndFillWithPatients(int numberOfPatients, Long wardId, String roomNumber) {
        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new EntityNotFoundException("Ward with ID " + wardId + " not found."));

        // Create and save the room
        Room room = new Room(roomNumber, ward);
        Room savedRoom = roomRepository.save(room);

        // Create random patients and link them to the room
        List<Patient> createdPatients = new ArrayList<>();
        for (int i = 0; i < numberOfPatients; i++) {
            Patient patient = patientService.createRandomPatient(savedRoom);
            createdPatients.add(patient);
        }

        // Link patients to the room
        savedRoom.setPatients(createdPatients);

        return savedRoom;
    }

    @Transactional
    public Map<String, Object> deleteRoomAndPatients(Long roomId) {
        // find the room
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room with ID " + roomId + " not found."));

        // Find all patients in this room
        List<Patient> patientsInRoom = patientRepository.findByRoom(room);

        // Delete patients first
        if (!patientsInRoom.isEmpty()) {
            patientRepository.deleteAll(patientsInRoom);
        }

        // Delete the room itself
        roomRepository.delete(room);

        // Return confirmation
        return Map.of(
                "message", "Room with ID " + roomId + " and its " + patientsInRoom.size()
                        + " patient(s) were deleted successfully.");
    }
}
