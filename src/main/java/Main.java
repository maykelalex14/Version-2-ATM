// Import JDBCHandler for database connectivity and operations
import persistence.JDBCHandler;
// Import all service classes (ATMService, AuthService, BankTechnician) using wildcard
import services.*;
// Import Account model class which represents user account data
import core.Account;
// Import BankNote class for bank note handling
import core.BankNote;
// Import Scanner for reading user input from console
import java.util.Scanner;
// Import Map and HashMap for bank note selection
import java.util.Map;
import java.util.HashMap;

/**
 * ============== VIEW (MVC) ==============
 * Main.java acts as the View layer in the MVC architecture - responsible for:
 * - Displaying the user interface (menus, prompts, output)
 * - Collecting user input via Scanner
 * - Routing user actions to appropriate Controllers (ATMService, AuthService, BankTechnician)
 * - Orchestrating the flow between different application roles (user, technician)
 * 
 * ============== SOLID PRINCIPLES APPLIED ==============
 * 1. SINGLE RESPONSIBILITY PRINCIPLE (SRP):
 *    - Has ONE reason to change: when UI/View logic changes
 *    - Focused responsibility: user interface and input collection
 *    - Does NOT handle business logic (Controllers do that)
 *    - Does NOT handle data persistence (Persistence layer does that)
 *    - Does NOT manage Models directly (Controllers coordinate)
 * 
 * 2. SEPARATION OF CONCERNS (MVC):
 *    - View (Main.java) is completely separated from:
 *      * Model (Account, PaperTank, InkTank) - data and business logic
 *      * Controller (ATMService, AuthService, BankTechnician) - business orchestration
 *    - Changes to UI don't affect business logic
 *    - Business logic changes don't require UI changes
 * 
 * 3. DEPENDENCY MANAGEMENT:
 *    - Creates instances of Controllers and passes them Scanner for input
 *    - Delegates all business logic to Controllers
 *    - Views responsibility is ONLY to present and collect input
 * 
 * 4. METHOD ORGANIZATION:
 *    - Each handler method represents a specific user action/flow
 *    - Demonstrates how View delegates to Controllers without knowing implementation
 * 
 * MVC FLOW EXAMPLES:
 * User Input (View) → authenticate() (Controller) → verifyPin() (Model) → Output (View)
 * User Input (View) → withdraw() (Controller) → withdraw() (Model) → Persistence → Output (View)
 */
// Main class - entry point of the ATM application
public class Main {
    // ============== View Components ==============
    // Scanner object for reading user input from console input stream
    private static Scanner scanner;
    
    // ============== Controller Instances ==============
    // View holds references to Controllers to coordinate flow
    // ============== SINGLETON-LIKE PATTERN: Single Instances ==============
    // These static fields implement a SINGLETON-LIKE PATTERN:
    // - Each field represents a SINGLE instance shared across entire application
    // - Created ONCE in main() method during initialization
    // - Reused in all menu handlers (never recreated)
    // - Ensures consistent state throughout application lifecycle
    // - Similar to Singleton: provides single point of access to critical objects
    // - Unlike true Singleton: uses static fields instead of getInstance() method
    
    // ATMService controller: handles user account operations (withdraw, deposit, transfer, check balance)
    // SINGLETON: ONE instance manages ALL user ATM operations
    private static ATMService atmService;
    // AuthService controller: handles authentication of technicians (login verification)
    // SINGLETON: ONE instance authenticates ALL technician login attempts
    private static AuthService authService;
    // BankTechnician controller: handles technician-specific operations (refill ink, paper, check supplies)
    // SINGLETON: ONE instance manages ALL technician maintenance operations
    private static BankTechnician technician;
    // JDBCHandler persistence layer: handles all database operations (create, read, update accounts and transactions)
    // SINGLETON-LIKE: ONE instance maintains SINGLE database connection for ALL operations
    private static JDBCHandler dbHandler;

