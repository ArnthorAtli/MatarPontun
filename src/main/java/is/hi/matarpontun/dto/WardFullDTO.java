package is.hi.matarpontun.dto;
// is the “big picture” version of a ward, so it should expose everything relevant for UC8/UC11 (fetch all data for a ward), but without leaking sensitive stuff (like passwords).

import java.util.List;

public class WardFullDTO {
    private Long id;
    private String wardName;
    private List<PatientDTO> patients; // nested patients

    // --- getters & setters ---
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getWardName() {
        return wardName;
    }
    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public List<PatientDTO> getPatients() {
        return patients;
    }
    public void setPatients(List<PatientDTO> patients) {
        this.patients = patients;
    }
}

