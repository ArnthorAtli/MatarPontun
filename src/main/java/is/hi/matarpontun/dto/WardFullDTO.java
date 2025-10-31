package is.hi.matarpontun.dto;

import java.util.List;

public class WardFullDTO {
    private String wardName;
    private List<PatientDailyOrderDTO> patients;

    public WardFullDTO(String wardName, List<PatientDailyOrderDTO> patients) {
        this.wardName = wardName;
        this.patients = patients;
    }

    public String getWardName() {
        return wardName;
    }

    public List<PatientDailyOrderDTO> getPatients() {
        return patients;
    }
}
