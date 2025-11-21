package is.hi.matarpontun.service;

import is.hi.matarpontun.dto.*;
import is.hi.matarpontun.model.*;
import is.hi.matarpontun.repository.*;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing {@link Ward} entities.
 */
@Service
public class WardService {

    protected static final Logger log = LoggerFactory.getLogger(WardService.class);

    private final WardRepository wardRepository;
    private final DailyOrderService dailyOrderService;
    private final RoomRepository roomRepository;
    private final PatientRepository patientRepository;
    private final DailyOrderRepository dailyOrderRepository;

    /**
     * Constructs a new {@code WardService} with the required repositories and
     * services.
     *
     * @param wardRepository    the repository for persisting and retrieving
     *                          {@link Ward} entities
     * @param dailyOrderService the service for generating and managing
     *                          {@link DailyOrder}
     * @param roomRepository    the repository for accessing {@link Room} entities
     * @param patientRepository the repository responsible for storing and
     *                          retrieving {@link Patient} entities
     */
    public WardService(WardRepository wardRepository,
            DailyOrderService dailyOrderService,
            RoomRepository roomRepository,
            PatientRepository patientRepository, DailyOrderRepository dailyOrderRepository) {
        this.wardRepository = wardRepository;
        this.dailyOrderService = dailyOrderService;
        this.roomRepository = roomRepository;
        this.patientRepository = patientRepository;
        this.dailyOrderRepository = dailyOrderRepository;
    }

    /**
     * UC2 - Generates daily meal orders for all patients in a given ward.
     *
     * @param wardId the id of the ward
     * @return an {@link OrderDTO} summarizing generated orders
     * @throws EntityNotFoundException if the ward does not exist
     */
    public OrderDTO generateDailyOrdersForWard(Long wardId) {
        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new EntityNotFoundException("Ward not found: " + wardId));

