package is.hi.matarpontun.controller;

import is.hi.matarpontun.dto.WardDTO;
import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.service.PatientService;
import is.hi.matarpontun.service.WardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private final WardService wardService;
    private final PatientService patientService;

    // depends á WardService því viljum að aðeins logged-in wards geti nálgast uppls.
    @Autowired
    public PatientController(WardService wardService, PatientService patientService) {
        this.wardService = wardService;
        this.patientService = patientService;
    }

    // UC8 - to fetch patients for a ward
    // For now, we identify the ward by asking for wardName + password again in the request.
    // Later, when we add tokens (e.g. JWT), this controller method will stay almost identical.
    // The only difference is: instead of @RequestParam wardName/password,
    // we will look up the ward based on the token in the Authorization header.
    @GetMapping("/all")
    public ResponseEntity<?> getAllPatientsForWard(@RequestBody WardDTO request) {
        var wardOpt =  wardService.signInAndGetData(request.wardName(), request.password());

        if (wardOpt.isPresent()) {
            return ResponseEntity.ok(wardOpt.get());
        } else {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Invalid ward name or password"));
        }
    }

    // UC9: fetch single patient by ID
    @GetMapping("{id}")
    public ResponseEntity<?> getPatientByIdForWard(@RequestBody WardDTO request,
                                                   @PathVariable Long id) {
        var patientOpt = wardService.signInAndGetPatientData(request.wardName(), request.password(), id);

        if (patientOpt.isPresent()) {
            return ResponseEntity.ok(patientOpt.get());
        } else {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Patient not found for this ward or invalid login"));
        }
    }

    /**
     * UC12 – Add a single restriction string to the patient's restriction list.
     * Example request:
     *   POST /patients/3/restrictions/add
     *   { "restriction": "no sugar" }
     */
    @PostMapping("/{id}/restrictions/add")
    public ResponseEntity<Patient> addRestriction(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String restriction = request.get("restriction");
        Patient updated = patientService.addRestriction(id, restriction);
        return ResponseEntity.ok(updated);
    }

    //Remove one or more restrictions
    //Body: { "remove": ["no sugar", "no dairy"] }
    @PatchMapping("/{id}/restrictions/remove")
    public ResponseEntity<Patient> removeRestrictions(
            @PathVariable Long id,
            @RequestBody Map<String, java.util.List<String>> body) {

        java.util.List<String> toRemove = body.get("remove");
        Patient updated = patientService.removeRestrictions(id, toRemove);
        return ResponseEntity.ok(updated);
    }

    //Remove all restrictions
    @DeleteMapping("/{id}/restrictions")
    public ResponseEntity<Patient> clearAllRestrictions(@PathVariable Long id) {
        Patient updated = patientService.clearAllRestrictions(id);
        return ResponseEntity.ok(updated);
    }

    // Add an allergy to a patient
    @PostMapping("/{id}/allergies/add")
    public ResponseEntity<Patient> addAllergy(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String allergy = request.get("allergy");
        Patient updated = patientService.addAllergy(id, allergy);
        return ResponseEntity.ok(updated);
    }

    //Remove one or more allergy
    //Body: { "remove": ["no sugar", "no dairy"] }
    @PatchMapping("/{id}/allergies/remove")
    public ResponseEntity<Patient> removeAllergy(
            @PathVariable Long id,
            @RequestBody Map<String, java.util.List<String>> body) {

        java.util.List<String> toRemove = body.get("remove");
        Patient updated = patientService.removeAllergies(id, toRemove);
        return ResponseEntity.ok(updated);
    }

    //Remove all restrictions
    @DeleteMapping("/{id}/allergies")
    public ResponseEntity<Patient> clearAllAllergies(@PathVariable Long id) {
        Patient updated = patientService.clearAllAllergies(id);
        return ResponseEntity.ok(updated);
    }


}


