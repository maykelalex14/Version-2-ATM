import services.AuthService;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * AuthServiceTest - Unit Test Class for AuthService
 * 
 * This test class verifies the authentication functionality of the AuthService component,
 * which handles technician login validation for the ATM system.
 * 
 * Purpose: Ensure that the AuthService correctly authenticates valid admin credentials
 * and prevents unauthorized access to the ATM technician features.
 * 
 * Tests: Authentication validation for admin access
 */
public class AuthServiceTest {
    private AuthService authService;

    /**
     * setUp() - Test Initialization Method
     * Runs before each test to create a fresh AuthService instance
     * 
     * Creates an AuthService object with default admin credentials:
     * - Username: admin
     * - Password: 1234
     * 
     * This setup initializes the authentication service for testing
     */
    @Before
    public void setUp() {
        authService = new AuthService();
    }

    /**
     * testAuthenticateWithCorrectCredentials() - Authentication Validation Test
     * 
     * Tests that the authenticate() method correctly validates proper admin credentials.
     * This test ensures that when the correct username "admin" and password "1234"
     * are provided, the authentication returns true, granting technician access.
     * 
     * Validates:
     * - Username matches stored admin username
     * - Password matches stored admin password
     * - Authentication succeeds with valid credentials
     * 
     * Expected Result: assertTrue - Authentication succeeds with correct credentials
     */
    @Test
    public void testAuthenticateWithCorrectCredentials() {
        assertTrue("Authentication should succeed with correct credentials", 
                   authService.authenticate("admin", "1234"));
    }
}
