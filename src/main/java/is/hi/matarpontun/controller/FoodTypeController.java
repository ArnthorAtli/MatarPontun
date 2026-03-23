package is.hi.matarpontun.controller;

import is.hi.matarpontun.repository.FoodTypeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/food-types")
public class FoodTypeController {

    private final FoodTypeRepository foodTypeRepository;

    public FoodTypeController(FoodTypeRepository foodTypeRepository) {
        this.foodTypeRepository = foodTypeRepository;
    }

    record FoodTypeSummary(Long id, String typeName, String description) {}

    @GetMapping
    public ResponseEntity<List<FoodTypeSummary>> getAllFoodTypes() {
        List<FoodTypeSummary> result = foodTypeRepository.findAll().stream()
                .map(ft -> new FoodTypeSummary(ft.getId(), ft.getTypeName(), ft.getDescription()))
                .toList();
        return ResponseEntity.ok(result);
    }
}
