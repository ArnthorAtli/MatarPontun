package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DemoApplication {
  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

  @GetMapping("/hello")
  public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
    return String.format("Hello %s!", name);
  }
  // Sunna getur gert commit

  // Katrín líka!
  // Silja líka! woohoo
  public String[] faediOptions = {
      "A1 - almennt fæði",
      "A2 - Hentar eldri kynslóðinni",
      "A3 - Grænmetisfæði",
      "OP - Orku og próteinbætt fæði",
      "RDS kjöt/fiskur",
      "RDS grænmetisfæði",
      "M1 - Mjúkt",
      "M2 - Hakkað",
      "M3 - Fínmaukað",
      "F1 - Fljótandi fæði",
      "F1 Sykurskert",
      "F1 Mjólkurlaust",
      "F2 - Tært fljótandi",
      "F3 - Fljótandi fæði eftir aðgerð",
      "F4 - Þykkfljótandi fæði",
      "F4 Sykurskert",
      "F5 - Fljótandi fæði kalt",
      "Fituskert 40g",
      "Próteinskert 50g",
      "Próteinskert 50g Sykurskert",
      "Próteinskert 60g",
      "Saltskert",
      "Salskert sykurskert",
      "Blóðskilunarfæði",
      "Blóðskilunarfæði Sykurskert",
      "FSMS",
      "Mjólkursykurskert",
      "Glútensnautt",
      "Örveruskert",
      "Joðsnautt",
      "Ungbarnafæði",
      "FASTANDI",
      "EINNOTA"
  };

}