package is.hi.matarpontun.controller;

import is.hi.matarpontun.dto.*;
import is.hi.matarpontun.model.DailyOrder;
import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.service.DailyOrderService;
import is.hi.matarpontun.service.PatientService;
import is.hi.matarpontun.service.WardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller responsible for handling requests related to patients.
 */
@RestController
@RequestMapping("/patients")
public class PatientController {

    private final WardService wardService;
    private final PatientService patientService;
    private final DailyOrderService dailyOrderService;

    /**
     * Constructs a new {@code PatientController} with required services.
     *
     * @param wardService       the service responsible for business logic related to ward authentication and data access.
     * @param patientService    the service responsible for business logic for patient updates.
     * @param dailyOrderService the service responsible for business logic for managing {@link DailyOrder}s.
     */
    public PatientController(WardService wardService, PatientService patientService,
            DailyOrderService dailyOrderService) {
        this.wardService = wardService;
        this.patientService = patientService;
        this.dailyOrderService = dailyOrderService;
    }

    /**
     * UC8 - Retrieves all patients for a specific ward.
     *
     * @param request ward name and password ({@link WardDTO})
     * @return {@code 200 OK} with the ward’s patients on success
     *         or {@code 404 Not Found} if credentials are invalid
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllPatientsForWard(@RequestBody WardDTO request) {
        var wardPatientsInfo = wardService.signInAndGetData(request.wardName(), request.password());

        if (wardPatientsInfo.isPresent()) {
            return ResponseEntity.ok(wardPatientsInfo.get());
        } else {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Invalid ward name or password"));
        }
    }

    /**
     * UC9 - Retrieves details for a single patient by patient id.
     *
     * @param request ward authentication details
     * @param id      patient id
     * @return {@code 200 OK} with patient data or {@code 404 Not Found} if
     *         not found or the ward is not authorized
     */
    @GetMapping("{id}")
    public ResponseEntity<?> getPatientByIdForWard(@RequestBody WardDTO request,
            @PathVariable Long id) {
        var patientInfo = wardService.signInAndGetPatientData(request.wardName(), request.password(), id);

        if (patientInfo.isPresent()) {
            return ResponseEntity.ok(patientInfo.get());
        } else {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Patient not found for this ward or invalid login"));
        }
    }

    /**
     * UC3 - Adds a restriction and reassigns the patient's food type if a conflict arises.
     *
     * @param id      patient id
     * @param request JSON body containing {@code "restriction"}
     * @return {@code 200 OK} with a {@link RestrictionCheckResultDTO} summarizing the update
     *         or {@code 400 Bad Request} if the restriction is missing
     */
    @PostMapping("/{id}/restrictions/addAndReassign")
    public ResponseEntity<?> addRestrictionAndReassign(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String restriction = request.get("restriction");
        if (restriction == null || restriction.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing restriction"));
        }

        // Add the restriction to the patient
        Patient updated = patientService.addRestriction(id, restriction);

        // Recheck today's daily order for conflicts
        DailyOrder order = dailyOrderService.checkForConflicts(id);

        // Return a detailed result
        return ResponseEntity.ok(new RestrictionCheckResultDTO(
                updated,
                order,
                "Restriction added and daily order rechecked."));
    }

    /**
     * UC12 - Adds a single restriction to the patient's restriction list.
     * POST {@code /patients/{id}/restrictions/add}
     *
     * @param id      patient id
     * @param request JSON body containing {@code "restriction"}
     * @return {@code 200 OK} with {@link PatientDailyOrderDTO}
     */
    @PostMapping("/{id}/restrictions/add")
    public ResponseEntity<PatientDailyOrderDTO> addRestriction(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String restriction = request.get("restriction");
        Patient updatedPatient = patientService.addRestriction(id, restriction);
        DailyOrder order = dailyOrderService.findTodayOrderForPatient(updatedPatient);

        PatientDailyOrderDTO dto = PatientMapper.toDailyOrderDTO(
                updatedPatient,
                updatedPatient.getWard(),
                order);

        return ResponseEntity.ok(dto);
    }

    /**
     * Removes one or more restrictions from a patient's restrictions list.
     *
     * @param id   patient id
     * @param body JSON body with {@code "remove"} as a list of restrictions.
     * @return {@code 200 OK} with {@link PatientDailyOrderDTO}
     */
    @PatchMapping("/{id}/restrictions/remove")
    public ResponseEntity<PatientDailyOrderDTO> removeRestrictions(
            @PathVariable Long id,
            @RequestBody Map<String, java.util.List<String>> body) {

        java.util.List<String> toRemove = body.get("remove");
        Patient updated = patientService.removeRestrictions(id, toRemove);

        DailyOrder order = dailyOrderService.findTodayOrderForPatient(updated);
        PatientDailyOrderDTO dto = PatientMapper.toDailyOrderDTO(updated, updated.getWard(), order);

        return ResponseEntity.ok(dto);
    }

    /**
     * Removes all restrictions from a patient's restriction list.
     *
     * @param id patient id
     * @return {@code 200 OK} with {@link PatientDailyOrderDTO}
     */
    @DeleteMapping("/{id}/restrictions")
    public ResponseEntity<PatientDailyOrderDTO> clearAllRestrictions(@PathVariable Long id) {
        Patient updated = patientService.clearAllRestrictions(id);

        DailyOrder order = dailyOrderService.findTodayOrderForPatient(updated);
        PatientDailyOrderDTO dto = PatientMapper.toDailyOrderDTO(updated, updated.getWard(), order);

        return ResponseEntity.ok(dto);
    }

