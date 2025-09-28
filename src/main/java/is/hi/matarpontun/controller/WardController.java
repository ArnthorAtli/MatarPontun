package is.hi.matarpontun.controller;

import is.hi.matarpontun.dto.WardDTO;
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

    private final WardService wardService;

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
    public ResponseEntity<WardDTO> createWard(@RequestBody WardDTO request) {
        Ward savedWard = wardService.createWard(new Ward(request.wardName(), request.password()));
        return ResponseEntity.ok(new WardDTO(savedWard.getId(), savedWard.getWardName(), null));
    }

    // UC5 - sign inn, skilar bara success eða error
    @PostMapping("/signIn")
    public ResponseEntity<?> signIn(@RequestBody WardDTO request) {
        return wardService.signInAndGetData(request.wardName(), request.password())
                .map(ward -> ResponseEntity.ok(Map.of("message", "Login successful")))
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "Invalid ward name or password")));
        }

    // UC8 - to fetch all data
    // For now, we identify the ward by asking for wardName + password again in the request.
    // Later, when we add tokens (e.g. JWT), this controller method will stay almost identical.
    // The only difference is: instead of @RequestParam wardName/password,
    // we will look up the ward based on the token in the Authorization header.
    @GetMapping("/data")
    public ResponseEntity<?> getWardData(@RequestBody WardDTO request) {

        var wardOpt = wardService.signInAndGetData(request.wardName(), request.password());

        if (wardOpt.isPresent()) {
            return ResponseEntity.ok(wardOpt.get());
        } else {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid ward name or password"));
        }
    }

    // Fetch all data - bara til að skoða
    @GetMapping("/all-data")
    public List<Ward> getAllData() { //notum DTO?
        return wardService.findAllWards();
    }
}