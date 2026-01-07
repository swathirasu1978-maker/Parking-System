package app;

import dao.VehicleDAO;
import model.Vehicle;
import service.ParkingService;

import java.sql.Timestamp;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final VehicleDAO vehicleDAO = new VehicleDAO();
    private static final ParkingService parkingService = new ParkingService();

    public static void main(String[] args) {
        parkingService.displayWelcomeMessage();

        boolean running = true;
        while (running) {
            showMenu();
            String choiceStr = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(choiceStr);
            } catch (Exception ex) {
                System.out.println("Enter a valid number.");
                continue;
            }

            switch (choice) {
                case 1 -> addVehicleFlow();
                case 2 -> viewVehiclesFlow();
                case 3 -> updateVehicleFlow();
                case 4 -> exitVehicleFlow();
                case 5 -> searchVehicleFlow();
                case 6 -> todayRevenueFlow();
                case 7 -> monthRevenueFlow();
                case 8 -> showHistoryFlow();
                case 9 -> showSlotAvailability();
                case 10 -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void showMenu() {
        System.out.println("\nMenu:");
        System.out.println("1. Add Vehicle (auto slot)");
        System.out.println("2. View Parked Vehicles");
        System.out.println("3. Update Vehicle details (number/type/slot)");
        System.out.println("4. Exit Vehicle (calculate fee)");
        System.out.println("5. Search active vehicles (number/slot/type)");
        System.out.println("6. Today's Income");
        System.out.println("7. This Month Income");
        System.out.println("8. View Parking History (last 50)");
        System.out.println("9. Slot availability (next free)");
        System.out.println("10. Quit");
        System.out.print("Choice: ");
    }

    private static void addVehicleFlow() {
        System.out.print("Vehicle Number: ");
        String number = scanner.nextLine().trim();
        if (!parkingService.isValidVehicleNumber(number)) {
            System.out.println("Invalid number.");
            return;
        }

        System.out.print("Vehicle Type (Car/Bike): ");
        String type = scanner.nextLine().trim();
        if (!parkingService.isValidVehicleType(type)) {
            System.out.println("Invalid type.");
            return;
        }

        int slot = vehicleDAO.getNextAvailableSlot();
        System.out.println("Assigned Slot: " + slot);

        Vehicle v = new Vehicle(number, type, slot);
        boolean ok = vehicleDAO.addVehicle(v);
        if (ok)
            System.out.println("Added. ID: " + v.getId());
        else
            System.out.println("Failed to add.");
    }

    private static void viewVehiclesFlow() {
        List<Vehicle> list = vehicleDAO.getAllVehicles();
        if (list.isEmpty()) {
            System.out.println("No vehicles parked.");
            return;
        }
        System.out.println("\nParked Vehicles:");
        for (Vehicle v : list)
            System.out.println(v);
    }

    private static void updateVehicleFlow() {
        System.out.print("Enter vehicle ID to update: ");
        String idStr = scanner.nextLine().trim();
        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (Exception e) {
            System.out.println("Invalid ID.");
            return;
        }

        Vehicle v = vehicleDAO.findById(id);
        if (v == null) {
            System.out.println("Not found.");
            return;
        }

        System.out.print("New vehicle number (enter to skip): ");
        String num = scanner.nextLine().trim();
        if (!num.isEmpty())
            v.setVehicleNumber(num);

        System.out.print("New type (Car/Bike) (enter to skip): ");
        String type = scanner.nextLine().trim();
        if (!type.isEmpty() && parkingService.isValidVehicleType(type))
            v.setVehicleType(type);

        System.out.print("New slot (enter 0 to skip): ");
        String sslot = scanner.nextLine().trim();
        if (!sslot.isEmpty()) {
            int newSlot = Integer.parseInt(sslot);
            v.setSlotNumber(newSlot);
        }

        boolean ok = vehicleDAO.updateVehicle(v);
        System.out.println(ok ? "Updated successfully." : "Update failed.");
    }

    private static void exitVehicleFlow() {
        System.out.print("Enter Vehicle ID to exit: ");
        String idStr = scanner.nextLine().trim();
        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (Exception e) {
            System.out.println("Invalid ID.");
            return;
        }

        Vehicle v = vehicleDAO.findById(id);
        if (v == null) {
            System.out.println("Not found.");
            return;
        }

        Timestamp exit = parkingService.nowTs();
        v.setExitTime(exit);

        double hours = parkingService.calculateHours(v.getEntryTime(), v.getExitTime());
        v.setParkingHours(hours);

        double fee = parkingService.calculateFee(hours, v.getVehicleType());
        v.setFee(fee);

        boolean updated = vehicleDAO.updateVehicleExit(v);
        if (!updated) {
            System.out.println("Failed to update exit details.");
            return;
        }

        boolean archived = vehicleDAO.addToHistory(v);
        if (!archived)
            System.out.println("Warning: archiving failed.");

        boolean deleted = vehicleDAO.deleteVehicle(v.getId());
        if (!deleted)
            System.out.println("Warning: delete from active failed.");

        System.out.printf("Exited. Hours: %.2f | Fee: %.2f%n", hours, fee);
    }

    private static void searchVehicleFlow() {
        System.out.print("Enter search term (number/type/slot): ");
        String q = scanner.nextLine().trim();
        if (q.isEmpty()) {
            System.out.println("Empty input.");
            return;
        }
        List<Vehicle> res = vehicleDAO.searchActive(q);
        if (res.isEmpty())
            System.out.println("No results.");
        else
            for (Vehicle v : res)
                System.out.println(v);

    }

    private static void todayRevenueFlow() {
        try {
            Timestamp[] range = parkingService.todayRange();
            double total = vehicleDAO.getTotalFeeBetween(range[0], range[1]);
            System.out.printf("Today's total revenue: %.2f%n", total);
        } catch (Exception e) {
            System.out.println("Report failed: " + e.getMessage());
        }
    }

    private static void monthRevenueFlow() {
        try {
            Timestamp[] range = parkingService.monthRange();
            double total = vehicleDAO.getTotalFeeBetween(range[0], range[1]);
            System.out.printf("This month's total revenue: %.2f%n", total);
        } catch (Exception e) {
            System.out.println("Month report failed: " + e.getMessage());
        }
    }

    private static void showHistoryFlow() {
        List<Vehicle> list = vehicleDAO.getHistory(50);
        if (list.isEmpty()) {
            System.out.println("No history.");
            return;
        }
        for (Vehicle v : list)
            System.out.println(v);
    }

    private static void showSlotAvailability() {
        int next = vehicleDAO.getNextAvailableSlot();
        System.out.println("Next available slot: " + next);
    }
}

// javac -d bin -cp "lib/*" src\app\Main.java src\dao\*.java src\model\*.java src\service\*.java
// java -cp "bin;lib/*" app.Main