    // ============== APPLICATION INITIALIZATION ==============
    // main() method - entry point when program starts. args: command-line arguments passed to program (unused here)
    // ============== SINGLETON INITIALIZATION ==============
    // This method initializes all SINGLETON-LIKE objects:
    // - Each object created ONCE and stored in static field
    // - Never recreated during application lifetime
    // - Guarantees single point of access and consistent state
    // - Similar to Singleton pattern getInstance() pattern
    public static void main(String[] args) {
        // Initialize View components
        // Create new Scanner object to read user input from System.in (console)
        scanner = new Scanner(System.in);
        
        // Initialize Persistence layer
        // ============== SINGLETON INITIALIZATION: Get SINGLE database handler ==============
        // Get JDBCHandler instance for database operations (SQLite database management)
        // Using getInstance() to get the SINGLE instance (Singleton pattern)
        // This SINGLE instance will be reused for ALL database operations throughout application
        dbHandler = JDBCHandler.getInstance();
        
        // ============== DEPENDENCY INJECTION ==============
        // Create Controllers and inject dependencies (passing dbHandler to services that need database access)
        
        // ============== SINGLETON INITIALIZATION: Create SINGLE ATMService instance ==============
        // Create ATMService controller and pass JDBCHandler so it can access database for account operations
        // This SINGLE instance manages ALL user transactions for entire application lifetime
        atmService = new ATMService(dbHandler);
        // ============== SINGLETON INITIALIZATION: Create SINGLE AuthService instance ==============
        // Create AuthService controller (no dependencies needed for simple authentication)
        // This SINGLE instance authenticates ALL technician login attempts
        authService = new AuthService();
        // ============== SINGLETON INITIALIZATION: Create SINGLE BankTechnician instance ==============
        // Create BankTechnician controller and pass atmService and dbHandler for technician operations
        // This SINGLE instance handles ALL technician/maintenance operations
        technician = new BankTechnician(atmService, dbHandler);

        // Start View: display main menu (entry point to application UI - loops until user exits)
        mainMenu();
        
        // Cleanup resources
        // Close the Scanner to free up system resources and prevent memory leaks
        scanner.close();
        // Close database connection to SQLite database (releases connection and cleanup)
        dbHandler.closeConnection();
    }

    // ============== VIEW: Main Menu ==============
    // mainMenu() method - displays main menu and routes user to appropriate flows based on selection
    // This is the primary user interface loop that runs when application starts
    private static void mainMenu() {
        // Initialize boolean flag to control the main menu loop (true = menu continues running)
        boolean running = true;
        // Loop continues while running is true (until user selects exit option)
        while (running) {
            // Print blank line for readability in console
            System.out.println("\n=== ATM SYSTEM ===");
            // Display menu option 1: User Login
            System.out.println("1. User Login");
            // Display menu option 2: Technician Login
            System.out.println("2. Technician Login");
            // Display menu option 3: Exit application
            System.out.println("3. Exit");
            // Prompt user to enter their choice (with > symbol)
            System.out.print("> ");
            
            // Read user input from console and remove leading/trailing whitespace
            String choice = scanner.nextLine().trim();

            // ============== VIEW: Route to appropriate Controller ==============
            // Switch statement: evaluate user's choice and execute corresponding action
            switch (choice) {
                // User selected option "1" - User Login
                case "1":
                    // Call handleUserLogin() method to execute user login flow
                    handleUserLogin();  // Route to user authentication flow
                    // Control returns here after login completes
                    break;
                // User selected option "2" - Technician Login
                case "2":
                    // Call handleTechnicianLogin() method to execute technician login flow
                    handleTechnicianLogin();  // Route to technician authentication flow
                    // Control returns here after technician login completes
                    break;
                // User selected option "3" - Exit Application
                case "3":
                    // Print exit message to user
                    System.out.println("Thank you for using the ATM. Goodbye!");
                    // Set running flag to false to exit the while loop and terminate program
                    running = false;
                    // Exit switch statement (though loop will end due to running = false)
                    break;
                // User entered invalid option not matching any case above
                default:
                    // Print error message if user's choice doesn't match any menu option
                    System.out.println("Invalid choice. Please try again.");
                    // Continue loop - user will see menu again and can make valid selection
            }
        }
    }