    /**
     * Removes one or more allergies from a patient's allergy list.
     *
     * @param id   patient id
     * @param body JSON body with {@code "remove"} as a list of allergies.
     * @return {@code 200 OK} with {@link PatientDailyOrderDTO}
     */
    @PatchMapping("/{id}/allergies/remove")
    public ResponseEntity<PatientDailyOrderDTO> removeAllergy(
            @PathVariable Long id,
            @RequestBody Map<String, java.util.List<String>> body) {

        java.util.List<String> toRemove = body.get("remove");
        Patient updated = patientService.removeAllergies(id, toRemove);

        DailyOrder order = dailyOrderService.findTodayOrderForPatient(updated);
        PatientDailyOrderDTO dto = PatientMapper.toDailyOrderDTO(updated, updated.getWard(), order);

        return ResponseEntity.ok(dto);
    }

    /**
     * Removes all allergies from a patient's allergy list.
     *
     * @param id patient id
     * @return {@code 200 OK} with {@link PatientDailyOrderDTO}
     */
    @DeleteMapping("/{id}/allergies")
    public ResponseEntity<PatientDailyOrderDTO> clearAllAllergies(@PathVariable Long id) {
        Patient updated = patientService.clearAllAllergies(id);

        DailyOrder order = dailyOrderService.findTodayOrderForPatient(updated);
        PatientDailyOrderDTO dto = PatientMapper.toDailyOrderDTO(updated, updated.getWard(), order);

        return ResponseEntity.ok(dto);
    }

    /**
     * UC1 - Orders a specific food type for a patient, updates the patient's food type,
     * and creates today's {@link DailyOrder}.
     *
     * @param id   patient id
     * @param body JSON body with {@code "foodType"}
     * @return {@code 200 OK} with a confirmation message describing the created order
     *         or {@code 400 Bad Request} when {@code "foodType"} is missing.
     */
    @PostMapping("{id}/order")
    public ResponseEntity<?> orderFoodForPatient(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String foodType = body.get("foodType");
        if (foodType == null || foodType.isBlank()) {
            return ResponseEntity.badRequest().body("Missing foodType");
        }
        // Update patient's food type using the service
        patientService.updatePatientFoodType(id, foodType);

        // Order daily meals for the patient
        DailyOrder order = dailyOrderService.orderFoodTypeForPatient(id);

        return ResponseEntity.ok(Map.of(
                "message", "Daily order created for patient",
                "patientId", id,
                "foodType", order.getFoodType() != null ? order.getFoodType().getTypeName() : "",
                "orderDate", order.getOrderDate().toString(),
                "meals", Map.of(
                        "breakfast", order.getBreakfast() != null ? order.getBreakfast().getName() : "N/A",
                        "lunch", order.getLunch() != null ? order.getLunch().getName() : "N/A",
                        "afternoonSnack",
                        order.getAfternoonSnack() != null ? order.getAfternoonSnack().getName() : "N/A",
                        "dinner", order.getDinner() != null ? order.getDinner().getName() : "N/A",
                        "nightSnack", order.getNightSnack() != null ? order.getNightSnack().getName() : "N/A"),
                "status", order.getStatus()));
    }

    /**
     * UC14 - Fixes conflicts in a patient's daily order by checking restrictions and
     * automatically replacing meals when possible.
     *
     * @param id patient id
     * @return {@code 200 OK} with a message, status and {@link PatientDailyOrderDTO} or
     *         {@code 400 Bad Request} if the check fails.
     */
    @PatchMapping("/{id}/fixConflicts")
    public ResponseEntity<?> fixConflicts(@PathVariable Long id) {
        try {
            DailyOrder updatedOrder = dailyOrderService.checkForConflicts(id);

            String message;
            switch (updatedOrder.getStatus()) {
                case "AUTO CHANGED" -> message = "Conflicts found and meals automatically adjusted.";
                case "NEEDS MANUAL CHANGE" -> message = "Conflicts found — manual change required.";
                default -> message = "No conflicts found.";
            }

            // Build DTO for clean structured response
            Patient patient = updatedOrder.getPatient();
            PatientDailyOrderDTO dto = PatientMapper.toDailyOrderDTO(
                    patient,
                    patient.getWard(),
                    updatedOrder);

            return ResponseEntity.ok(Map.of(
                    "message", message,
                    "status", updatedOrder.getStatus(),
                    "data", dto));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Unable to check conflicts: " + e.getMessage()));
        }
    }

    /**
     * UC13 — Deletes today's daily order for a specific patient.
     * <p>
     * Allows ward staff to remove the current day's {@link DailyOrder} for the given patient.
     *
     * @param id patient id
     * @return {@code 200 OK} with a confirmation message if the order was deleted
     *         or {@code 404 Not Found} if no order existed for today.
     */
    @DeleteMapping("/{id}/order/today")
    public ResponseEntity<?> deleteTodaysOrder(@PathVariable Long id) {
        boolean deleted = dailyOrderService.deleteTodaysOrderForPatient(id);

        if (deleted) {
            return ResponseEntity.ok(Map.of(
                    "message", "Today's order deleted for patient ID " + id));
        } else {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "No order found for patient ID " + id + " today"));
        }
    }

}
