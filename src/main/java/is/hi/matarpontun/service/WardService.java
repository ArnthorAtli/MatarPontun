package is.hi.matarpontun.service;

import is.hi.matarpontun.dto.*;
import is.hi.matarpontun.dto.PatientMapper;
import is.hi.matarpontun.model.*;
import is.hi.matarpontun.repository.*;

import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class WardService {

    protected static final Logger log = LoggerFactory.getLogger(WardService.class);

    private final WardRepository wardRepository;
    private final DailyOrderService dailyOrderService;
    private final RoomRepository roomRepository;
    private final PatientRepository patientRepository;

    public WardService(WardRepository wardRepository,
                       DailyOrderService dailyOrderService,
                       RoomRepository roomRepository,
                       PatientRepository patientRepository) {
        this.wardRepository = wardRepository;
        this.dailyOrderService = dailyOrderService;
        this.roomRepository = roomRepository;
        this.patientRepository = patientRepository;
    }

    // UC2 - Generate meal orders for this ward and return grouped summary
    public OrderDTO generateDailyOrdersForWard(Long wardId) {
        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new EntityNotFoundException("Ward not found: " + wardId));

        log.info("Generating daily orders for ward: {}", ward.getWardName());
        return dailyOrderService.generateOrdersForWard(ward);
    }

    public Ward createWard(Ward ward) {
        if (wardRepository.findByWardName(ward.getWardName()).isPresent()) {
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

    // UC6: Update ward info
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

    // UC16 – Get summary for a single ward by ID
    @Transactional
    public WardSummaryDTO getWardSummaryById(Long wardId) {
        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new EntityNotFoundException("Ward not found"));

        int rooms = (int) roomRepository.countByWard_Id(wardId);
        int patients = (int) patientRepository.countByWard_Id(wardId);

        return new WardSummaryDTO(ward.getId(), ward.getWardName(), rooms, patients);
    }

    @Transactional
    public void deleteWardCascade(Long wardId) {
        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new EntityNotFoundException("Ward not found with ID: " + wardId));

        // Delete all patients in all rooms of the ward
        for (Room room : ward.getRooms()) {
            patientRepository.deleteAll(room.getPatients());
        }

        // Delete all rooms in the ward
        roomRepository.deleteAll(ward.getRooms());

        // Delete the ward itself
        wardRepository.delete(ward);
    }

    // --------------------- Private Helpers ---------------------

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
