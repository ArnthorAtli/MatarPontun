package is.hi.matarpontun.controller;

import is.hi.matarpontun.service.KitchenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller responsible for handling requests related to the kitchen.
 */
@RestController
@RequestMapping("/kitchen")
public class KitchenController {

    private final KitchenService kitchenService;

    /**
     * Constructs a new {@code KitchenController} with the specified {@link KitchenService}.
     *
     * @param kitchenService the service responsible for business logic related to kitchen operations
     */
    public KitchenController(KitchenService kitchenService) {
        this.kitchenService = kitchenService;
    }

    /**
     * UC EX1
     * Handles HTTP GET requests to {@code /kitchen/todaysOrders}.
     * 
     * Retrieves a summary of today's meal orders, showing the number of meals of each food type
     * for each ward and meal category.
     *
     * @return a {@link ResponseEntity} containing a map representing the summary of today's orders
     */
    @GetMapping("/todaysOrders")
    public ResponseEntity<Map<String, Object>> getTodaysOrders() {
        Map<String, Object> summary = kitchenService.getTodaysOrdersSummary();
        return ResponseEntity.ok(summary);
    }
}
