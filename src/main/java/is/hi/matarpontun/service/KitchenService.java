package is.hi.matarpontun.service;

import is.hi.matarpontun.model.DailyOrder;
import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.repository.DailyOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KitchenService {

    private final DailyOrderRepository dailyOrderRepository;

    public KitchenService(DailyOrderRepository dailyOrderRepository) {
        this.dailyOrderRepository = dailyOrderRepository;
    }

    /**
     * Builds a hierarchical summary of today's meals grouped by:
     * Ward → Meal Category → FoodType → Count
     */
    public Map<String, Object> getTodaysOrdersSummary() {
        LocalDate today = LocalDate.now();

        List<DailyOrder> todaysOrders = dailyOrderRepository.findAll().stream()
                .filter(order -> order.getOrderDate().equals(today))
                .collect(Collectors.toList());

        if (todaysOrders.isEmpty()) {
            return Map.of("message", "No daily orders found for today (" + today + ")");
        }

        Map<String, Map<String, Map<String, Long>>> wardSummary = new TreeMap<>();

        for (DailyOrder order : todaysOrders) {
            String wardName = order.getWardName() != null ? order.getWardName() : "Unassigned";
            wardSummary.putIfAbsent(wardName, new TreeMap<>());

            addMealToSummary(wardSummary.get(wardName), "Breakfast", order.getBreakfast());
            addMealToSummary(wardSummary.get(wardName), "Lunch", order.getLunch());
            addMealToSummary(wardSummary.get(wardName), "AfternoonSnack", order.getAfternoonSnack());
            addMealToSummary(wardSummary.get(wardName), "Dinner", order.getDinner());
            addMealToSummary(wardSummary.get(wardName), "NightSnack", order.getNightSnack());
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("date", today.toString());
        response.put("wards", wardSummary);
        response.put("totalOrders", todaysOrders.size());

        return response;
    }

    private void addMealToSummary(Map<String, Map<String, Long>> mealSummary, String category, Meal meal) {
        if (meal == null || meal.getFoodType() == null) return;

        String foodType = meal.getFoodType().getTypeName();
        mealSummary.putIfAbsent(category, new TreeMap<>());
        Map<String, Long> categoryMap = mealSummary.get(category);

        categoryMap.put(foodType, categoryMap.getOrDefault(foodType, 0L) + 1);
    }
}
