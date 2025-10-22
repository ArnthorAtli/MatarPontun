package is.hi.matarpontun.controller;

import is.hi.matarpontun.dto.ManualFoodTypeChangeDTO;
import is.hi.matarpontun.dto.PatientMealDTO;
import is.hi.matarpontun.dto.RestrictionUpdateResultDTO;
import is.hi.matarpontun.dto.WardDTO;
import is.hi.matarpontun.model.MealOrder;
import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.service.MealOrderService;
import is.hi.matarpontun.service.PatientService;
import is.hi.matarpontun.service.WardService;
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

    // UC9: fetch single patient by ID
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

    //Remove one or more restrictions
    //Body: { "remove": ["no sugar", "no dairy"] }
    @PatchMapping("/{id}/restrictions/remove")
    public ResponseEntity<PatientMealDTO> removeRestrictions(
            @PathVariable Long id,
            @RequestBody Map<String, java.util.List<String>> body) {

        java.util.List<String> toRemove = body.get("remove");
        Patient updated = patientService.removeRestrictions(id, toRemove);
        return ResponseEntity.ok(patientService.mapToPatientMealDTO(updated));
    }

    //Remove all restrictions
    @DeleteMapping("/{id}/restrictions")
    public ResponseEntity<PatientMealDTO> clearAllRestrictions(@PathVariable Long id) {
        Patient updated = patientService.clearAllRestrictions(id);
        return ResponseEntity.ok(patientService.mapToPatientMealDTO(updated));
    }

    // Add an allergy to a patient
    @PostMapping("/{id}/allergies/add")
    public ResponseEntity<PatientMealDTO> addAllergy(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String allergy = request.get("allergy");
        Patient updated = patientService.addAllergy(id, allergy);
        return ResponseEntity.ok(patientService.mapToPatientMealDTO(updated));
    }

    //Remove one or more allergy
    //Body: { "remove": ["no sugar", "no dairy"] }
    @PatchMapping("/{id}/allergies/remove")
    public ResponseEntity<PatientMealDTO> removeAllergy(
            @PathVariable Long id,
            @RequestBody Map<String, java.util.List<String>> body) {

        java.util.List<String> toRemove = body.get("remove");
        Patient updated = patientService.removeAllergies(id, toRemove);
        return ResponseEntity.ok(patientService.mapToPatientMealDTO(updated));
    }

    //Remove all restrictions
    @DeleteMapping("/{id}/allergies")
    public ResponseEntity<PatientMealDTO> clearAllAllergies(@PathVariable Long id) {
        Patient updated = patientService.clearAllAllergies(id);
        return ResponseEntity.ok(patientService.mapToPatientMealDTO(updated));
    }

    //UC3 - Manually change the next meal's food type for a patient
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

        boolean ok = patientService.orderFood(id, foodType);
        if (ok) {
            return ResponseEntity.ok(
                    java.util.Map.of(
                            "message", "Order has been made",
                            "patientId", id,
                            "foodType", body.get("foodType")
                    )
            );
        }

        return ResponseEntity.badRequest().body("Order failed");
    }

}


