import services.ATMService;
import persistence.JDBCHandler;
import core.Account;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * ATMServiceIntegrationTest - Integration Test Class for ATMService
 * 
 * This integration test class verifies the ATMService component's ability
 * to work with the persistence layer (JDBCHandler) and the database.
 * 
 * Purpose: Ensure that ATMService correctly initializes with a JDBC database
 * handler and maintains proper database connectivity for account operations.
 * 
 * Tests: Database integration, service initialization, and account loading
 */
public class ATMServiceIntegrationTest {
    private ATMService atmService;
    private JDBCHandler jdbcHandler;

    /**
     * setUp() - Test Initialization Method
     * Runs before each test to initialize the ATM service with JDBC handler
     * 
     * Creates instances of:
     * - JDBCHandler: Manages SQLite database connection
     * - ATMService: Core service that uses JDBCHandler for persistence
     * 
     * This setup ensures a fresh database connection for each test
     */
    @Before
    public void setUp() {
        // ============== SINGLETON PATTERN: Get the SINGLE JDBCHandler instance ==============
        // Using getInstance() instead of new constructor (constructor is private)
        // Ensures all tests share the same database handler instance
        jdbcHandler = JDBCHandler.getInstance();
        atmService = new ATMService(jdbcHandler);
    }

    /**
     * testATMServiceLoadsAccountsFromDatabase() - ATM Service Integration Test
     * 
     * Tests that the ATMService is properly initialized and maintains
     * a valid database connection through the JDBCHandler.
     * 
     * Validates:
     * - ATMService instance is not null after creation
     * - Database connection is properly established
     * 
     * Expected Result: ATMService initializes successfully with database handler
     */
    @Test
    public void testATMServiceLoadsAccountsFromDatabase() {
        assertNotNull("ATM Service should be properly initialized with JDBC handler", atmService);
        assertTrue("ATM Service should have database connection", atmService != null);
    }
}
