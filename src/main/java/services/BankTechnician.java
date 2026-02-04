package services;

import interfaces.TechActions;
import interfaces.Persistence;
import persistence.JDBCHandler;
import core.Account;
import java.util.Scanner;

/**
 * ============== CONTROLLER (MVC) ==============
 * BankTechnician acts as a Controller in the MVC architecture - responsible for:
 * - Managing technician/maintenance operations for the ATM machine
 * - Coordinating cash, paper, and ink refills through ATMService
 * - Handling technician menu logic and user input
 * - Logging all technician activities to database
 * 
 * ============== SOLID PRINCIPLES APPLIED ==============
 * 1. SINGLE RESPONSIBILITY PRINCIPLE (SRP):
 *    - Has ONE reason to change: when technician operations logic changes
 *    - Focused responsibility: managing ATM maintenance and diagnostics
 *    - Does NOT handle account operations (ATMService does that)
 *    - Does NOT handle authentication (Main.java + AuthService do that)
 *    - Does NOT know HOW to persist data (delegates to Persistence)
 * 
 * 2. DEPENDENCY INVERSION PRINCIPLE (DIP):
 *    - Depends on Persistence interface, NOT concrete JDBCHandler
 *    - Constructor accepts both ATMService and Persistence abstractions
 *    - Can work with any Persistence implementation
 *    - Allows unit testing with mock implementations
 * 
 * 3. INTERFACE SEGREGATION PRINCIPLE (ISP):
 *    - Implements TechActions interface with focused technician methods only
 *    - Not forced to implement unrelated Account or User operations
 *    - Each method serves a specific technician concern
 * 
 * 4. LISKOV SUBSTITUTION PRINCIPLE (LSP):
 *    - Implements TechActions contract - any TechActions implementation is substitutable
 *    - Methods return same types and maintain expected behavior
 *    - Future technician types (ATMTechnician, MaintenanceTech) can substitute this class
 * 
 * 5. OPEN/CLOSED PRINCIPLE (OCP):
 *    - Open for extension: new technician operations can be added without modifying existing code
 *    - Closed for modification: changes to persistence don't require changes here
 */
public class BankTechnician implements TechActions {
    // ============== DEPENDENCY INJECTION (DIP) ==============
    private ATMService service;  // Injected controller
    private Persistence persistence;  // Injected abstraction - not concrete class
    private JDBCHandler jdbcHandler;  // Optional: used for advanced logging

    // ============== CONSTRUCTOR DEPENDENCY INJECTION ==============
    // Accepts abstractions (ATMService, Persistence) - promotes loose coupling
    public BankTechnician(ATMService service, Persistence persistence) {
        this.service = service;
        this.persistence = persistence;
        this.jdbcHandler = (persistence instanceof JDBCHandler) ? (JDBCHandler) persistence : null;
    }