    // ============== VIEW: User Authentication Flow ==============
    // handleUserLogin() method - collects account credentials and authenticates user
    // VIEW RESPONSIBILITY: Collect input
    // CONTROLLER RESPONSIBILITY (ATMService): Authenticate and return result
    private static void handleUserLogin() {
        // Prompt user to enter their account number
        System.out.print("Enter Account Number: ");
        // Read account number from user input and remove whitespace
        String accountNumber = scanner.nextLine().trim();
        // Prompt user to enter their PIN
        System.out.print("Enter PIN: ");
        // Read PIN from user input and remove whitespace
        String pin = scanner.nextLine().trim();

        // ============== MVC: Delegate authentication to Controller ==============
        // Call atmService.authenticate() to verify account number and PIN against database
        // Returns Account object if credentials valid, null if invalid
        Account account = atmService.authenticate(accountNumber, pin);
        // Check if authentication failed (atmService returned null)
        if (account == null) {
            // Print error message if account or PIN was incorrect
            System.out.println("Invalid account number or PIN.");
            // Exit method - return to mainMenu()
            return;
        }

        // ============== MVC: User authenticated - show user menu (View) ==============
        // Only reaches here if authentication succeeded (account is not null)
        // Call userMenu() to display user operations menu
        userMenu();
        // Control returns here after user exits user menu
    }

    // ============== VIEW: User Menu ==============
    // userMenu() method - displays available user operations and routes selections
    // This menu appears AFTER user successfully logs in
    private static void userMenu() {
        // Initialize boolean flag to control user menu loop (true = menu continues running)
        boolean inUserMenu = true;
        // Loop continues while inUserMenu is true (until user selects logout option)
        while (inUserMenu) {
            // Print blank line for readability
            System.out.println("\n=== USER MENU ===");
            // Display menu option 1: Check account balance
            System.out.println("1. View Account Balance");
            // Display menu option 2: Withdraw cash
            System.out.println("2. Withdraw Funds");
            // Display menu option 3: Deposit cash
            System.out.println("3. Deposit Funds");
            // Display menu option 4: Transfer money to another account
            System.out.println("4. Transfer Funds");
            // Display menu option 5: View past transactions
            System.out.println("5. View Transaction History");
            // Display menu option 6: Logout and return to main menu
            System.out.println("6. Exit User Session");
            // Prompt user to select an option
            System.out.print("Choose an option: ");
            
            // Read user's menu choice and remove whitespace
            String choice = scanner.nextLine().trim();

            // ============== VIEW: Route to appropriate handler ==============
            // Switch statement: evaluate user's choice and execute corresponding action
            switch (choice) {
                // User selected option "1" - View Account Balance
                case "1":
                    // Call handleCheckBalance() method to display account balance
                    handleCheckBalance();
                    // Control returns here after balance is displayed
                    break;
                // User selected option "2" - Withdraw Funds
                case "2":
                    // Call handleWithdrawal() method to process withdrawal
                    handleWithdrawal();
                    // Control returns here after withdrawal completes
                    break;
                // User selected option "3" - Deposit Funds
                case "3":
                    // Call handleDeposit() method to process deposit
                    handleDeposit();
                    // Control returns here after deposit completes
                    break;
                // User selected option "4" - Transfer Funds
                case "4":
                    // Call handleTransferFunds() method to process transfer
                    handleTransferFunds();
                    // Control returns here after transfer completes
                    break;
                // User selected option "5" - View Transaction History
                case "5":
                    // Call handleViewTransactionHistory() method to display transaction records
                    handleViewTransactionHistory();
                    // Control returns here after history is displayed
                    break;
                // User selected option "6" - Exit User Session / Logout
                case "6":
                    // Print logout message to user
                    System.out.println("Logging out...");
                    // ============== MVC: Notify Controller to clear session ==============
                    // Call atmService.logout() to clear current user session and reset state
                    atmService.logout();
                    // Set inUserMenu to false to exit the while loop and return to main menu
                    inUserMenu = false;
                    // Exit switch statement (loop will end due to inUserMenu = false)
                    break;
                // User entered invalid option not matching any case above
                default:
                    // Print error message if user's choice doesn't match any menu option
                    System.out.println("Invalid choice. Please try again.");
                    // Continue loop - user will see menu again and can make valid selection
            }
        }
    }

