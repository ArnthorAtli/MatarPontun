package is.hi.matarpontun.service;

import is.hi.matarpontun.model.FoodType;
import is.hi.matarpontun.model.Patient;
import org.springframework.stereotype.Service;

@Service
public class KitchenService {
    // send order to kitchen
    public void logOrder(Patient patient, FoodType foodType) {
        System.out.printf("Order logged: %s -> %s%n", patient.getName(), foodType.getTypeName());
    }
}