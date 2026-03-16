package is.hi.matarpontun.dto;

import java.util.List;

public class WardFullDTO {
    private Long wardId;
    private String wardName;
    private List<PatientDailyOrderDTO> patients;

    public WardFullDTO(Long wardId, String wardName, List<PatientDailyOrderDTO> patients) {
        this.wardId = wardId;
        this.wardName = wardName;
        this.patients = patients;
    }

    public String getWardName() {
        return wardName;
    }

    public List<PatientDailyOrderDTO> getPatients() {
        return patients;
    }

    public Long getWardId() {
        return wardId;
    }
}
