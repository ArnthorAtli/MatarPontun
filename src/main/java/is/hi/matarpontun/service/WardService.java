package is.hi.matarpontun.service;

import is.hi.matarpontun.dto.*;
import is.hi.matarpontun.model.*;
import is.hi.matarpontun.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WardService {

    protected static final Logger log = LoggerFactory.getLogger(WardService.class);

    private final WardRepository wardRepository;
    private final MealOrderService mealOrderService;
    private final PatientService patientService;
    private final RoomRepository roomRepository;
    private final PatientRepository patientRepository;

    public WardService(WardRepository wardRepository,
                       MealOrderService mealOrderService,
                       PatientService patientService,
                       RoomRepository roomRepository,
                       PatientRepository patientRepository) {
        this.wardRepository = wardRepository;
        this.mealOrderService = mealOrderService;
        this.patientService = patientService;
        this.roomRepository = roomRepository;
        this.patientRepository = patientRepository;
    }

    // UC2 - Generate meal orders for this ward and return grouped summary
    // Responsible for what to order (ward selection), not how to order.
    // vil bæta við log: log.info("Generating meal orders for ward: {}", ward.getWardName());
    public OrderDTO generateMealOrdersForWard(Long wardId) {
        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new EntityNotFoundException("Ward not found: " + wardId));

        log.info("Generating meal orders for ward: {}", ward.getWardName());
        return mealOrderService.generateOrdersForWard(ward);
    }

    public Ward createWard(Ward ward) {
        // Check if a ward with the given name already exists
        if (wardRepository.findByWardName(ward.getWardName()).isPresent()) {
            // If it exists, throw an exception with a clear message
            throw new IllegalArgumentException("A ward with the name '" + ward.getWardName() + "' already exists.");
        }
        
        return wardRepository.save(ward);
    }

    public List<Ward> findAllWards() {
        return wardRepository.findAll();
    }

    // UC8: Ward login + fetch all patient data
    public Optional<WardFullDTO> signInAndGetData(String wardName, String password) {
        return wardRepository.findByWardNameAndPassword(wardName, password)
                .map(this::mapToWardFullDTO);
    }

    // UC9: Ward login + fetch single patient by ID
    public Optional<PatientMealDTO> signInAndGetPatientData(String wardName, String password, Long patientId) {
        return wardRepository.findByWardNameAndPassword(wardName, password)
                .flatMap(ward -> ward.getPatients().stream()
                        .filter(p -> p.getPatientID().equals(patientId))
                        .findFirst()
                        .map(patientService::mapToPatientMealDTO));
    }

    // UC6: update ward
    @Transactional
    public Ward updateWard(Long id, WardUpdateDTO req) {
        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ward not found"));

        if (req.wardName() == null || req.wardName().isBlank()) {
            throw new IllegalArgumentException("Ward name cannot be empty.");
        }
        if (wardRepository.existsByWardNameAndIdNot(req.wardName(), id)) {
            throw new IllegalArgumentException(
                    "A ward with the name '" + req.wardName() + "' already exists.");
        }
        ward.setWardName(req.wardName());

        if (req.password() == null || req.password().isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        ward.setPassword(req.password());
        return wardRepository.save(ward);
    }

    // UC16 – Get summary for a single ward by ID
    @Transactional(readOnly = true)
    public WardSummaryDTO getWardSummaryById(Long wardId) {
        var ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new EntityNotFoundException("Ward not found"));

        int rooms = (int) roomRepository.countByWard_Id(wardId);
        int patients = (int) patientRepository.countByWard_Id(wardId);

        return new WardSummaryDTO(
                ward.getId(),
                ward.getWardName(),
                rooms,
                patients
        );
    }

    // --------------------- Private Helpers ---------------------
    private WardFullDTO mapToWardFullDTO(Ward ward) {
        var patientDTOs = ward.getPatients().stream()
                .map(patientService::mapToPatientMealDTO)
                .toList();

        return new WardFullDTO(ward.getWardName(), patientDTOs);
    }
}

