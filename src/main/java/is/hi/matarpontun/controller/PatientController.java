package is.hi.matarpontun.controller;

import is.hi.matarpontun.dto.PatientMealDTO;
import is.hi.matarpontun.dto.WardDTO;
import is.hi.matarpontun.model.Patient;
import is.hi.matarpontun.service.PatientService;
import is.hi.matarpontun.service.WardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private final WardService wardService;

    @Autowired
    public PatientController(WardService wardService) {
        this.wardService = wardService;
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
}


