package is.hi.matarpontun.controller;

import is.hi.matarpontun.model.Ward;
import is.hi.matarpontun.service.WardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;


@Controller
public class WardController {

    @Autowired
    private WardService wardService;

    // This method takes simple data, calls the service, and returns a result.
    public void createWard(String wardName, String password) {
        Ward newWard = new Ward(wardName, password);
        wardService.createWard(newWard);
    }

    // This method handles the sign-in logic.
    public Optional<Ward> signIn(String wardName, String password) {
        return wardService.signIn(wardName, password);
    }
    // method to fetch all wards
    public List<Ward> fetchAllWards() {
        return wardService.findAllWards();
    }
}