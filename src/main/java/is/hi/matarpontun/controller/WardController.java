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

    @PostMapping
    // This method takes simple data, calls the service, and returns a result.
    public Ward createWard(@RequestParam String wardName, @RequestParam String password) {
        Ward newWard = new Ward(wardName, password);
        return wardService.createWard(newWard);
    }

    /*@PostMapping("/signIn")
    // This method handles the sign-in logic.
    public Optional<Ward> signIn(@RequestParam String wardName, @RequestParam String password) {
        return wardService.signIn(wardName, password);
    }*/
    @PostMapping("/signIn")
    public ResponseEntity<?> signIn(@RequestParam String wardName, @RequestParam String password) {
        Optional<Ward> ward = wardService.signIn(wardName, password);

        if (ward.isPresent()) {
            return ResponseEntity.ok(ward.get());
        } else {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("error", "Invalid ward name or password"));
        }
    }


    @GetMapping
    // method to fetch all wards
    public List<Ward> fetchAllWards() {
        return wardService.findAllWards();
    }

    // UC8 - to fetch all data
    @GetMapping("/all-data")
    public List<Ward> getAllData() {
        return wardService.findAllWards();
    }


}