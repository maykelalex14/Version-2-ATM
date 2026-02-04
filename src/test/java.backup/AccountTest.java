import core.Account;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * AccountTest - Unit Test Class for Account Entity
 * 
 * This test class verifies the core functionality of the Account class,
 * specifically testing PIN verification and account operations.
 * 
 * Purpose: Ensure that Account objects correctly validate user PINs
 * and maintain account integrity.
 */
public class AccountTest {
    private Account account;

    /**
     * setUp() - Test Initialization Method
     * Runs before each test to create a fresh Account instance
     * 
     * Creates an account with:
     * - Account Number: ACC001
     * - Holder Name: John Doe
     * - Balance: $1000.00
     * - PIN: 1234
     */
    @Before
    public void setUp() {
        account = new Account("ACC001", "John Doe", 1000.00, "1234");
    }

    /**
     * testVerifyPinCorrect() - PIN Verification Test
     * 
     * Tests that the verifyPin() method correctly validates the PIN.
     * This test ensures that when the correct PIN "1234" is provided,
     * the verification returns true, allowing the user to access their account.
     * 
     * Expected Result: assertTrue - PIN verification succeeds with correct PIN
     */
    @Test
    public void testVerifyPinCorrect() {
        assertTrue("PIN verification should return true for correct PIN", account.verifyPin("1234"));
    }
}
