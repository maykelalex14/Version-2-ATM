// Declare package: services package contains Controller classes that orchestrate business logic
package services;

// Import Account model class - represents bank account data and operations
import core.Account;
// Import PaperTank model class - manages paper inventory for receipts
import core.PaperTank;
// Import InkTank model class - manages ink inventory for receipts
import core.InkTank;
// Import Persistence interface - abstraction for data persistence layer (DIP principle)
import interfaces.Persistence;
// Import JDBCHandler persistence implementation - for logging transactions
import persistence.JDBCHandler;
// Import List interface - needed for managing collections of accounts
import java.util.List;

/**
 * ============== CONTROLLER (MVC) ==============
 * ATMService acts as a Controller in the MVC architecture - responsible for:
 * - Orchestrating business logic for ATM operations (withdrawals, deposits, transfers)
 * - Authenticating users and managing current account session
 * - Coordinating between Models (Account, PaperTank, InkTank) and Persistence layer
 * - Managing ATM cash reserves
 * 
 * ============== SOLID PRINCIPLES APPLIED ==============
 * 1. DEPENDENCY INVERSION PRINCIPLE (DIP):
 *    - Depends on Persistence interface, NOT concrete JDBCHandler
 *    - Constructor accepts Persistence abstraction
 *    - Can work with any Persistence implementation (JPA, REST, XML, etc.)
 *    - Allows unit testing with mock Persistence implementations
 * 
 * 2. SINGLE RESPONSIBILITY PRINCIPLE (SRP):
 *    - Has ONE reason to change: when ATM business logic changes
 *    - Focused on orchestrating ATM operations
 *    - Does NOT handle UI (Main.java handles that)
 *    - Does NOT handle database operations directly (delegates to Persistence)
 *    - Does NOT know how to render data (View handles that)
 * 
 * 3. OPEN/CLOSED PRINCIPLE (OCP):
 *    - Open for extension: new operations can be added (e.g., new transaction types)
 *    - Closed for modification: existing operations work with interface-based dependencies
 *    - Changes to Persistence implementation don't require changes here
 * 
 * 4. LISKOV SUBSTITUTION PRINCIPLE (LSP):
 *    - Any Persistence implementation can substitute another without affecting behavior
 *    - loadAccounts(), saveATMState() work identically regardless of concrete implementation
 */
// ATMService class - acts as the Controller/Business Logic orchestrator for ATM operations
public class ATMService {
    // ============== INSTANCE FIELDS: ATM STATE ==============
    // accounts field - list of all bank accounts in the system (loaded from database)
    // Updated when new accounts created or account data changes
    private List<Account> accounts;
    
    // currentAccount field - the account currently logged in by user (null if no user logged in)
    // Set during authentication, cleared during logout
    private Account currentAccount;
    
    // atmCash field - total cash available in the ATM machine (in dollars)
    // Decreased by withdrawals, increased by deposits
    private double atmCash;
    
    // paperTank field - tracks paper sheet inventory for receipt printing
    // Used to check availability and log consumption during receipt printing
    private PaperTank paperTank;
    
    // inkTank field - tracks ink unit inventory for receipt printing
    // Used to check availability and log consumption during receipt printing
    private InkTank inkTank;
    
    // persistence field - reference to Persistence abstraction (DIP principle)
    // Allows this class to work with any Persistence implementation without knowing concrete type
    // SINGLETON-LIKE: receives SINGLE JDBCHandler instance from Main.main()
    private Persistence persistence;
    
    // ============== SINGLETON REFERENCE: Optional Direct Access ==============
    // jdbcHandler field - reference to concrete JDBCHandler for transaction logging
    // Optional: only used if persistence is actually a JDBCHandler instance
    // Used for logging transactions, account creation, and retrieving transaction history
    // This stores the SINGLE instance for specialized operations (logging, audit trails)
    private JDBCHandler jdbcHandler;

