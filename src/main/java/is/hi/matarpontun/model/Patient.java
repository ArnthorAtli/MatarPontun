package is.hi.matarpontun.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;


@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patientID;

    private String name;
    private int age;
    //private Room room;
    private int bedNumber;
    @OneToOne

    @JoinColumn(name = "foodtype_id")
    private FoodType foodType;

    // Many patients belong to one ward. patient.getWard() → gives the ward the patient belongs to.
    @ManyToOne
    @JoinColumn(name = "ward_id") //býr til auðkennislykil dálk
    @JsonBackReference //pervents inf loop
    private Ward ward;

    /*@OneToMany
    @JoinColumn(name = "allergy_id")
    private FoodAllergy allergy
     */

    /*@OneToMany
    @JoinColumn(name = "restriction_id")
    // private Restriction restriction;
    */

    // using the empty constructor + setters
    public Patient() {}

    // using the all-args constructor
    public Patient(String name, int age, int bedNumber, String wardName) {
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
    //--------------------------------------------------

}