        log.info("Generating daily orders for ward: {}", ward.getWardName());
        return dailyOrderService.generateOrdersForWard(ward);
    }

    /**
     * Creates a new {@link Ward}.
     * 
     * Ensures that no duplicate ward names exist before saving.
     *
     * @param ward the {@link Ward} entity to create
     * @return the saved {@link Ward}
     * @throws IllegalArgumentException if a ward with the same name already exists
     */
    public Ward createWard(Ward ward) {
        if (wardRepository.findByWardName(ward.getWardName()).isPresent()) {
            throw new IllegalArgumentException("A ward with the name '" + ward.getWardName() + "' already exists.");
        }
        return wardRepository.save(ward);
    }

    /**
     * Retrieves all wards from the system.
     *
     * @return a list of all {@link Ward} entities
     */
    public List<Ward> findAllWards() {
        return wardRepository.findAll();
    }

    /**
     * UC8 - Authenticates a ward and retrieves its associated data,
     * including rooms, patients, and their daily orders.
     *
     * @param wardName the ward’s name
     * @param password the ward’s password
     * @return an optional {@link WardFullDTO} containing ward and patient details
     *         if authentication succeeds
     */
    public Optional<WardFullDTO> signInAndGetData(String wardName, String password) {
        return wardRepository.findByWardNameAndPassword(wardName, password)
                .map(this::mapToWardFullDTO);
    }

    /**
     * UC9 - Authenticates a ward and retrieves a single patient’s data by id.
     *
     * @param wardName  the ward’s name
     * @param password  the ward’s password
     * @param patientId the id of the patient to retrieve
     * @return an optional {@link PatientDailyOrderDTO} with the patient’s
     *         information if found and authorized
     */
    public Optional<PatientDailyOrderDTO> signInAndGetPatientData(String wardName, String password, Long patientId) {
        return wardRepository.findByWardNameAndPassword(wardName, password)
                .flatMap(ward -> ward.getPatients().stream()
                        .filter(p -> p.getPatientID().equals(patientId))
                        .findFirst()
                        .map(patient -> {
                            DailyOrder order = dailyOrderService.findTodayOrderForPatient(patient);
                            return PatientMapper.toDailyOrderDTO(patient, ward, order);
                        }));
    }

    /**
     * UC6 - Updates the information of an existing {@link Ward}.
     * 
     * Both the ward name and password must be provided.
     *
     * @param id  the id of the ward to update
     * @param req the {@link WardUpdateDTO} containing updated values
     * @return the updated {@link Ward}
     * @throws EntityNotFoundException  if the ward does not exist
     * @throws IllegalArgumentException if the new name or password is invalid or if
     *                                  the name is already in use
     */
    @Transactional
    public Ward updateWard(Long id, WardUpdateDTO req) {
        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ward not found"));

        if (req.wardName() == null || req.wardName().isBlank()) {
            throw new IllegalArgumentException("Ward name cannot be empty.");
        }
        if (wardRepository.existsByWardNameAndIdNot(req.wardName(), id)) {
            throw new IllegalArgumentException("A ward with the name '" + req.wardName() + "' already exists.");
        }

        ward.setWardName(req.wardName());

        if (req.password() == null || req.password().isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        ward.setPassword(req.password());
        return wardRepository.save(ward);
    }

    /**
     * UC16 - Retrieves a summary for a specific ward by its id.
     * 
     * The summary includes the ward name, room count and patient count.
     *
     * @param wardId the id of the ward
     * @return a {@link WardSummaryDTO} with ward statistics
     * @throws EntityNotFoundException if the ward does not exist
     */
    @Transactional
    public WardSummaryDTO getWardSummaryById(Long wardId) {
        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new EntityNotFoundException("Ward not found"));

        int rooms = (int) roomRepository.countByWard_Id(wardId);
        int patients = (int) patientRepository.countByWard_Id(wardId);

        return new WardSummaryDTO(ward.getId(), ward.getWardName(), rooms, patients);
    }

    /**
     * UC17 - Deletes a ward and all associated rooms and patients in a cascading
     * transaction.
     * 
     * The deletion order ensures all patients are removed before rooms and the ward
     * itself.
     *
     * @param wardId the id of the ward to delete
     * @throws EntityNotFoundException if the ward does not exist
     */
    @Transactional
    public void deleteWardCascade(Long wardId) {
        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new EntityNotFoundException("Ward not found with ID: " + wardId));


        // First, delete all daily orders for patients in the ward
        for (Room room : ward.getRooms()) {
            for (Patient patient : room.getPatients()) {
                List<DailyOrder> orders = dailyOrderRepository.findAllByPatient(patient);
                for (DailyOrder order : orders) {
                    order.setPatient(null);
                    dailyOrderRepository.delete(order);
                    System.out.println("Deleted today's order for patient " + patient.getName());
                }
            }
        }

        // Then delete all patients in each room
        for (Room room : ward.getRooms()) {
            patientRepository.deleteAll(room.getPatients());
        }

        // Delete all rooms in the ward
        roomRepository.deleteAll(ward.getRooms());

        // Finally, delete the ward itself
        wardRepository.delete(ward);

        System.out.println("Deleted ward '" + ward.getWardName() + "' and all associated data.");
    }

    /**
     * Retrieves a single {@link Ward} by id.
     *
     * @param wardId the ward’s id
     * @return the corresponding {@link Ward}
     * @throws EntityNotFoundException if no ward exists with the given id
     */
    public Ward findById(Long wardId) {
        return wardRepository.findById(wardId)
                .orElseThrow(() -> new EntityNotFoundException("Ward not found with ID: " + wardId));
    }

    // --------------------- Private Helpers ---------------------
    /**
     * Helper method for mapping a {@link Ward} to a {@link WardFullDTO},
     * including all patients and their daily orders.
     *
     * @param ward the ward entity to map
     * @return a {@link WardFullDTO} with ward name and patient data
     */
    private WardFullDTO mapToWardFullDTO(Ward ward) {
        var patientDTOs = ward.getPatients().stream()
                .map(patient -> {
                    DailyOrder order = dailyOrderService.findTodayOrderForPatient(patient);
                    return PatientMapper.toDailyOrderDTO(patient, ward, order);
                })
                .toList();

        return new WardFullDTO(ward.getWardName(), patientDTOs);
    }
}