    // ============== CONSTRUCTOR: Initialize ATMService ==============
    // ATMService() constructor - initializes the controller with a Persistence implementation
    // Parameter: persistence - the Persistence interface implementation (e.g., JDBCHandler)
    public ATMService(Persistence persistence) {
        // Store the Persistence abstraction for later data operations
        this.persistence = persistence;
        // Use instanceof to check if persistence is a JDBCHandler (for optional logging features)
        // If it is, cast and store it; if not, set to null (logging features won't be available)
        this.jdbcHandler = (persistence instanceof JDBCHandler) ? (JDBCHandler) persistence : null;
        // Load all accounts from persistent storage (database)
        // Each account will have its data (balance, pin, holder name, etc.)
        this.accounts = persistence.loadAccounts();
        // Load current ATM cash balance from persistent storage
        // This is the total money available in the physical ATM machine
        this.atmCash = persistence.loadATMCash();
        // Load paper tank state from persistent storage
        // Creates a PaperTank object with the current paper sheet count
        this.paperTank = persistence.loadPaperTank();
        // Load ink tank state from persistent storage
        // Creates an InkTank object with the current ink unit count
        this.inkTank = persistence.loadInkTank();
        // Initialize currentAccount to null (no user logged in yet)
        // Will be set when a user successfully authenticates
        this.currentAccount = null;
    }

    // ============== AUTHENTICATION: User Login ==============
    // authenticate() method - verifies account number and PIN, establishes user session
    // Parameter: accountNumber - the account number entered by user
    // Parameter: pin - the 4-digit PIN entered by user
    // Returns: Account object if authentication successful, null if failed
    public Account authenticate(String accountNumber, String pin) {
        // Search accounts list for account matching the entered account number
        // Use Stream API: filter to find matching account, findFirst to get first match
        Account account = accounts.stream()
            // Filter: keep only accounts where account number matches entered number
            .filter(a -> a.getAccountNumber().equals(accountNumber))
            // findFirst: get the first (and should be only) matching account
            .findFirst()
            // orElse: if no matching account found, return null
            .orElse(null);
        
        // Check if account was found AND PIN verification succeeds
        // account != null: account exists in system
        // account.verifyPin(pin): PIN matches stored PIN for this account
        if (account != null && account.verifyPin(pin)) {
            // Authentication succeeded: store account as current session
            // This marks user as logged in for subsequent operations
            this.currentAccount = account;
            // Return the authenticated Account object to View
            return account;
        }
        // Authentication failed: either account not found or PIN incorrect
        // Return null to indicate failed authentication
        return null;
    }

    // ============== SESSION MANAGEMENT: Logout ==============
    // logout() method - ends the current user session
    // Clears the currentAccount reference, preventing further operations without new login
    public void logout() {
        // Set currentAccount to null to indicate no user is logged in
        // Subsequent operations will fail with "No account logged in" error
        this.currentAccount = null;
    }

    // ============== ACCESSOR: Get Current Account ==============
    // getCurrentAccount() method - retrieves the currently logged-in account
    // Returns: Account object of current user, or null if no user logged in
    public Account getCurrentAccount() {
        // Return the currentAccount field (could be Account object or null)
        return currentAccount;
    }

    // ============== BALANCE INQUIRY: Display Account Information ==============
    // displayAccountInfo() method - displays current account details and prints receipt
    // Shows: account holder name, account number, current balance
    public void displayAccountInfo() {
        // Check if currentAccount is null (no user logged in)
        if (currentAccount == null) {
            // Display error message: cannot show account info without login
            System.out.println("No account logged in.");
            // Exit method early - no further processing needed
            return;
        }
        // Account is logged in: display account information
        // Print header separator for clarity
        System.out.println("\n--- Account Information ---");
        // Print account holder name (retrieved from current account)
        System.out.println("Account Holder: " + currentAccount.getAccountHolder());
        // Print account number (unique identifier)
        System.out.println("Account Number: " + currentAccount.getAccountNumber());
        // Print current balance with 2 decimal places formatting (currency format)
        System.out.println("Current Balance: $" + String.format("%.2f", currentAccount.getBalance()));
        
        // Print a receipt for this balance check transaction
        // Parameter: "BALANCE CHECK" identifies the transaction type in receipt
        printReceipt("BALANCE CHECK");
        // Save the updated state to persistent storage (paper/ink consumption logged)
        saveState();
    }

