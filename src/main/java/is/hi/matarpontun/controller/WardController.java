package is.hi.matarpontun.controller;

import is.hi.matarpontun.dto.PatientMealDTO;
import is.hi.matarpontun.dto.WardDTO;
import is.hi.matarpontun.dto.WardSummaryDTO;
import is.hi.matarpontun.dto.WardUpdateDTO;
import is.hi.matarpontun.model.Ward;
import is.hi.matarpontun.service.WardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/wards")
public class WardController {

    private final WardService wardService;

    public WardController(WardService wardService) {
        this.wardService = wardService;
    }

    /**
     * Default welcome.
     *
     * @return a welcome message
     */
    @GetMapping
    public ResponseEntity<?> welcome() {
        return ResponseEntity.ok(Map.of("message", "Welcome to the Hospital Meal Ordering System!"));
    }

    /**
     * UC4 – Creates a new ward account.
     +
     * @param request the ward registration information: name and password
     * @return the created ward containing the new ward's ID and name
     */
    @PostMapping
    public ResponseEntity<WardDTO> createWard(@RequestBody WardDTO request) {
        Ward savedWard = wardService.createWard(new Ward(request.wardName(), request.password()));
        return ResponseEntity.ok(new WardDTO(savedWard.getId(), savedWard.getWardName(), null));
    }

    // UC5 - sign inn, skilar bara success eða error (Boolean), bæta við tokan seinna
    /**
     * UC5 – Signs in a ward by validating credentials.
     *
     * @param request contains the ward name and password
     * @return message if login was successful or not
     */
    @PostMapping("/signIn")
    public ResponseEntity<?> signIn(@RequestBody WardDTO request) {
        return wardService.signInAndGetData(request.wardName(), request.password())
                .map(ward -> ResponseEntity.ok(Map.of("message", "Login successful")))
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "Invalid ward name or password")));
    }

    // (Admin/debug helper only)
    @GetMapping("/all-data")
    public List<Ward> getAllData() {
        return wardService.findAllWards();
    }

    // UC6 – Modify account information
    @PutMapping("/{id}")
    public ResponseEntity<WardDTO> updateWard(
            @PathVariable Long id,
            @RequestBody WardUpdateDTO request) {

        Ward updated = wardService.updateWard(id, request);
        return ResponseEntity.ok(new WardDTO(updated.getId(), updated.getWardName(), null));
    }

    //UC2 - Order meal at mealtime
    @GetMapping("/{wardId}/order")
    public ResponseEntity<?> orderMealsForWard(@PathVariable Long wardId) {
        List<PatientMealDTO> patients = wardService.generateMealOrdersForWard(wardId);

        if (patients.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "message", "No meals were ordered. Possibly no suitable meals found for this ward."
            ));
        }

        Map<String, Object> response = new LinkedHashMap<>(); // preserves key order
        response.put("message", "Meal orders successfully created and logged.");
        response.put("totalPatients", patients.size());
        response.put("patients", patients);

        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        // Return a 409 Conflict status with the error message from the service
        return ResponseEntity.status(409).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }

    // UC16 – Fetch summary for a single ward (GET)
    @GetMapping("/summary/{wardId}")
    public ResponseEntity<?> getWardSummary(@PathVariable Long wardId) {
        try {
            WardSummaryDTO dto = wardService.getWardSummaryById(wardId);
            return ResponseEntity.ok(dto);
        } catch (jakarta.persistence.EntityNotFoundException ex) {
            return ResponseEntity.status(404).body(Map.of("error", "Ward not found"));
        }
    }
}