    // ============== VIEW: Technician Authentication Flow ==============
    // handleTechnicianLogin() method - authenticates technician with username/password
    // VIEW RESPONSIBILITY: Collect credentials from user
    // CONTROLLER RESPONSIBILITY (AuthService): Verify credentials against hardcoded database
    private static void handleTechnicianLogin() {
        // Prompt technician to enter their username
        System.out.print("Username: ");
        // Read username from input and remove whitespace
        String user = scanner.nextLine().trim();
        // Prompt technician to enter their password
        System.out.print("Password: ");
        // Read password from input and remove whitespace
        String pass = scanner.nextLine().trim();

        // ============== MVC: Delegate authentication to Controller ==============
        // Call authService.authenticate() with username and password to verify credentials
        // Returns true if credentials valid, false if invalid
        if (authService.authenticate(user, pass)) {
            // ============== MVC: Technician authenticated - show technician menu ==============
            // Only reaches here if authentication succeeded (credentials were correct)
            // Call technician.handleManagementMenu() to display technician operations menu
            // Pass scanner so technician menu can collect technician's input
            technician.handleManagementMenu(scanner);
            // Control returns here after technician exits management menu
        } else {
            // Else: authentication failed (invalid username or password)
            // Print error message indicating login denied
            System.out.println("Access Denied: Invalid Credentials.");
            // Return to mainMenu() - technician must try again or select another option
        }
    }

    // ============== VIEW: Withdrawal Handler ==============
    // handleWithdrawal() method - processes user cash withdrawal transaction with bank note selection
    // VIEW RESPONSIBILITY: Display account info, collect withdrawal amount and bank note selection
    // CONTROLLER RESPONSIBILITY (ATMService): Execute withdrawal logic and update database
    private static void handleWithdrawal() {
        // Call atmService.displayAccountInfo() to show current balance to user before withdrawal
        atmService.displayAccountInfo();
        // Prompt user to enter amount they want to withdraw (or 0 to cancel)
        System.out.print("\nEnter withdrawal amount (or 0 to cancel): $");
        // Try block: attempt to parse user input as a double (currency amount)
        try {
            // Convert user's string input to double (decimal number) for withdrawal amount
            double amount = Double.parseDouble(scanner.nextLine().trim());
            // Check if user entered 0 (cancel operation)
            if (amount == 0) {
                // Print cancellation message to user
                System.out.println("Withdrawal cancelled.");
            // Check if amount is positive (valid withdrawal amount)
            } else if (amount > 0) {
                // Validate that ATM has sufficient cash for this withdrawal
                if (atmService.validateWithdrawalAmount(amount)) {
                    // ============== NEW: Bank Note Selection ==============
                    // Display available bank notes to the customer
                    atmService.displayBankNotes();
                    
                    // Get bank note selection from customer
                    Map<Integer, Integer> selectedNotes = getBankNoteSelection(amount);
                    
                    // If user cancelled bank note selection, abort withdrawal
                    if (selectedNotes == null) {
                        System.out.println("Withdrawal cancelled.");
                        return;
                    }
                    
                    // ============== DISPENSE BANK NOTES ==============
                    // Dispense the selected bank notes and get breakdown for receipt
                    String bankNotesBreakdown = atmService.dispenseBankNotes(selectedNotes);
                    
                    // Check if bank note dispension was successful
                    if (bankNotesBreakdown != null) {
                        // ============== MVC: Delegate business logic to Controller ==============
                        // Call atmService.withdrawWithBankNotes() to process withdrawal with bank notes breakdown
                        atmService.withdrawWithBankNotes(amount, bankNotesBreakdown);
                    } else {
                        // Bank note dispension failed - restore user's withdrawal attempt
                        System.out.println("Unable to dispense selected bank notes. Please try again.");
                    }
                } else {
                    // ATM doesn't have sufficient cash available
                    System.out.println("Error: ATM does not have sufficient cash available for this withdrawal.");
                }
            // Amount is negative (invalid)
            } else {
                // Print error message if amount is invalid (negative)
                System.out.println("Invalid amount.");
            }
        } catch (NumberFormatException e) {
            // Catch block: executes if user input cannot be converted to a number
            // Print error message indicating invalid input
            System.out.println("Invalid amount entered.");
        }
        // Method ends and control returns to userMenu()
    }

