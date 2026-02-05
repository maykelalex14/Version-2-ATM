package persistence;

import core.*;
import interfaces.Persistence;
import java.sql.*;
import java.util.*;
import java.util.List;

/**
 * ============== PERSISTENCE LAYER (MVC) ==============
 * JDBCHandler implements the Persistence interface to handle data persistence
 * Responsible for:
 * - Database initialization and connection management
 * - Storing and retrieving Models (Account, PaperTank, InkTank) from SQLite database
 * - Logging transactions and technician activities for audit trails
 * - Managing database transactions for data consistency
 * 
 * ============== SOLID PRINCIPLES APPLIED ==============
 * 1. DEPENDENCY INVERSION PRINCIPLE (DIP):
 *    - Implements Persistence interface - high-level modules depend on abstraction
 *    - ATMService depends on Persistence, NOT on JDBCHandler directly
 *    - Can be swapped with SqliteHandler or other implementations without affecting clients
 *    - Allows testing with mock Persistence implementations
 * 
 * 2. SINGLE RESPONSIBILITY PRINCIPLE (SRP):
 *    - Has ONE reason to change: when database logic or persistence mechanism changes
 *    - Focused responsibility: all data persistence concerns
 *    - Does NOT contain business logic (ATMService handles that)
 *    - Does NOT contain UI logic (Main.java handles that)
 *    - Does NOT manage Models' behavior (Models encapsulate their own logic)
 * 
 * 3. INTERFACE SEGREGATION PRINCIPLE (ISP):
 *    - Implements minimal Persistence interface (only required methods)
 *    - Additional methods (logTransaction, getTransactionHistory, etc.) are optional utilities
 *    - Clients implementing Persistence only depend on core persistence methods
 * 
 * ============== DESIGN PATTERN: SINGLETON (Bill Pugh Implementation) ==============
 * This class implements the SINGLETON design pattern using the Bill Pugh technique:
 * 
 * WHAT IS SINGLETON?
 * - A creational design pattern that restricts instantiation to ONE instance
 * - Ensures only ONE JDBCHandler instance exists throughout the application lifetime
 * - Provides a global point of access to that single instance
 * - Prevents resource waste (only 1 database connection pool)
 * 
 * WHY BILL PUGH SINGLETON (Best Practice)?
 * - THREAD-SAFE: Guaranteed by Java class loading mechanism (no synchronization needed)
 * - LAZY INITIALIZATION: Instance created only on first getInstance() call
 * - EFFICIENT: No synchronization overhead on every getInstance() call
 * - CLEAN: No double-checked locking complexity
 * - FOOLPROOF: Reflection/serialization attacks can be prevented if needed
 * 
 * HOW IT WORKS:
 * 1. JDBCHandlerHolder is a static inner class (loaded only when needed)
 * 2. INSTANCE field is initialized when JDBCHandlerHolder loads (only once)
 * 3. getInstance() returns the same INSTANCE every time (guaranteed)
 * 4. Java ClassLoader ensures thread-safety automatically
 * 
 * USAGE PATTERN:
 * // Get the SINGLE instance from anywhere in the application
 * JDBCHandler handler = JDBCHandler.getInstance();
 * 
 * BENEFITS IN THIS ATM APPLICATION:
 * - ONE database connection for entire ATM system
 * - No resource leaks from multiple connections
 * - Consistent state across all ATM operations
 * - Clean, testable code (can mock the Persistence interface)
 * 
 * DESIGN PATTERN: Strategy Pattern
 * - Persistence interface defines the persistence "strategy"
 * - JDBCHandler is one concrete strategy using JDBC/SQLite
 * - Another strategy could use JPA, REST API, file storage, etc.
 * - Switching strategies requires NO changes to business logic
 * 
 * DATABASE SCHEMA:
 * - accounts: Stores Account models (account_number, holder, balance, pin)
 * - atm_state: Stores ATM machine state (cash, paper, ink)
 * - transactions: Audit log of all transactions (for compliance and analysis)
 * - technician_activities: Audit log of technician operations (for security)
 * - technician_credentials: Credentials for technician authentication
 */