    // ============== WITHDRAWAL: Withdraw Cash ==============
    // withdraw() method - processes cash withdrawal from current account
    // Parameter: amount - the amount to withdraw in dollars
    // Returns: boolean - true if withdrawal successful, false if failed
    public boolean withdraw(double amount) {
        // Check if a user is currently logged in (currentAccount is not null)
        if (currentAccount == null) {
            // No user logged in: cannot perform withdrawal
            System.out.println("Error: No account logged in.");
            // Return false to indicate withdrawal failed
            return false;
        }

        // Validate that withdrawal amount is positive and non-zero
        if (amount <= 0) {
            // Amount is invalid (zero or negative)
            System.out.println("Error: Invalid amount.");
            // Return false to indicate withdrawal failed
            return false;
        }

        // Check if ATM has sufficient cash to dispense
        // Compare requested withdrawal amount with available ATM cash
        if (amount > atmCash) {
            // ATM doesn't have enough cash for this withdrawal
            // Display error with available cash amount for user reference
            System.out.println("Error: ATM does not have sufficient cash. Available: $" + String.format("%.2f", atmCash));
            // Return false to indicate withdrawal failed
            return false;
        }

        // Save previous balance for transaction logging (before withdrawal)
        // This is needed to record the balance change in transaction history
        double previousBalance = currentAccount.getBalance();
        // Attempt to withdraw amount from account (validates sufficient funds)
        // withdraw() method on Account checks if account balance >= amount
        // If successful, subtracts amount from account balance
        // If fails, returns false
        if (!currentAccount.withdraw(amount)) {
            // Account doesn't have sufficient funds (insufficient balance)
            System.out.println("Error: Insufficient funds in your account.");
            // Return false to indicate withdrawal failed
            return false;
        }

        // Withdrawal succeeded: deduct cash from ATM machine
        // Reduce ATM cash inventory by the withdrawn amount
        atmCash -= amount;
        // Get the account's new balance after withdrawal
        double newBalance = currentAccount.getBalance();
        // Display success message to user
        System.out.println("Withdrawal successful! Dispensing $" + String.format("%.2f", amount));
        // Display updated account balance
        System.out.println("Remaining Balance: $" + String.format("%.2f", newBalance));
        
        // Print receipt for this withdrawal transaction
        printReceipt("WITHDRAWAL");
        
        // Log transaction to database if JDBCHandler is available
        // Check if jdbcHandler was successfully initialized in constructor
        if (jdbcHandler != null) {
            // Log the withdrawal transaction with all details
            // Parameters: account number, holder name, transaction type, amount, previous balance, new balance
            jdbcHandler.logTransaction(currentAccount.getAccountNumber(), currentAccount.getAccountHolder(), 
                                      "WITHDRAWAL", amount, previousBalance, newBalance);
        }
        
        // Save updated ATM state to persistent storage
        // Updates all accounts (with new balance), atmCash, paperTank, inkTank in database
        saveState();
        // Return true to indicate withdrawal was successful
        return true;
    }

