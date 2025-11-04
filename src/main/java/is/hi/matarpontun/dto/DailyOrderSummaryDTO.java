package is.hi.matarpontun.dto;

import java.time.LocalDate;

public class DailyOrderSummaryDTO {
    private Long orderId;
    private LocalDate orderDate;
    private String wardName;//taka út?
    private String roomNumber;
    private String patientName;
    private String foodType;
    private String status;

    public DailyOrderSummaryDTO(Long orderId, LocalDate orderDate, String wardName, String roomNumber,
                                String patientName, String foodType, String status) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.wardName = wardName;
        this.roomNumber = roomNumber;
        this.patientName = patientName;
        this.foodType = foodType;
        this.status = status;
    }

    // --- Getters --- //
    public Long getOrderId() { return orderId; }
    public LocalDate getOrderDate() { return orderDate; }
    public String getWardName() { return wardName; }
    public String getRoomNumber() { return roomNumber; }
    public String getPatientName() { return patientName; }
    public String getFoodType() { return foodType; }
    public String getStatus() { return status; }
}