public class JDBCHandler implements Persistence {
    // ============== DATABASE CONFIGURATION ==============
    private static final String DATABASE_URL = "jdbc:sqlite:data/atm_database.db";
    private static final String DRIVER = "org.sqlite.JDBC";
    
    // ============== SINGLETON PATTERN: Bill Pugh Implementation ==============
    // This static inner class is the key to the Bill Pugh Singleton pattern
    // It is NOT loaded until getInstance() is first called
    // When loaded, it creates the SINGLE JDBCHandler instance (guaranteed by Java ClassLoader)
    private static class JDBCHandlerHolder {
        // SINGLETON INSTANCE: Created once and only once when this class loads
        // static final ensures it's immutable and thread-safe (Java guarantees)
        static final JDBCHandler INSTANCE = new JDBCHandler();
    }
    
    // ============== SINGLETON GETTER ==============
    // PUBLIC METHOD to get the SINGLE instance of JDBCHandler
    // Thread-safe without synchronization (Java ClassLoader handles synchronization)
    // Lazy initialization: instance created on first call
    // Every subsequent call returns the SAME instance
    public static JDBCHandler getInstance() {
        // ============== SINGLETON RETURN: always return the SAME instance ==============
        return JDBCHandlerHolder.INSTANCE;
    }
    
    // ============== INSTANCE FIELDS ==============
    // connection field - maintains a SINGLE persistent connection to the database
    // This works with the Singleton pattern to ensure only ONE connection throughout app
    private Connection connection;

