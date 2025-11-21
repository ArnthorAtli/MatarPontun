package is.hi.matarpontun.service;

import is.hi.matarpontun.model.Room;
import is.hi.matarpontun.model.Ward;
import is.hi.matarpontun.model.DailyOrder;
import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.repository.RoomRepository;
import is.hi.matarpontun.repository.WardRepository;
import is.hi.matarpontun.repository.DailyOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import is.hi.matarpontun.repository.PatientRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for managing {@link Room} entities within hospital wards.
 */
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final WardRepository wardRepository;
    private final PatientService patientService;
    private final PatientRepository patientRepository;
    private final DailyOrderRepository dailyOrderRepository;

    /**
     * Constructs a new {@code RoomService} with required repositories and
     * services..
     *
     * @param roomRepository    the repository for persisting and retrieving
     *                          {@link Room} entities
     * @param wardRepository    the repository for accessing {@link Ward} entities
     * @param patientService    the service used to create and manage
     *                          {@link Patient} entities
     * @param patientRepository the repository responsible for storing and
     *                          retrieving {@link Patient} entities
     */
    public RoomService(RoomRepository roomRepository, WardRepository wardRepository, PatientService patientService,
            PatientRepository patientRepository, DailyOrderRepository dailyOrderRepository) {
        this.roomRepository = roomRepository;
        this.wardRepository = wardRepository;
        this.patientService = patientService;
        this.patientRepository = patientRepository;
        this.dailyOrderRepository = dailyOrderRepository;
    }

    /**
     * Creates a new {@link Room} associated with a specific {@link Ward}.
     *
     * @param wardId     the id of the ward to which the room will belong
     * @param roomNumber the room number
     * @return the created {@link Room}
     * @throws EntityNotFoundException if the specified ward does not exist
     */
    public Room createRoomForWard(Long wardId, String roomNumber) {
        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new EntityNotFoundException("Ward with ID " + wardId + " not found."));
        Room room = new Room(roomNumber, ward);
        return roomRepository.save(room);
    }

    /**
     * Creates a new {@link Room} under the given ward and fills it with randomly
     * generated {@link Patient}s.
     *
     * @param numberOfPatients the number of patients to create
     * @param wardId           the id of the ward to which the room belongs
     * @param roomNumber       the identifier of the new room
     * @return the created {@link Room} including its assigned patients
     * @throws EntityNotFoundException if the ward does not exist
     */
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

    /**
     * Deletes a {@link Room} and all {@link Patient}s assigned to it in a single
     * transaction.
     * 
     * The method ensures that patient entities are deleted before the room itself,
     * maintaining referential integrity.
     *
     * @param roomId the id of the room to delete
     * @return a {@link Map} containing a confirmation message and the number of
     *         deleted patients
     * @throws RuntimeException if the specified room does not exist
     */
    @Transactional
    public Map<String, Object> deleteRoomAndPatients(Long roomId) {
        // Find the room
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room with ID " + roomId + " not found."));

        // Find all patients in this room
        List<Patient> patientsInRoom = patientRepository.findByRoom(room);

        for (Patient patient : patientsInRoom) {
            List<DailyOrder> orders = dailyOrderRepository.findAllByPatient(patient);

            for (DailyOrder order : orders) {
                order.setPatient(null); 
                dailyOrderRepository.delete(order);
                System.out.println("Deleted order (" + order.getOrderDate() + ") for " + patient.getName());
            }
        }

        // Delete the room itself
        roomRepository.delete(room);

        // Return confirmation
        return Map.of(
                "message", "Room with ID " + roomId + " and its " + patientsInRoom.size()
                        + " patient(s) were deleted successfully.");
    }
}