    // ============== DEPOSIT: Deposit Cash ==============
    // deposit() method - processes cash deposit to current account
    // Parameter: amount - the amount to deposit in dollars
    // Returns: boolean - true if deposit successful, false if failed
    public boolean deposit(double amount) {
        // Check if a user is currently logged in
        if (currentAccount == null) {
            // No user logged in: cannot perform deposit
            System.out.println("Error: No account logged in.");
            // Return false to indicate deposit failed
            return false;
        }

        // Validate that deposit amount is positive and non-zero
        if (amount <= 0) {
            // Amount is invalid (zero or negative)
            System.out.println("Error: Invalid amount.");
            // Return false to indicate deposit failed
            return false;
        }

        // Save previous balance for transaction logging (before deposit)
        double previousBalance = currentAccount.getBalance();
        // Add deposit amount to account balance
        // This is a business logic operation on the Account model
        currentAccount.deposit(amount);
        // Add deposited cash to ATM machine's cash reserve
        // ATM now has more cash available for future withdrawals
        atmCash += amount;
        // Get account's new balance after deposit
        double newBalance = currentAccount.getBalance();
        // Display success message to user
        System.out.println("Deposit successful! Amount deposited: $" + String.format("%.2f", amount));
        // Display new updated balance
        System.out.println("New Balance: $" + String.format("%.2f", newBalance));
        
        // Print receipt for this deposit transaction
        printReceipt("DEPOSIT");
        
        // Log transaction to database if JDBCHandler is available
        if (jdbcHandler != null) {
            // Log the deposit transaction with all details
            jdbcHandler.logTransaction(currentAccount.getAccountNumber(), currentAccount.getAccountHolder(), 
                                      "DEPOSIT", amount, previousBalance, newBalance);
        }
        
        // Save updated ATM state to persistent storage
        saveState();
        // Return true to indicate deposit was successful
        return true;
    }

    // ============== PIN CHANGE: Change Account PIN ==============
    // changePin() method - allows user to change their account PIN
    // Parameter: newPin - the new 4-digit PIN to set
    public void changePin(String newPin) {
        // Check if a user is currently logged in
        if (currentAccount == null) {
            // No user logged in: cannot change PIN
            System.out.println("Error: No account logged in.");
            // Exit method early - no further processing
            return;
        }

        // Update the account's PIN to the new value
        // This calls the Account model's setPin() method
        currentAccount.setPin(newPin);
        // Display success message to user
        System.out.println("PIN changed successfully.");
        
        // Log transaction to database if JDBCHandler is available
        // Note: PIN_CHANGE transaction logs amount as 0.0 since no money moves
        // Balance doesn't change, so previous and new balance are the same
        if (jdbcHandler != null) {
            // Log the PIN change operation for audit trail
            jdbcHandler.logTransaction(currentAccount.getAccountNumber(), currentAccount.getAccountHolder(), 
                                      "PIN_CHANGE", 0.0, currentAccount.getBalance(), currentAccount.getBalance());
        }
        
        // Save updated ATM state to persistent storage
        saveState();
    }

    // ============== RECEIPT PRINTING: Print Transaction Receipt ==============
    // printReceipt() method - prints a receipt if paper and ink available
    // Parameter: transactionType - string describing transaction (e.g., "WITHDRAWAL", "DEPOSIT")
    // This is a private helper method called by other methods
    private void printReceipt(String transactionType) {
        // Check if both paper and ink are available (required for printing)
        // paperTank.hasPaper(): returns true if paper sheets > 0
        // inkTank.hasInk(): returns true if ink units > 0
        if (paperTank.hasPaper() && inkTank.hasInk()) {
            // Both resources available: proceed with printing
            // Consume one sheet of paper
            paperTank.useSheet();
            // Consume one unit of ink
            inkTank.useInk(1);
            // Display receipt confirmation with remaining resources
            System.out.println("Receipt printed - Paper: " + paperTank.getCurrentSheets() + 
                             " sheets left, Ink: " + inkTank.getCurrentInk() + " units left");
        } else {
            // One or both resources unavailable: cannot print receipt
            // Check specifically for paper shortage
            if (!paperTank.hasPaper()) {
                // No paper available: display warning
                System.out.println("Warning: Out of paper. Cannot print receipt.");
            }
            // Check specifically for ink shortage
            if (!inkTank.hasInk()) {
                // No ink available: display warning
                System.out.println("Warning: Out of ink. Cannot print receipt.");
            }
        }
    }