    // ============== CONSTRUCTOR: Initialize Database ==============
    // PRIVATE CONSTRUCTOR: prevents instantiation from outside this class
    // Only JDBCHandlerHolder can instantiate via getInstance()
    // This enforces the Singleton pattern - no "new JDBCHandler()" allowed from outside
    private JDBCHandler() {
        try {
            initializeDatabase();
        } catch (Exception e) {
            System.err.println("Error initializing JDBC database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============== CONNECTION MANAGEMENT ==============
    // Handles lazy initialization and connection pooling
    // SINGLETON PATTERN IMPLEMENTATION:
    // - This method implements LAZY INITIALIZATION (Singleton best practice)
    // - First call creates the connection
    // - Subsequent calls reuse the SAME connection instance
    // - Thread-safe would require synchronized keyword (simple version shown)
    // - Ensures only ONE active database connection throughout application lifetime
    private Connection getConnection() throws SQLException {
        // SINGLETON CHECK: if connection doesn't exist OR connection is closed
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName(DRIVER);
            } catch (ClassNotFoundException e) {
                System.err.println("SQLite JDBC Driver not found: " + e.getMessage());
            }
            // SINGLETON INITIALIZATION: create the SINGLE connection instance
            connection = DriverManager.getConnection(DATABASE_URL);
            connection.setAutoCommit(true);
        }
        // SINGLETON RETURN: always return the SAME connection instance
        return connection;
    }

    // ============== DATABASE INITIALIZATION ==============
    // Creates all necessary tables and initializes default data
    // Demonstrates: Schema creation, transaction handling, initial data seeding
    private void initializeDatabase() throws SQLException {
        try {
            // Create data directory if needed
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("data"));
        } catch (Exception e) {
            System.err.println("Error creating data directory: " + e.getMessage());
        }

        Connection conn = getConnection();
        
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            
            // ============== Create accounts table (Model storage) ==============
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS accounts (" +
                "  account_number TEXT PRIMARY KEY," +
                "  account_holder TEXT NOT NULL," +
                "  balance REAL NOT NULL," +
                "  pin TEXT NOT NULL" +
                ")"
            );

            // ============== Create atm_state table (Machine state) ==============
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS atm_state (" +
                "  id INTEGER PRIMARY KEY," +
                "  atm_cash REAL NOT NULL," +
                "  paper_sheets INTEGER NOT NULL," +
                "  ink INTEGER NOT NULL," +
                "  last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // ============== Create bank_notes table (Track ATM note inventory) ==============
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS bank_notes (" +
                "  denomination INTEGER PRIMARY KEY," +
                "  quantity INTEGER NOT NULL DEFAULT 0," +
                "  last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // ============== Create transactions table (Audit log for compliance) ==============
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS transactions (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  account_number TEXT NOT NULL," +
                "  account_holder TEXT NOT NULL," +
                "  transaction_type TEXT NOT NULL," +
                "  amount REAL NOT NULL," +
                "  previous_balance REAL NOT NULL," +
                "  new_balance REAL NOT NULL," +
                "  bank_notes_breakdown TEXT," +
                "  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (account_number) REFERENCES accounts(account_number)" +
                ")"
            );

            // ============== Create technician_activities table (Audit log for security) ==============
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS technician_activities (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  activity_type TEXT NOT NULL," +
                "  amount REAL," +
                "  description TEXT," +
                "  previous_value REAL," +
                "  new_value REAL," +
                "  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // ============== Create technician_credentials table ==============
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS technician_credentials (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  username TEXT NOT NULL UNIQUE," +
                "  password TEXT NOT NULL," +
                "  full_name TEXT," +
                "  role TEXT DEFAULT 'TECHNICIAN'," +
                "  created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  last_login TIMESTAMP" +
                ")"
            );

            // ============== Check if technician credentials exist, if not insert default ==============
            rs = stmt.executeQuery("SELECT COUNT(*) FROM technician_credentials");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute(
                    "INSERT INTO technician_credentials (username, password, full_name, role) " +
                    "VALUES ('admin', '1234', 'System Administrator', 'TECHNICIAN')"
                );
            }
            rs.close();

            // ============== Initialize bank notes if empty ==============
            rs = stmt.executeQuery("SELECT COUNT(*) FROM bank_notes");
            if (rs.next() && rs.getInt(1) == 0) {
                // Initialize ATM with standard bank note denominations
                stmt.execute("INSERT INTO bank_notes (denomination, quantity) VALUES (5, 100)");
                stmt.execute("INSERT INTO bank_notes (denomination, quantity) VALUES (10, 100)");
                stmt.execute("INSERT INTO bank_notes (denomination, quantity) VALUES (20, 150)");
                stmt.execute("INSERT INTO bank_notes (denomination, quantity) VALUES (50, 50)");
                stmt.execute("INSERT INTO bank_notes (denomination, quantity) VALUES (100, 50)");
            }
            rs.close();

            // ============== Initialize default data if empty ==============
            rs = stmt.executeQuery("SELECT COUNT(*) FROM accounts");
            int accountCount = 0;
            if (rs.next()) {
                accountCount = rs.getInt(1);
            }
            rs.close();

            // Check if accounts table is empty and initialize with default data
            if (accountCount == 0) {
                stmt.execute("INSERT INTO accounts (account_number, account_holder, balance, pin) VALUES ('1001', 'John Doe', 1500.0, '1234')");
                stmt.execute("INSERT INTO accounts (account_number, account_holder, balance, pin) VALUES ('1002', 'Jane Smith', 2500.0, '5678')");
                stmt.execute("INSERT INTO accounts (account_number, account_holder, balance, pin) VALUES ('1003', 'Bob Johnson', 3390.5, '2222')");
            }

            // ============== Ensure ATM state exists ==============
            rs = stmt.executeQuery("SELECT COUNT(*) FROM atm_state");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute(
                    "INSERT INTO atm_state (atm_cash, paper_sheets, ink) " +
                    "VALUES (10000.0, 50, 500)"
                );
            }
            rs.close();

            System.out.println("[INFO] SQLite Database initialized successfully");
        } finally {
            if (stmt != null) stmt.close();
        }
    }

