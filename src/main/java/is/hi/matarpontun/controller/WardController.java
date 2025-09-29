package is.hi.matarpontun.controller;

import is.hi.matarpontun.dto.WardDTO;
import is.hi.matarpontun.model.Ward;
import is.hi.matarpontun.service.WardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/wards")
public class WardController {

    private final WardService wardService;

    public WardController(WardService wardService) {
        this.wardService = wardService;
    }

    // default viðmót - skilar Map
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

    // UC5 - sign inn, skilar bara success eða error (Boolean), bæta við tokan seinna
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
}