package is.hi.matarpontun;

import is.hi.matarpontun.controller.WardController;
import is.hi.matarpontun.dto.WardDTO;
import is.hi.matarpontun.model.Ward;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class AppRunner implements CommandLineRunner {

    @Autowired
    private WardController wardController;

    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Hospital Meal Ordering System!");

        while (true) {
            printMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    createWardAccount(scanner);
                    break;
                case "2":
                    //signIn(scanner);
                    break;
                case "3":
                    //fetchWardData(scanner);
                    break;
                case "4":
                    System.out.println("Exiting application. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
            System.out.println("\n----------------------------------------\n");
        }
    }

    private void printMenu() {
        System.out.println("Please choose an option:");
        System.out.println("1. Create Ward Account (UC4)");
        System.out.println("2. Sign In (UC5)");
        System.out.println("3. Fetch Ward Data (UC8)");
        System.out.println("4. Exit");
        System.out.print("> ");
    }

    // UC4
    private void createWardAccount(Scanner scanner) {
        System.out.print("Enter ward name: ");
        String wardName = scanner.nextLine();
        System.out.print("Enter shared password: ");
        String password = scanner.nextLine();

        ResponseEntity<WardDTO> response = wardController.createWard(new WardDTO(null, wardName, password));
        WardDTO ward = response.getBody();
        if (ward != null) {
            System.out.println("SUCCESS: Ward account '" + ward.wardName() + "' created. (id=" + ward.id() + ")");
        }
    }

    // UC5
    /*private void signIn(Scanner scanner) {
        System.out.print("Enter ward name: ");
        String wardName = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        WardDTO request = new WardDTO(null, wardName, password);
        ResponseEntity<?> response = wardController.signIn(request);
        System.out.println(response.getBody());
    }

    // UC8
    private void fetchWardData(Scanner scanner) {
        System.out.print("Enter ward name: ");
        String wardName = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        WardDTO request = new WardDTO(null, wardName, password);
        ResponseEntity<?> response = wardController.getWardData(request);
        System.out.println(response.getBody());
    }*/
}