    // ============== PERSISTENCE INTERFACE IMPLEMENTATION ==============
    // These methods implement the Persistence abstraction (DIP)
    
    /**
     * ============== LOAD ACCOUNTS (DIP Implementation) ==============
     * Loads all Account models from persistent storage
     * Called by ATMService during initialization
     */
    @Override
    public List<Account> loadAccounts() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT account_number, account_holder, balance, pin FROM accounts";

        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // ============== SRP: Materialize database rows into Model objects ==============
            while (rs.next()) {
                String accountNumber = rs.getString("account_number");
                String accountHolder = rs.getString("account_holder");
                double balance = rs.getDouble("balance");
                String pin = rs.getString("pin");

                accounts.add(new Account(accountNumber, accountHolder, balance, pin));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error loading accounts: " + e.getMessage());
        }

        return accounts;
    }

    /**
     * ============== LOAD ATM CASH (DIP Implementation) ==============
     * Loads ATM cash reserve from persistent storage
     */
    @Override
    public double loadATMCash() {
        String sql = "SELECT atm_cash FROM atm_state WHERE id = 1";
        double cash = 10000.0;

        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                cash = rs.getDouble("atm_cash");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error loading ATM cash: " + e.getMessage());
        }

        return cash;
    }

    /**
     * ============== LOAD PAPER TANK (DIP Implementation) ==============
     * Loads PaperTank model from persistent storage
     */
    @Override
    public PaperTank loadPaperTank() {
        String sql = "SELECT paper_sheets FROM atm_state WHERE id = 1";
        int sheets = 50;

        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                sheets = rs.getInt("paper_sheets");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error loading paper tank: " + e.getMessage());
        }

        return new PaperTank(sheets);
    }

    /**
     * ============== LOAD INK TANK (DIP Implementation) ==============
     * Loads InkTank model from persistent storage
     */
    @Override
    public InkTank loadInkTank() {
        String sql = "SELECT ink FROM atm_state WHERE id = 1";
        int ink = 500;

        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                ink = rs.getInt("ink");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error loading ink tank: " + e.getMessage());
        }

        return new InkTank(ink);
    }

    /**
     * ============== SAVE ATM STATE (DIP Implementation) ==============
     * Persists complete ATM state to database
     * Demonstrates: Transaction handling, batch operations, rollback on error
     */
    @Override
    public void saveATMState(List<Account> accounts, double atmCash, PaperTank paperTank, InkTank inkTank) {
        try (Connection conn = getConnection()) {
            // ============== TRANSACTION MANAGEMENT: Disable auto-commit for consistency ==============
            conn.setAutoCommit(false);

            try {
                // ============== Update ATM state (cash, paper, ink) ==============
                String updateStateSQL = "UPDATE atm_state SET atm_cash = ?, paper_sheets = ?, ink = ? WHERE id = 1";
                try (PreparedStatement pstmt = conn.prepareStatement(updateStateSQL)) {
                    pstmt.setDouble(1, atmCash);
                    pstmt.setInt(2, paperTank.getCurrentSheets());
                    pstmt.setInt(3, inkTank.getCurrentInk());
                    pstmt.executeUpdate();
                }

                // ============== Delete old accounts ==============
                String deleteAccountsSQL = "DELETE FROM accounts";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(deleteAccountsSQL);
                }

                // ============== Batch insert all accounts (efficient) ==============
                String insertAccountSQL = "INSERT INTO accounts (account_number, account_holder, balance, pin) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertAccountSQL)) {
                    for (Account account : accounts) {
                        pstmt.setString(1, account.getAccountNumber());
                        pstmt.setString(2, account.getAccountHolder());
                        pstmt.setDouble(3, account.getBalance());
                        pstmt.setString(4, account.getPin());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }

                // ============== TRANSACTION MANAGEMENT: Commit if all operations succeeded ==============
                conn.commit();
                System.out.println("[INFO] ATM state saved successfully");
            } catch (SQLException e) {
                // ============== TRANSACTION MANAGEMENT: Rollback on any error ==============
                conn.rollback();
                System.err.println("Error saving ATM state: " + e.getMessage());
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error in saveATMState: " + e.getMessage());
        }
    }

    // ============== CONNECTION CLEANUP ==============
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[INFO] Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    // ============== TRANSACTION LOGGING (Audit Trail for Compliance) ==============
    // Called by ATMService to log all financial transactions
    // Purpose: Maintain audit trail for regulatory compliance and analysis
    // ============== BANK NOTES MANAGEMENT ==============
    /**
     * Load all bank notes from database
     * @return List of BankNote objects with current denominations and quantities
     */
    public List<BankNote> loadBankNotes() {
        List<BankNote> bankNotes = new ArrayList<>();
        String sql = "SELECT denomination, quantity FROM bank_notes ORDER BY denomination DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int denomination = rs.getInt("denomination");
                int quantity = rs.getInt("quantity");
                bankNotes.add(new BankNote(denomination, quantity));
            }
        } catch (SQLException e) {
            System.err.println("Error loading bank notes: " + e.getMessage());
        }

        return bankNotes;
    }

    /**
     * Save bank notes to database (after dispensing notes)
     * @param bankNotes - List of BankNote objects to save
     */
    public void saveBankNotes(List<BankNote> bankNotes) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try {
                String updateSQL = "UPDATE bank_notes SET quantity = ? WHERE denomination = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                    for (BankNote note : bankNotes) {
                        pstmt.setInt(1, note.getQuantity());
                        pstmt.setInt(2, note.getDenomination());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error saving bank notes: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Error in saveBankNotes: " + e.getMessage());
        }
    }

    // ============== OVERLOADED LOG TRANSACTION WITH BANK NOTES ==============
    /**
     * Log transaction with bank notes breakdown
     * @param accountNumber - Account performing transaction
     * @param accountHolder - Account holder name
     * @param transactionType - WITHDRAWAL, DEPOSIT, TRANSFER, etc.
     * @param amount - Transaction amount
     * @param previousBalance - Balance before transaction
     * @param newBalance - Balance after transaction
     * @param bankNotesBreakdown - String representation of bank notes used (e.g., "100x$5, 50x$10")
     */
    public void logTransaction(String accountNumber, String accountHolder, String transactionType, 
                               double amount, double previousBalance, double newBalance, String bankNotesBreakdown) {
        String sql = "INSERT INTO transactions (account_number, account_holder, transaction_type, amount, previous_balance, new_balance, bank_notes_breakdown) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            pstmt.setString(2, accountHolder);
            pstmt.setString(3, transactionType);
            pstmt.setDouble(4, amount);
            pstmt.setDouble(5, previousBalance);
            pstmt.setDouble(6, newBalance);
            pstmt.setString(7, bankNotesBreakdown);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging transaction: " + e.getMessage());
        }
    }

    // ============== ORIGINAL LOG TRANSACTION (For backwards compatibility) ==============
    /**
     * Log transaction without bank notes breakdown
     * (Kept for backwards compatibility with existing code)
     */
    public void logTransaction(String accountNumber, String accountHolder, String transactionType, 
                               double amount, double previousBalance, double newBalance) {
        logTransaction(accountNumber, accountHolder, transactionType, amount, previousBalance, newBalance, null);
    }

    // ============== TRANSACTION HISTORY RETRIEVAL ==============
    // Called by ATMService to display user's transaction history
    public void getTransactionHistory(String accountNumber) {
        String sql = "SELECT id, transaction_type, amount, previous_balance, new_balance, timestamp FROM transactions " +
                     "WHERE account_number = ? ORDER BY id DESC LIMIT 10";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();

            boolean hasTransactions = false;
            System.out.println("\n" + "=".repeat(100));
            System.out.printf("%-5s | %-20s | %-12s | %-12s | %-12s | %s\n", 
                            "ID", "Type", "Amount", "Prev Balance", "New Balance", "Timestamp");
            System.out.println("=".repeat(100));

            while (rs.next()) {
                hasTransactions = true;
                int id = rs.getInt("id");
                String type = rs.getString("transaction_type");
                double amount = rs.getDouble("amount");
                double prevBalance = rs.getDouble("previous_balance");
                double newBalance = rs.getDouble("new_balance");
                String timestamp = rs.getString("timestamp");

                System.out.printf("%-5d | %-20s | $%-11.2f | $%-11.2f | $%-11.2f | %s\n",
                                id, type, amount, prevBalance, newBalance, timestamp);
            }

            if (!hasTransactions) {
                System.out.println("No transaction history found for this account.");
            }
            System.out.println("=".repeat(100));

            rs.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving transaction history: " + e.getMessage());
        }
    }

    // ============== TECHNICIAN ACTIVITY LOGGING (Audit Trail for Security) ==============
    // Called by BankTechnician to log all maintenance operations
    // Purpose: Track technician actions for security and compliance
    public void logTechnicianActivity(String activityType, double amount, String description, 
                                     double previousValue, double newValue) {
        String sql = "INSERT INTO technician_activities (activity_type, amount, description, previous_value, new_value) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, activityType);
            if (amount != 0) {
                pstmt.setDouble(2, amount);
            } else {
                pstmt.setNull(2, java.sql.Types.DOUBLE);
            }
            pstmt.setString(3, description);
            if (previousValue >= 0) {
                pstmt.setDouble(4, previousValue);
            } else {
                pstmt.setNull(4, java.sql.Types.DOUBLE);
            }
            if (newValue >= 0) {
                pstmt.setDouble(5, newValue);
            } else {
                pstmt.setNull(5, java.sql.Types.DOUBLE);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging technician activity: " + e.getMessage());
        }
    }

    // ============== TECHNICIAN ACTIVITY LOG RETRIEVAL ==============
    // Called by BankTechnician to view audit trail of technician operations
    public void getTechnicianActivityLog() {
        String sql = "SELECT id, activity_type, amount, description, previous_value, new_value, timestamp FROM technician_activities " +
                     "ORDER BY id DESC LIMIT 20";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            boolean hasActivities = false;
            System.out.println("\n" + "=".repeat(130));
            System.out.printf("%-5s | %-20s | %-12s | %-20s | %-15s | %-15s | %s\n", 
                            "ID", "Activity", "Amount", "Description", "Prev Value", "New Value", "Timestamp");
            System.out.println("=".repeat(130));

            while (rs.next()) {
                hasActivities = true;
                int id = rs.getInt("id");
                String activityType = rs.getString("activity_type");
                double amount = rs.getDouble("amount");
                String description = rs.getString("description");
                double prevValue = rs.getDouble("previous_value");
                double newValue = rs.getDouble("new_value");
                String timestamp = rs.getString("timestamp");

                String amountStr = amount != 0 ? String.format("$%.2f", amount) : "N/A";
                String prevStr = !rs.wasNull() ? String.format("%.2f", prevValue) : "N/A";
                String newStr = !rs.wasNull() ? String.format("%.2f", newValue) : "N/A";

                System.out.printf("%-5d | %-20s | %-12s | %-20s | %-15s | %-15s | %s\n",
                                id, activityType, amountStr, description, prevStr, newStr, timestamp);
            }

            if (!hasActivities) {
                System.out.println("No technician activities found.");
            }
            System.out.println("=".repeat(130));

            rs.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving technician activity log: " + e.getMessage());
        }
    }
}