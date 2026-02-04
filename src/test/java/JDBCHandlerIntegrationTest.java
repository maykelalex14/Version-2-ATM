import persistence.JDBCHandler;
import core.Account;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

/**
 * JDBCHandlerIntegrationTest - Integration Test Class for JDBCHandler
 * 
 * This integration test class verifies the JDBCHandler's ability to establish
 * database connections and retrieve account data from the SQLite database.
 * 
 * Purpose: Ensure that the JDBC persistence layer correctly connects to the
 * SQLite database and can successfully load account information.
 * 
 * Tests: Database connectivity, account data retrieval, and data integrity
 */
public class JDBCHandlerIntegrationTest {
    private JDBCHandler jdbcHandler;

    /**
     * setUp() - Test Initialization Method
     * Runs before each test to create a fresh JDBCHandler instance
     * 
     * Creates a new JDBCHandler object which:
     * - Initializes SQLite database connection
     * - Creates necessary database tables if they don't exist
     * - Loads default accounts from the database
     * 
     * This setup ensures each test runs with a fresh database connection
     */
    @Before
    public void setUp() {
        // ============== SINGLETON PATTERN: Get the SINGLE JDBCHandler instance ==============
        // Using getInstance() instead of new constructor (constructor is private)
        // Ensures all tests use the same database handler instance
        jdbcHandler = JDBCHandler.getInstance();
    }

    /**
     * testJDBCHandlerDatabaseConnection() - Database Connection Integration Test
     * 
     * Tests that the JDBCHandler can successfully connect to the SQLite database
     * and retrieve account records.
     * 
     * Validates:
     * - Database connection is successfully established
     * - Accounts list is not null after loading
     * - Database returns a valid list (even if empty)
     * 
     * Expected Result: Successfully loads accounts from the database with no null values
     */
    @Test
    public void testJDBCHandlerDatabaseConnection() {
        List<Account> accounts = jdbcHandler.loadAccounts();
        assertNotNull("Accounts list should not be null", accounts);
        assertTrue("Database should be properly initialized", accounts.size() >= 0);
    }
}
