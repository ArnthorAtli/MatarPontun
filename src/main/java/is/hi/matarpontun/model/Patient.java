package is.hi.matarpontun.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
/**
 * The Patient entity represents a hospital patient in the MatarPöntun system.
 * Each patient belongs to a ward and a room, and has one restriction profile and one allergies profile.
 *
 * The restriction defines dietary rules (e.g. no meat, low sodium, etc.)
 * and will automatically be saved or deleted together with the patient
 * thanks to the cascade = CascadeType.ALL setting.
 */
@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patientID;

    private String name;
    private int age;
    private int bedNumber;

    // Many patients share the same FoodType
    @ManyToOne
    @JoinColumn(name = "foodtype_id")
    private FoodType foodType;

    // Many patients belong to one ward. patient.getWard() → gives the ward the patient belongs to.
    @ManyToOne
    @JoinColumn(name = "ward_id") //býr til auðkennislykil dálk
    @JsonBackReference //pervents inf loop
    private Ward ward;

    // Many patients can share one room
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    @JsonBackReference("room-patients")
    private Room room;

    /*@OneToMany
    @JoinColumn(name = "allergy_id")
    private FoodAllergy allergy
     */

    /**
     * Each patient has exactly one Restriction entity (1:1 relationship).
     * The cascade = CascadeType.ALL means:
     * - When you save a Patient, its Restriction will automatically be saved too.
     * - When you delete a Patient, its Restriction will also be deleted.
     *
     * This avoids needing to manually call restrictionRepository.save() in your service.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "restriction_id")
    private Restriction restriction;


    // using the empty constructor + setters
    public Patient() {}

    // using the all-args constructor
    public Patient(String name, int age, int bedNumber, Ward ward) {
        this.name = name;
        this.age = age;
        this.bedNumber = bedNumber;
        this.ward = ward;
    }


    // SETTERS AND GETTERS //
    //----------------------------------------------
    public Ward getWard() {
        return ward;
    }

    public void setWard(Ward ward) {
        this.ward = ward;
    }

    public Long getPatientID() {
        return patientID;
    }

    public void setPatientID(Long patientID) {
        this.patientID = patientID;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public int getBedNumber() {
        return bedNumber;
    }
    public void setBedNumber(int bedNumber) {
        this.bedNumber = bedNumber;
    }
    public FoodType getFoodType() {
        return foodType;
    }
    public void setFoodType(FoodType foodType) {
        this.foodType = foodType;
    }

    public Room getRoom() { return room; }

    public void setRoom(Room room) { this.room = room; }

    public Restriction getRestriction() {
        return restriction;
    }
    public void setRestriction(Restriction restriction) {
        this.restriction = restriction;
    }
    

}