    // ============== TECHNICIAN SESSION MANAGEMENT ==============
    // Handles: View (menu display) → Input processing → Delegation to specific operations
    public void handleManagementMenu(Scanner inputScanner) {
        boolean sessionActive = true;
        while (sessionActive) {
            System.out.println("\nTechnician Session:");
            System.out.println("1. View ATM Status");
            System.out.println("2. Add Cash to ATM");
            System.out.println("3. Add Ink to ATM");
            System.out.println("4. Add Paper to ATM");
            System.out.println("5. Collect Income (Cash from ATM)");
            System.out.println("6. Exit Technician Session");
            System.out.print("Choose an option: ");

            if (!inputScanner.hasNextLine()) {
                break;
            }
            String choice = inputScanner.nextLine().trim();
            // ============== SRP: Route to appropriate technician operation ==============
            switch (choice) {
                case "1": displayATMStatus(); break;
                case "2": refillATMCash(inputScanner); break;
                case "3": refillInkLevel(inputScanner); break;
                case "4": refillPaperLevel(inputScanner); break;
                case "5": collectATMCash(inputScanner); break;
                case "6": sessionActive = false; break;
                default: System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // ============== INTERFACE IMPLEMENTATION (ISP) ==============
    // Implements TechActions.displayMachineStatus() - focused technician operation
    @Override
    public void displayMachineStatus() {
        System.out.println("\n[ATM DIAGNOSTICS]");
        System.out.println("ATM Cash Available: $" + String.format("%.2f", service.getATMCash()));
        System.out.println("Total Accounts: " + service.getAccounts().size());
    }

    // ============== TECHNICIAN OPERATION: View ATM Status ==============
    // Coordinates: retrieves ATM state from Controller → displays to technician
    private void displayATMStatus() {
        System.out.println("\n[ATM STATUS]");
        System.out.println("ATM Cash: $" + String.format("%.2f", service.getATMCash()));
        System.out.println("Paper Sheets: " + service.getPaperTank().getCurrentSheets());
        System.out.println("Ink Units: " + service.getInkTank().getCurrentInk());
        System.out.println("Active Accounts: " + service.getAccounts().size());
    }

    // ============== TECHNICIAN OPERATION: Refill ATM Cash ==============
    // Coordinates: input validation → ATM state update → audit logging → persistence
    private void refillATMCash(Scanner scanner) {
        System.out.print("Enter amount to refill: $");
        try {
            if (!scanner.hasNextLine()) return;
            double amount = Double.parseDouble(scanner.nextLine().trim());
            if (amount > 0) {
                // ============== SRP: Track previous state for audit ==============
                double previousCash = service.getATMCash();
                double newCash = previousCash + amount;
                // ============== UPDATE ATM CASH IN SERVICE ==============
                // Actually update the atmCash field in ATMService
                service.setATMCash(newCash);
                System.out.println("ATM refilled with $" + String.format("%.2f", amount));
                System.out.println("New ATM balance: $" + String.format("%.2f", newCash));
                
                // ============== AUDIT LOGGING (via JDBCHandler) ==============
                // Log technician activity for compliance and troubleshooting
                if (jdbcHandler != null) {
                    jdbcHandler.logTechnicianActivity("CASH_REFILL", amount, "ATM cash refilled", previousCash, newCash);
                }
                
                // ============== DIP: Persist state via Persistence abstraction ==============
                sync();
            } else {
                System.out.println("Invalid amount.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    @SuppressWarnings("unused")
    private void viewAllAccounts() {
        System.out.println("\n--- All Bank Accounts ---");
        for (Account acc : service.getAccounts()) {
            System.out.println("Account: " + acc.getAccountNumber() + 
                             " | Holder: " + acc.getAccountHolder() + 
                             " | Balance: $" + String.format("%.2f", acc.getBalance()));
        }
    }

    @SuppressWarnings("unused")
    // ============== TECHNICIAN OPERATION: Collect Cash from ATM ==============
    private void collectATMCash(Scanner scanner) {
        System.out.print("Enter amount to collect: $");
        try {
            if (!scanner.hasNextLine()) return;
            double amount = Double.parseDouble(scanner.nextLine().trim());
            if (amount > 0 && amount <= service.getATMCash()) {
                // ============== SRP: Track previous state for audit ==============
                double previousCash = service.getATMCash();
                double newCash = previousCash - amount;
                // ============== UPDATE ATM CASH IN SERVICE ==============
                // Actually update the atmCash field in ATMService
                service.setATMCash(newCash);
                System.out.println("Cash collected: $" + String.format("%.2f", amount));
                System.out.println("Remaining ATM balance: $" + String.format("%.2f", newCash));
                
                // ============== AUDIT LOGGING (via JDBCHandler) ==============
                if (jdbcHandler != null) {
                    jdbcHandler.logTechnicianActivity("CASH_COLLECTION", amount, "Cash collected from ATM", previousCash, newCash);
                }
                
                sync();
            } else {
                System.out.println("Invalid amount or insufficient ATM cash.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    // ============== TECHNICIAN OPERATION: Refill Paper ==============
    // Pattern: input validation → Model update → audit logging → persistence
    private void refillPaperLevel(Scanner scanner) {
        System.out.print("Enter number of sheets to add: ");
        try {
            if (!scanner.hasNextLine()) return;
            int sheets = Integer.parseInt(scanner.nextLine().trim());
            if (sheets > 0) {
                // ============== SRP: Track previous state for audit ==============
                int previousSheets = service.getPaperTank().getCurrentSheets();
                // ============== SRP: Model (PaperTank) handles its own refill ==============
                service.getPaperTank().refill(sheets);
                int newSheets = service.getPaperTank().getCurrentSheets();
                System.out.println("Paper refilled with " + sheets + " sheets");
                System.out.println("New paper level: " + newSheets + " sheets");
                
                // ============== AUDIT LOGGING ==============
                if (jdbcHandler != null) {
                    jdbcHandler.logTechnicianActivity("PAPER_REFILL", sheets, "Paper refilled", previousSheets, newSheets);
                }
                
                sync();
            } else {
                System.out.println("Invalid amount.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    // ============== TECHNICIAN OPERATION: Refill Ink ==============
    // Pattern: input validation → Model update → audit logging → persistence
    private void refillInkLevel(Scanner scanner) {
        System.out.print("Enter number of ink units to add: ");
        try {
            if (!scanner.hasNextLine()) return;
            int ink = Integer.parseInt(scanner.nextLine().trim());
            if (ink > 0) {
                // ============== SRP: Track previous state for audit ==============
                int previousInk = service.getInkTank().getCurrentInk();
                // ============== SRP: Model (InkTank) handles its own refill ==============
                service.getInkTank().refill(ink);
                int newInk = service.getInkTank().getCurrentInk();
                System.out.println("Ink refilled with " + ink + " units");
                System.out.println("New ink level: " + newInk + " units");
                
                // ============== AUDIT LOGGING ==============
                if (jdbcHandler != null) {
                    jdbcHandler.logTechnicianActivity("INK_REFILL", ink, "Ink refilled", previousInk, newInk);
                }
                
                sync();
            } else {
                System.out.println("Invalid amount.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    @SuppressWarnings("unused")
    // ============== DIAGNOSTIC OPERATION ==============
    // Demonstrates: gathering state from multiple Models and displaying comprehensive status
    private void runDiagnostics() {
        System.out.println("\n[SYSTEM DIAGNOSTICS]");
        System.out.println("ATM Status: OPERATIONAL");
        System.out.println("Connection Status: ONLINE");
        System.out.println("Card Reader: OK");
        System.out.println("Cash Dispenser: OK");
        System.out.println("Receipt Printer: " + (service.getPaperTank().hasPaper() && service.getInkTank().hasInk() ? "OK" : "LOW ON SUPPLIES"));
        System.out.println("Total Accounts: " + service.getAccounts().size());
        System.out.println("Total Deposits: $" + String.format("%.2f", calculateTotalDeposits()));
        System.out.println("ATM Cash: $" + String.format("%.2f", service.getATMCash()));
        System.out.println("Paper Sheets: " + service.getPaperTank().getCurrentSheets());
        System.out.println("Ink Units: " + service.getInkTank().getCurrentInk());
    }

    // ============== AGGREGATION OPERATION ==============
    // Uses functional programming (stream) to calculate total deposits across all accounts
    private double calculateTotalDeposits() {
        return service.getAccounts().stream()
            .mapToDouble(Account::getBalance)
            .sum();
    }

    // ============== INTERFACE IMPLEMENTATION (ISP) ==============
    // These methods are part of TechActions contract but not applicable for ATM
    // Demonstrates ISP: interface provides contract, implementation decides what's relevant
    
    @Override
    public void collectIncome() {
        // Not applicable for ATM - direct operation available instead
        System.out.println("Use 'Collect Cash from ATM' option instead.");
    }

    @Override
    public void refillInventory() {
        // Not applicable for ATM system
        System.out.println("Not applicable for ATM system.");
    }

    @Override
    public void refillPaper() {
        // Not applicable for ATM - use specific refillPaperLevel instead
        System.out.println("Not applicable for ATM system.");
    }

    @Override
    // ============== INTERFACE IMPLEMENTATION (ISP) ==============
    // Implements updateFirmware - part of TechActions contract
    public void updateFirmware() {
        System.out.println("Notification: Firmware update started...");
        System.out.println("System rebooting... Firmware v3.0.0 (ATM) installed successfully.");
    }

    // ============== PERSISTENCE SYNCHRONIZATION (DIP) ==============
    // Uses Persistence abstraction - can work with any implementation
    // Delegates all persistence concerns to abstraction layer
    private void sync() {
        persistence.saveATMState(service.getAccounts(), service.getATMCash(), service.getPaperTank(), service.getInkTank());
    }

    @SuppressWarnings("unused")
    // ============== ACTIVITY LOG RETRIEVAL ==============
    // Demonstrates: optional feature using JDBCHandler for detailed logging
    private void viewActivityLog() {
        if (jdbcHandler != null) {
            jdbcHandler.getTechnicianActivityLog();
        } else {
            System.out.println("Error: Database handler not available.");
        }
    }
}
