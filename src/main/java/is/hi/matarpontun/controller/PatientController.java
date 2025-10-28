package is.hi.matarpontun.controller;

import is.hi.matarpontun.dto.*;
import is.hi.matarpontun.model.MealOrder;
import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.service.MealOrderService;
import is.hi.matarpontun.service.PatientService;
import is.hi.matarpontun.service.WardService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private final WardService wardService;
    private final PatientService patientService;
    private final MealOrderService mealOrderService;

    // depends á WardService því viljum að aðeins logged-in wards geti nálgast uppls.
    public PatientController(WardService wardService, PatientService patientService, MealOrderService mealOrderService) {
        this.wardService = wardService;
        this.patientService = patientService;
        this.mealOrderService = mealOrderService;
    }

    // UC8 - to fetch patients for a ward
    // For now, we identify the ward by asking for wardName + password again in the request.
    // Later, when we add tokens (e.g. JWT), this controller method will stay almost identical.
    // The only difference is: instead of @RequestParam wardName/password,
    // we will look up the ward based on the token in the Authorization header.
    /**
     * UC8 – Retrieves all patients for a specific ward.
     *
     * @param request contains the ward name and password for authentication
     * @return a list of patients for the ward if credentials are valid
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllPatientsForWard(@RequestBody WardDTO request) {
        var wardPatientsInfo =  wardService.signInAndGetData(request.wardName(), request.password());

        if (wardPatientsInfo.isPresent()) {
            return ResponseEntity.ok(wardPatientsInfo.get());
        } else {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Invalid ward name or password"));
        }
    }

    /**
     * UC9 – Retrieves details for a single patient by patient id.
     *
     * @param request the ward authentication details
     * @param id      the patient ID
     * @return the patient data if found and authorized
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
     * @param id       the patient's ID
     * @param request  contains the restriction that should be added
     * @return a result DTO describing whether a reassignment was needed
     */
    @PostMapping("/{id}/restrictions/addAndReassign")
    public ResponseEntity<RestrictionUpdateResultDTO> addRestrictionAndReassign(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String restriction = request.get("restriction");
        if (restriction == null || restriction.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        RestrictionUpdateResultDTO result = patientService.addRestrictionAndReassignFoodType(id, restriction);
        return ResponseEntity.ok(result);
    }

    /**
     * UC12 – Add a single restriction string to the patient's restriction list.
     * Example request:
     *   POST /patients/3/restrictions/add
     *   { "restriction": "ig3" }
     */
    @PostMapping("/{id}/restrictions/add")
    public ResponseEntity<PatientMealDTO> addRestriction(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String restriction = request.get("restriction");
        Patient updated = patientService.addRestriction(id, restriction);
        return ResponseEntity.ok(patientService.mapToPatientMealDTO(updated));
    }

    /**
     * Removes one or more restrictions from a patient.
     *
     * @param id    the patient's ID
     * @param body  contains a list of restrictions to remove
     * @return updated patient meal information
     */
    @PatchMapping("/{id}/restrictions/remove")
    public ResponseEntity<PatientMealDTO> removeRestrictions(
            @PathVariable Long id,
            @RequestBody Map<String, java.util.List<String>> body) {

        java.util.List<String> toRemove = body.get("remove");
        Patient updated = patientService.removeRestrictions(id, toRemove);
        return ResponseEntity.ok(patientService.mapToPatientMealDTO(updated));
    }

    /**
     * Removes all restrictions from a patient.
     *
     * @param id the patient's ID
     * @return updated patient meal information
     */
    @DeleteMapping("/{id}/restrictions")
    public ResponseEntity<PatientMealDTO> clearAllRestrictions(@PathVariable Long id) {
        Patient updated = patientService.clearAllRestrictions(id);
        return ResponseEntity.ok(patientService.mapToPatientMealDTO(updated));
    }

    /**
     * UC 3 - Adds an allergy to a patient's allergy list and reasings the diet for one spesifc meal if conflict.
     *
     * @param id       the patient's ID
     * @param request  contains the allergy string that should be added
     * @return updated patient meal information
     */
    @PostMapping("/{id}/allergies/add")
    public ResponseEntity<PatientMealDTO> addAllergy(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String allergy = request.get("allergy");
        Patient updated = patientService.addAllergy(id, allergy);
        return ResponseEntity.ok(patientService.mapToPatientMealDTO(updated));
    }

    /**
     * Removes one or more allergies from a patient's allergy list.
     *
     * @param id    the patient's ID
     * @param body  contains a list of allergies that should be removed
     * @return updated patient meal information
     */
    @PatchMapping("/{id}/allergies/remove")
    public ResponseEntity<PatientMealDTO> removeAllergy(
            @PathVariable Long id,
            @RequestBody Map<String, java.util.List<String>> body) {

        java.util.List<String> toRemove = body.get("remove");
        Patient updated = patientService.removeAllergies(id, toRemove);
        return ResponseEntity.ok(patientService.mapToPatientMealDTO(updated));
    }

    /**
     * Removes all allergies from a patient's restriction list.
     *
     * @param id the patient's ID
     * @return updated patient meal information
     */
    @DeleteMapping("/{id}/allergies")
    public ResponseEntity<PatientMealDTO> clearAllAllergies(@PathVariable Long id) {
        Patient updated = patientService.clearAllAllergies(id);
        return ResponseEntity.ok(patientService.mapToPatientMealDTO(updated));
    }

    /**
     * Manually changes the next meal's food type for a patient.
     *
     * @param patientId the patient's ID
     * @param request   contains the new food type
     * @return a confirmation message
     */
   @PatchMapping("/{id}/change-next-meal")
    public ResponseEntity<?> changeNextMeal(
            @PathVariable("id") Long patientId,
            @RequestBody ManualFoodTypeChangeDTO request) {

        // Call the service method to perform the logic
        String successMessage = mealOrderService.manuallyChangeNextMeal(patientId, request.newFoodTypeName());
        // Return the success message from the service in a simple JSON object
        return ResponseEntity.ok(Map.of("message", successMessage));
    }

    /**
     * UC1 – Orders a specific food type for a patient.
     *
     * @param id    the patient's ID
     * @param body  contains the chosen food type name
     * @return a success message if the order was made, the
     * patient's ID and the food type, otherwise an error message
     */
    @PostMapping("{id}/order")
    public ResponseEntity<?> orderFoodForPatient(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String foodType = body.get("foodType");
        if (foodType == null || foodType.isBlank()) {
            return ResponseEntity.badRequest().body("Missing foodType");
        }

        // Call the service method
        MealOrder order = mealOrderService.orderFoodTypeForPatient(id, foodType);

        return ResponseEntity.ok(Map.of(
                "message", "Order logged and sent to kitchen",
                "patientId", id,
                "mealType", order.getMealType(),
                "foodType", order.getFoodType().getTypeName(),
                "status", order.getStatus()
        ));
    }
}


