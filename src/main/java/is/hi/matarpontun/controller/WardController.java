package is.hi.matarpontun.controller;

import is.hi.matarpontun.dto.*;
import is.hi.matarpontun.model.Ward;
import is.hi.matarpontun.service.DailyOrderService;
import is.hi.matarpontun.service.WardService;
import is.hi.matarpontun.service.RoomService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import is.hi.matarpontun.security.JwtTokenUtil;

@RestController
@RequestMapping("/wards")
public class WardController {

    private final WardService wardService;
    private final RoomService roomService;
    private final DailyOrderService dailyOrderService;
    private final JwtTokenUtil jwtTokenUtil;

    public WardController(WardService wardService, RoomService roomService, DailyOrderService dailyOrderService,
            JwtTokenUtil jwtTokenUtil) {
        this.wardService = wardService;
        this.roomService = roomService;
        this.dailyOrderService = dailyOrderService;
        this.jwtTokenUtil = jwtTokenUtil;
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
     * +
     * 
     * @param request the ward registration information: name and password
     * @return the created ward containing the new ward's ID and name
     */
    @PostMapping
    public ResponseEntity<WardDTO> createWard(@RequestBody WardDTO request) {
        Ward savedWard = wardService.createWard(new Ward(request.wardName(), request.password()));
        return ResponseEntity.ok(new WardDTO(savedWard.getId(), savedWard.getWardName(), null));
    }


    /**
     * UC5 & UC19 – Signs in a ward by validating credentials and creates a token.
     *
     * @param request contains the ward name and password
     * @return message if login was successful or not
     */
    @PostMapping("/signIn")
    public ResponseEntity<?> signIn(@RequestBody WardDTO request) {
        return wardService.signInAndGetData(request.wardName(), request.password())
                .map(ward -> {
                    // Generate JWT for this session, it works for all endpoints
                    String token = jwtTokenUtil.generateToken(ward.getWardName());

                    return ResponseEntity.ok(Map.of(
                            "message", "Login successful",
                            "wardName", ward.getWardName(),
                            "token", token));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid ward name or password")));
    }

    /**
     * UC6 – Updates information for an existing ward.
     *
     * @param id      the identifier of the ward
     * @param request update request containing the new data (e.g., name, password)
     * @return 200 OK with the updated information
     */
    @PutMapping("/{id}")
    public ResponseEntity<WardDTO> updateWard(
            @PathVariable Long id,
            @RequestBody WardUpdateDTO request) {

        Ward updated = wardService.updateWard(id, request);
        return ResponseEntity.ok(new WardDTO(updated.getId(), updated.getWardName(), null));
    }

    /**
     * UC2 – Places daily orders for all patients in the given ward.
     *
     * @param id the ward's unique identifier
     * @return 200 OK with a JSON object containing confirmation message, ward name
     *         and room information
     */
    @PostMapping("/{id}/order")
    public ResponseEntity<?> orderMealsForWard(@PathVariable Long id) {
        OrderDTO order = wardService.generateDailyOrdersForWard(id); // hér fer pöntunin fram

        return ResponseEntity.ok(Map.of(
                "message", "Daily orders successfully created and logged.",
                "ward", order.wardName(),
                "rooms", order.rooms()));
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

    // UC15 Create a ward with rooms and patients in one request
    @PostMapping("/createFullWard")
    public ResponseEntity<?> createFullWard(@RequestBody WardCreateRequestDTO request) {
        // Create the ward first
        Ward savedWard = wardService.createWard(new Ward(request.wardName(), request.password()));

        // Create rooms + patients
        int numberOfRooms = request.numberOfRooms();
        int patientsPerRoom = request.patientsPerRoom();

        for (int i = 1; i <= numberOfRooms; i++) {
            String roomNumber = "Room-" + (savedWard.getId() * 100 + i);
            roomService.createRoomAndFillWithPatients(patientsPerRoom, savedWard.getId(), roomNumber);
        }

        // Build response
        return ResponseEntity.ok(Map.of(
                "message", "Ward created successfully with rooms and patients.",
                "wardId", savedWard.getId(),
                "wardName", savedWard.getWardName(),
                "roomsCreated", numberOfRooms,
                "patientsPerRoom", patientsPerRoom));
    }

    // UC17: Delete a ward along with all its rooms and patients
    @DeleteMapping("/{wardId}")
    public ResponseEntity<?> deleteWard(@PathVariable Long wardId) {
        wardService.deleteWardCascade(wardId);
        return ResponseEntity.ok(Map.of("message", "Ward and all associated rooms and patients deleted successfully."));
    }

    // UC18: Delete a room along with all its patients
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long roomId) {
        try {
            Map<String, Object> result = roomService.deleteRoomAndPatients(roomId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * UC16 – Fetches a summary for a specific ward.
     *
     * @param wardId the ID of the ward
     * @return 200 OK with information about the ward, or 404 Not Found if the ward
     *         does not exist
     */
    @GetMapping("/summary/{wardId}")
    public ResponseEntity<?> getWardSummary(@PathVariable Long wardId) {
        try {
            WardSummaryDTO dto = wardService.getWardSummaryById(wardId);
            return ResponseEntity.ok(dto);
        } catch (jakarta.persistence.EntityNotFoundException ex) {
            return ResponseEntity.status(404).body(Map.of("error", "Ward not found"));
        }
    }

    // UC10 - Fetch filtered data via query parameters (e.g. by-category, by-date)
    // (GET)
    @GetMapping("/{wardId}/orders")
    public ResponseEntity<?> getFilteredOrdersForWard(
            @PathVariable Long wardId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String foodType,
            @RequestParam(required = false) String status) {

        // Parse date
        LocalDate parsedDate = null;
        if (date != null && !date.isBlank()) {
            try {
                parsedDate = LocalDate.parse(date);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid date format. Use yyyy-MM-dd"));
            }
        }

        // Fetch ward
        Ward ward = wardService.findById(wardId);
        String wardName = ward.getWardName();

        // Fetch filtered orders via DTO method
        List<DailyOrderSummaryDTO> filteredOrders = dailyOrderService.getFilteredOrdersDTO(parsedDate, foodType,
                wardName, status);

        // Build response
        Map<String, Object> filters = new HashMap<>();
        filters.put("date", date);
        filters.put("foodType", foodType);
        filters.put("status", status);

        Map<String, Object> response = new HashMap<>();
        response.put("wardId", wardId);
        response.put("wardName", wardName);
        response.put("filters", filters);
        response.put("orders", filteredOrders);

        return ResponseEntity.ok(response);
    }

    // Endpoint to check if the user is authenticated
    @GetMapping("/isAuthenticated")
    public ResponseEntity<?> isAuthenticated() {
        return ResponseEntity.ok(Map.of("authenticated", true));
    }

}