    // ============== STATE PERSISTENCE: Save ATM State ==============
    // saveState() method - persists current ATM state to database
    // This is a private helper method called after each state-modifying operation
    private void saveState() {
        // Delegate to Persistence interface to save complete ATM state
        // Parameters: current accounts list, ATM cash, paper tank state, ink tank state
        // This updates the database with all current values
        persistence.saveATMState(accounts, atmCash, paperTank, inkTank);
    }

    // ============== ACCOUNT CREATION: Create New Account ==============
    // createNewAccount() method - adds a new account to the system
    // Parameter: newAccount - the Account object to create (with account number, holder, balance, PIN)
    public void createNewAccount(Account newAccount) {
        // Check if account with this account number already exists
        // Use Stream API to search accounts list for duplicate account number
        boolean exists = accounts.stream()
            // Filter: find any account where account number matches the new account
            .anyMatch(a -> a.getAccountNumber().equals(newAccount.getAccountNumber()));
        
        // Check if account already exists
        if (exists) {
            // Account number is not unique: cannot create duplicate account
            System.out.println("Error: Account number already exists.");
            // Exit method early - don't create duplicate
            return;
        }
        
        // Account number is unique: add new account to system
        // Add the new account to the accounts list
        accounts.add(newAccount);
        // Display confirmation message
        System.out.println("New account created: " + newAccount.getAccountNumber());
        
        // Log account creation to database if JDBCHandler is available
        if (jdbcHandler != null) {
            // Log the account creation event
            // Amount is initial balance, previous balance is 0.0
            jdbcHandler.logTransaction(newAccount.getAccountNumber(), newAccount.getAccountHolder(), 
                                      "ACCOUNT_CREATED", newAccount.getBalance(), 0.0, newAccount.getBalance());
        }
        
        // Save updated ATM state to persistent storage (new account added to database)
        saveState();
    }

    // ============== TRANSFER: Transfer Funds Between Accounts ==============
    // transfer() method - transfers money from current account to recipient account
    // Parameter: recipientAccountNumber - account number to transfer funds to
    // Parameter: amount - amount to transfer in dollars
    // Returns: boolean - true if transfer successful, false if failed
    public boolean transfer(String recipientAccountNumber, double amount) {
        // Check if a user is currently logged in (sender account exists)
        if (currentAccount == null) {
            // No user logged in: cannot initiate transfer
            System.out.println("Error: No account logged in.");
            // Return false to indicate transfer failed
            return false;
        }

        // Validate that transfer amount is positive and non-zero
        if (amount <= 0) {
            // Amount is invalid
            System.out.println("Error: Invalid amount.");
            // Return false to indicate transfer failed
            return false;
        }

        // Search for recipient account in accounts list
        // Use Stream API to find account matching recipient account number
        Account recipientAccount = accounts.stream()
            // Filter: find account where account number matches recipient number
            .filter(a -> a.getAccountNumber().equals(recipientAccountNumber))
            // findFirst: get the first matching account
            .findFirst()
            // orElse: if not found, return null
            .orElse(null);

        // Check if recipient account was found
        if (recipientAccount == null) {
            // Recipient account doesn't exist in system
            System.out.println("Error: Recipient account not found.");
            // Return false to indicate transfer failed
            return false;
        }

        // Check if recipient account is the same as sender account
        // Can't transfer to yourself (that would just be a withdrawal)
        if (recipientAccountNumber.equals(currentAccount.getAccountNumber())) {
            // Sender and recipient are the same account
            System.out.println("Error: Cannot transfer to the same account.");
            // Return false to indicate transfer failed
            return false;
        }

        // Attempt to withdraw amount from sender's account
        // This validates that sender has sufficient funds
        if (!currentAccount.withdraw(amount)) {
            // Sender has insufficient funds
            System.out.println("Error: Insufficient funds in your account.");
            // Return false to indicate transfer failed
            return false;
        }

        // Withdrawal from sender succeeded: now add funds to recipient
        // Deposit the amount into recipient's account
        recipientAccount.deposit(amount);
        // Get sender's new balance after withdrawal
        double senderNewBalance = currentAccount.getBalance();
        // Get recipient's new balance after deposit
        double recipientNewBalance = recipientAccount.getBalance();

        // Display transfer completion message to sender
        System.out.println("Transfer successful!");
        // Show amount transferred
        System.out.println("Transferred: $" + String.format("%.2f", amount) + " to account " + recipientAccountNumber);
        // Show sender's updated balance
        System.out.println("Your new balance: $" + String.format("%.2f", senderNewBalance));

        // Print receipt for this transfer transaction
        printReceipt("TRANSFER");

        // Log transactions to database if JDBCHandler is available
        // Two transactions are logged: one for sender (TRANSFER_SENT) and one for recipient (TRANSFER_RECEIVED)
        if (jdbcHandler != null) {
            // Calculate sender's previous balance (before withdrawal)
            // Previous balance = new balance + amount withdrawn
            double previousBalance = senderNewBalance + amount;
            // Log the sender's transaction (money going out)
            jdbcHandler.logTransaction(currentAccount.getAccountNumber(), currentAccount.getAccountHolder(),
                                      "TRANSFER_SENT", amount, previousBalance, senderNewBalance);
            // Calculate recipient's previous balance (before deposit)
            // Previous balance = new balance - amount deposited
            double recipientPreviousBalance = recipientNewBalance - amount;
            // Log the recipient's transaction (money coming in)
            jdbcHandler.logTransaction(recipientAccount.getAccountNumber(), recipientAccount.getAccountHolder(),
                                      "TRANSFER_RECEIVED", amount, recipientPreviousBalance, recipientNewBalance);
        }

        // Save updated ATM state to persistent storage (both accounts modified)
        saveState();
        // Return true to indicate transfer was successful
        return true;
    }

