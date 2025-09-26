package is.hi.matarpontun;

import is.hi.matarpontun.controller.MealController;
import is.hi.matarpontun.controller.WardController;
import is.hi.matarpontun.model.Meal;
import is.hi.matarpontun.model.Ward;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class AppRunner implements CommandLineRunner {

    @Autowired
    private WardController wardController;

    @Autowired
    private MealController mealController;

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
                    signIn(scanner);
                    break;
                case "3":
                    seeAllWardAccounts();
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
        System.out.println("3. See available Ward accounts");
        System.out.println("4. Exit");
        System.out.print("> ");
    }

    private void createWardAccount(Scanner scanner) {
        System.out.print("Enter ward name: ");
        String wardName = scanner.nextLine();
        System.out.print("Enter shared password: ");
        String password = scanner.nextLine();

        wardController.createWard(wardName, password);
        System.out.println("SUCCESS: Ward account '" + wardName + "' created.");
    }

    private void signIn(Scanner scanner) {
        System.out.print("Enter ward name: ");
        String wardName = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        // The runner calls the controller method.
        Optional<Ward> ward = wardController.signIn(wardName, password);
        if (ward.isPresent()) {
            System.out.println("SUCCESS: Sign-in successful for " + ward.get().getWardName());
        } else {
            System.out.println("FAILURE: Wrong password.");
        }
    }
     private void seeAllWardAccounts() {
        List<Ward> wards = wardController.fetchAllWards();
        if (wards.isEmpty()) {
            System.out.println("No ward accounts found.");
            return;
        }
        System.out.println("Available Ward Accounts:");
        for (Ward ward : wards) {
            System.out.println("- " + ward.getWardName());
        }
    }

}