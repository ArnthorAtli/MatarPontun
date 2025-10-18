package is.hi.matarpontun.service;

import org.springframework.stereotype.Service;

@Service
public class KitchenService {
    // send order to kitchen
    public void sendOrder(Long patientId, String patientName, String foodType) {
        System.out.printf("Kitchen order -> patientId=%d name=%s food=%s%n", patientId, patientName, foodType);
    }
}