    // ============== TRANSACTION HISTORY: Display Transaction Log ==============
    // displayTransactionHistory() method - shows past transactions for current account
    // Retrieves and displays transaction history from database
    public void displayTransactionHistory() {
        // Check if a user is currently logged in
        if (currentAccount == null) {
            // No user logged in: cannot display history
            System.out.println("Error: No account logged in.");
            // Exit method early
            return;
        }

        // Check if JDBCHandler is available for database queries
        if (jdbcHandler == null) {
            // JDBCHandler not initialized: cannot query transaction history
            System.out.println("Error: Database handler not available.");
            // Exit method early
            return;
        }

        // Both conditions passed: display transaction history header
        System.out.println("\n=== TRANSACTION HISTORY FOR ACCOUNT " + currentAccount.getAccountNumber() + " ===");
        // Query database for all transactions for this account
        // This method retrieves and displays transactions from the database
        jdbcHandler.getTransactionHistory(currentAccount.getAccountNumber());
    }

    // ============== GETTERS: Accessor Methods for Testing/Debugging ==============
    // These methods allow external code (tests) to retrieve internal state for verification
    
    // getAccounts() - retrieve the list of all accounts
    // Returns: List<Account> - all accounts in system
    public List<Account> getAccounts() { 
        // Return the accounts list
        return accounts; 
    }
    
    // getATMCash() - retrieve current ATM cash balance
    // Returns: double - total cash available in ATM
    public double getATMCash() { 
        // Return the atmCash amount
        return atmCash; 
    }
    
    // setATMCash() - set the ATM cash balance (used by technician operations)
    // Parameter: cash - new cash amount to set
    public void setATMCash(double cash) {
        // Set the atmCash to new value
        this.atmCash = cash;
    }
    
    // getPaperTank() - retrieve paper tank state
    // Returns: PaperTank - the paper tank object
    public PaperTank getPaperTank() { 
        // Return the paper tank
        return paperTank; 
    }
    
    // getInkTank() - retrieve ink tank state
    // Returns: InkTank - the ink tank object
    public InkTank getInkTank() { 
        // Return the ink tank
        return inkTank; 
    }
}
