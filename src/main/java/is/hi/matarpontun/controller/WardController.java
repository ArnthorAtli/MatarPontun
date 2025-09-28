package is.hi.matarpontun.controller;

import is.hi.matarpontun.model.Ward;
import is.hi.matarpontun.service.WardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/wards")
public class WardController {

    @Autowired
    private WardService wardService;

    //sunna setti skoða
    public WardController(WardService wardService) {
        this.wardService = wardService;
    }

    // default viðmót
    @GetMapping
    public ResponseEntity<?> welcome() {
        return ResponseEntity.ok(Map.of("message", "Welcome to the Hospital Meal Ordering System!"));
    }

    // UC4 - create ward account
    @PostMapping
    public ResponseEntity<Ward> createWard(@RequestParam String wardName,
                                           @RequestParam String password) {
        Ward newWard = new Ward(wardName, password);
        Ward savedWard = wardService.createWard(newWard);
        return ResponseEntity.ok(savedWard);
    }

    @PostMapping("/signIn")
    public ResponseEntity<?> signIn(@RequestParam String wardName, @RequestParam String password) {
        Optional<Ward> ward = wardService.signIn(wardName, password);

        if (ward.isPresent()) {
            return ResponseEntity.ok(Map.of("message", "Login successful"));
        } else {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("error", "Invalid ward name or password"));
        }
    }

    // UC8 - to fetch all data
    // For now, we identify the ward by asking for wardName + password again in the request.
// Later, when we add tokens (e.g. JWT), this controller method will stay almost identical.
// The only difference is: instead of @RequestParam wardName/password,
// we will look up the ward based on the token in the Authorization header.
//
    @GetMapping("/data")
    public ResponseEntity<?> getWardData(@RequestParam String wardName,
                                         @RequestParam String password) {
        Optional<Ward> ward = wardService.signIn(wardName, password);

        if (ward.isPresent()) {
            return ResponseEntity.ok(ward.get());
        } else {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("error", "Invalid ward name or password"));
        }
    }

    // Fetch all data - jsut to see
    @GetMapping("/all-data")
    public List<Ward> getAllData() {
        return wardService.findAllWards();
    }


     /*@PostMapping
    // This method takes simple data, calls the service, and returns a result.
    public Ward createWard(@RequestParam String wardName, @RequestParam String password) {
        Ward newWard = new Ward(wardName, password);
        return wardService.createWard(newWard);
    }*/

    /*@PostMapping("/signIn")
    // This method handles the sign-in logic.
    public Optional<Ward> signIn(@RequestParam String wardName, @RequestParam String password) {
        return wardService.signIn(wardName, password);
    }*/

    /*@GetMapping("/data")
    public Optional<Ward> signInWithData(@RequestParam String wardName, @RequestParam String password) {
        return wardService.signInWithData(wardName, password);
    }*/

    /*
    @GetMapping
    // method to fetch all wards
    public List<Ward> fetchAllWards() {
        return wardService.findAllWards();
    }
     */
}