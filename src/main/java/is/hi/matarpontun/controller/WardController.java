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

   /* @Autowired ->> býr til smiðinn
    private WardService wardService;
    */
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
    @PostMapping //viljum svo breyta hér að notum WardDTO
    public ResponseEntity<Ward> createWard(@RequestParam String wardName,
                                           @RequestParam String password) {
        Ward newWard = new Ward(wardName, password);
        Ward savedWard = wardService.createWard(newWard); // WardDTO
        return ResponseEntity.ok(savedWard);
    }

    // UC5 - sign inn, skilar bara success eða error
    @PostMapping("/signIn")
    public ResponseEntity<?> signIn(@RequestParam String wardName,
                                    @RequestParam String password) {
        return wardService.signInAndGetData(wardName, password)
                .map(dto -> ResponseEntity.ok(Map.of("message", "Login successful")))
                .orElse(ResponseEntity.status(401)
                        .body(Map.of("error", "Invalid ward name or password")));
    }

    // UC8 - to fetch all data
    // For now, we identify the ward by asking for wardName + password again in the request.
    // Later, when we add tokens (e.g. JWT), this controller method will stay almost identical.
    // The only difference is: instead of @RequestParam wardName/password,
    // we will look up the ward based on the token in the Authorization header.
    @GetMapping("/data")
    public ResponseEntity<?> getWardData(@RequestParam String wardName,
                                         @RequestParam String password) {
        var wardOpt = wardService.signInAndGetData(wardName, password);

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