    // ============== HELPER: Get Bank Note Selection from Customer ==============
    // getBankNoteSelection() method - gets customer's selection of bank notes for withdrawal
    // Parameter: targetAmount - the total amount customer wants to withdraw
    // Returns: Map of denomination to quantity selected, or null if user cancels
    private static Map<Integer, Integer> getBankNoteSelection(double targetAmount) {
        // Create map to store customer's selections (denomination -> quantity)
        Map<Integer, Integer> selectedNotes = new HashMap<>();
        // Track total amount selected so far
        double totalSelected = 0;
        
        // Loop until customer confirms their selection or enters 0 to cancel
        while (totalSelected < targetAmount) {
            System.out.println("\nSelect bank notes to make up $" + String.format("%.2f", targetAmount));
            System.out.println("Current selection: $" + String.format("%.2f", totalSelected));
            System.out.println("Remaining: $" + String.format("%.2f", targetAmount - totalSelected));
            System.out.println("\nOptions:");
            
            // Display all available denominations
            int optionNum = 1;
            for (BankNote note : atmService.getBankNotes()) {
                System.out.println(optionNum + ". Add $" + note.getDenomination() + " note");
                optionNum++;
            }
            System.out.println(optionNum + ". Confirm selection");
            System.out.println((optionNum + 1) + ". Cancel withdrawal");
            
            System.out.print("\nChoose option: ");
            try {
                String choice = scanner.nextLine().trim();
                int choiceNum = Integer.parseInt(choice);
                
                // Get list of available bank notes
                java.util.List<BankNote> bankNotes = atmService.getBankNotes();
                
                // Check if user selected a denomination (1 to number of denominations)
                if (choiceNum >= 1 && choiceNum <= bankNotes.size()) {
                    // Get the selected denomination
                    BankNote selectedNote = bankNotes.get(choiceNum - 1);
                    int denomination = selectedNote.getDenomination();
                    
                    // Ask how many notes of this denomination
                    System.out.print("How many $" + denomination + " notes? ");
                    String qtyStr = scanner.nextLine().trim();
                    int quantity = Integer.parseInt(qtyStr);
                    
                    // Validate quantity
                    if (quantity <= 0) {
                        System.out.println("Invalid quantity.");
                        continue;
                    }
                    
                    // Validate that we don't exceed requested amount
                    double newTotal = totalSelected + (denomination * quantity);
                    if (newTotal > targetAmount) {
                        System.out.println("Error: That would exceed the requested amount of $" + 
                                         String.format("%.2f", targetAmount) + ".");
                        continue;
                    }
                    
                    // Validate that ATM has enough notes
                    if (selectedNote.getQuantity() < quantity) {
                        System.out.println("Error: Only " + selectedNote.getQuantity() + " x $" + 
                                         denomination + " notes available.");
                        continue;
                    }
                    
                    // Add to selection
                    selectedNotes.put(denomination, selectedNotes.getOrDefault(denomination, 0) + quantity);
                    totalSelected += (denomination * quantity);
                    System.out.println("Added " + quantity + " x $" + denomination + " notes.");
                    
                // Check if user selected confirm option
                } else if (choiceNum == (bankNotes.size() + 1)) {
                    // User confirmed - check if selection matches target amount
                    if (totalSelected == targetAmount) {
                        // Selection is perfect
                        System.out.println("Selection confirmed: $" + String.format("%.2f", totalSelected));
                        return selectedNotes;
                    } else if (totalSelected < targetAmount) {
                        System.out.println("Insufficient amount selected. Need $" + 
                                         String.format("%.2f", targetAmount - totalSelected) + " more.");
                    } else {
                        System.out.println("Over the requested amount. Please adjust your selection.");
                    }
                    
                // Check if user selected cancel option
                } else if (choiceNum == (bankNotes.size() + 2)) {
                    // User cancelled
                    return null;
                } else {
                    // Invalid option
                    System.out.println("Invalid option. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid option number.");
            }
        }
        
        // If exact amount was selected, return the selection
        if (totalSelected == targetAmount) {
            return selectedNotes;
        }
        
        return null;
    }

    // ============== VIEW: Deposit Handler ==============
    // handleDeposit() method - processes user cash deposit transaction
    // VIEW RESPONSIBILITY: Display account info, collect deposit amount
    // CONTROLLER RESPONSIBILITY (ATMService): Execute deposit logic and update database
    private static void handleDeposit() {
        // Call atmService.displayAccountInfo() to show current balance to user before deposit
        atmService.displayAccountInfo();
        // Prompt user to enter amount they want to deposit (or 0 to cancel)
        System.out.print("\nEnter deposit amount (or 0 to cancel): $");
        // Try block: attempt to parse user input as a double (currency amount)
        try {
            // Convert user's string input to double (decimal number) for deposit amount
            double amount = Double.parseDouble(scanner.nextLine().trim());
            // Check if user entered 0 (cancel operation)
            if (amount == 0) {
                // Print cancellation message to user
                System.out.println("Deposit cancelled.");
            // Check if amount is positive (valid deposit amount)
            } else if (amount > 0) {
                // ============== MVC: Delegate business logic to Controller ==============
                // Call atmService.deposit() to process deposit with specified amount
                // Controller updates balance, records transaction, checks paper supply, etc.
                atmService.deposit(amount);
                // Control returns here after deposit is processed
            // Amount is negative (invalid)
            } else {
                // Print error message if amount is invalid (negative)
                System.out.println("Invalid amount.");
            }
        } catch (NumberFormatException e) {
            // Catch block: executes if user input cannot be converted to a number
            // Print error message indicating invalid input
            System.out.println("Invalid amount entered.");
        }
        // Method ends and control returns to userMenu()
    }

    // ============== VIEW: Check Balance Handler ==============
    // handleCheckBalance() method - displays current account balance and account information
    // VIEW RESPONSIBILITY: Request display
    // CONTROLLER RESPONSIBILITY (ATMService): Retrieve and display account info from database
    private static void handleCheckBalance() {
        // Call atmService.displayAccountInfo() to retrieve and display account details
        // This shows account number, account holder name, and current balance
        atmService.displayAccountInfo();
        // Method ends and control returns to userMenu()
    }

    // ============== VIEW: Change PIN Handler (Future Feature) ==============
    // handleChangePIN() method - allows user to change their account PIN
    // @SuppressWarnings annotation: tells compiler to suppress "unused" warnings for this method
    // This method is NOT currently called from userMenu() but implemented for future use
    @SuppressWarnings("unused")
    private static void handleChangePIN() {
        // Prompt user to enter new PIN (4 digits)
        System.out.print("Enter New PIN (4 digits): ");
        // Read new PIN from input and remove whitespace
        String newPin = scanner.nextLine().trim();
        // Prompt user to confirm new PIN by entering it again
        System.out.print("Confirm New PIN: ");
        // Read confirmation PIN from input and remove whitespace
        String confirmPin = scanner.nextLine().trim();

        // Check if: PIN values match AND PIN length is 4 AND PIN contains only digits
        if (newPin.equals(confirmPin) && newPin.length() == 4 && newPin.matches("\\d+")) {
            // ============== MVC: Delegate PIN change to Controller ==============
            // All validation passed - call atmService.changePin() to update PIN in database
            atmService.changePin(newPin);
            // Control returns here after PIN is changed
        } else {
            // Validation failed - print error message explaining validation requirement
            System.out.println("PINs do not match, empty, or invalid format. PIN change cancelled.");
        }
        // Method ends and control returns to userMenu()
    }

    // ============== VIEW: Transfer Funds Handler ==============
    // handleTransferFunds() method - processes money transfer between two accounts
    // VIEW RESPONSIBILITY: Display account info, collect transfer details (recipient, amount)
    // CONTROLLER RESPONSIBILITY (ATMService): Execute transfer logic, handle both sender and recipient
    private static void handleTransferFunds() {
        // Call atmService.displayAccountInfo() to show sender's current balance
        atmService.displayAccountInfo();
        // Prompt user to enter the account number of the money recipient
        System.out.print("\nEnter recipient account number: ");
        // Read recipient account number from input and remove whitespace
        String recipientAccount = scanner.nextLine().trim();
        // Prompt user to enter transfer amount (or 0 to cancel)
        System.out.print("Enter transfer amount (or 0 to cancel): $");
        
        // Try block: attempt to parse user input as a double (currency amount)
        try {
            // Convert user's string input to double (decimal number) for transfer amount
            double amount = Double.parseDouble(scanner.nextLine().trim());
            // Check if user entered 0 (cancel operation)
            if (amount == 0) {
                // Print cancellation message to user
                System.out.println("Transfer cancelled.");
            // Check if amount is positive (valid transfer amount)
            } else if (amount > 0) {
                // ============== MVC: Delegate business logic to Controller ==============
                // Call atmService.transfer() with recipient account and amount to process transfer
                // Controller validates sender has sufficient funds, updates both accounts, records transactions
                atmService.transfer(recipientAccount, amount);
                // Control returns here after transfer is processed
            // Amount is negative (invalid)
            } else {
                // Print error message if amount is invalid (negative)
                System.out.println("Invalid amount.");
            }
        } catch (NumberFormatException e) {
            // Catch block: executes if user input cannot be converted to a number
            // Print error message indicating invalid input
            System.out.println("Invalid amount entered.");
        }
        // Method ends and control returns to userMenu()
    }

    // ============== VIEW: View Transaction History Handler ==============
    // handleViewTransactionHistory() method - displays list of all user's past transactions
    // VIEW RESPONSIBILITY: Request display
    // CONTROLLER RESPONSIBILITY (ATMService): Retrieve and display transaction history from database
    private static void handleViewTransactionHistory() {
        // Check if paper and ink are available to print receipt
        if (!atmService.getPaperTank().hasPaper()) {
            System.out.println("Error: No paper available to print receipt.");
            return;
        }
        
        if (!atmService.getInkTank().hasInk()) {
            System.out.println("Error: No ink available to print receipt.");
            return;
        }
        
        // Call atmService.displayTransactionHistory() to retrieve and display all transactions
        // Shows transaction type (withdrawal, deposit, transfer), amounts, dates, and times
        atmService.displayTransactionHistory();
        
        // Consume paper and ink for printing receipt
        atmService.getPaperTank().useSheet();
        atmService.getInkTank().useInk(5);
        
        // Print receipt with supply information
        System.out.println("\n=== RECEIPT ===");
        System.out.println("Paper: " + atmService.getPaperTank().getCurrentSheets() + " sheets left");
        System.out.println("Ink: " + atmService.getInkTank().getCurrentInk() + " units left");
        System.out.println("================\n");
        // Method ends and control returns to userMenu()
    }
}
