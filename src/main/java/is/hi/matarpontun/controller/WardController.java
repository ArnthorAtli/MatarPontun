package is.hi.matarpontun.controller;

import is.hi.matarpontun.model.Ward;
import is.hi.matarpontun.service.WardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/wards")
public class WardController {

    @Autowired
    private WardService wardService;

    @PostMapping
    // This method takes simple data, calls the service, and returns a result.
    public Ward createWard(@RequestParam String wardName, @RequestParam String password) {
        Ward newWard = new Ward(wardName, password);
        return wardService.createWard(newWard);
    }

    @PostMapping("/signIn")
    // This method handles the sign-in logic.
    public Optional<Ward> signIn(@RequestParam String wardName, @RequestParam String password) {
        return wardService.signIn(wardName, password);
    }

    @GetMapping
    // method to fetch all wards
    public List<Ward> fetchAllWards() {
        return wardService.findAllWards();
    }
}