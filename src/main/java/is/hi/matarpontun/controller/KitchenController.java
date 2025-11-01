package is.hi.matarpontun.controller;

import is.hi.matarpontun.service.KitchenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/kitchen")
public class KitchenController {

    private final KitchenService kitchenService;

    public KitchenController(KitchenService kitchenService) {
        this.kitchenService = kitchenService;
    }

    /**
     * GET /kitchen/todaysOrders
     * Returns a breakdown of how many meals of each food type exist
     * for each ward and meal category.
     */
    @GetMapping("/todaysOrders")
    public ResponseEntity<Map<String, Object>> getTodaysOrders() {
        Map<String, Object> summary = kitchenService.getTodaysOrdersSummary();
        return ResponseEntity.ok(summary);
    }
}
