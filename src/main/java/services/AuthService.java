package services;

/**
 * ============== CONTROLLER (MVC) ==============
 * AuthService acts as a Controller in the MVC architecture - responsible for:
 * - Authenticating technician/admin credentials
 * - Business logic for authentication verification
 * - Does NOT interact with View or Model directly
 * 
 * ============== SOLID PRINCIPLES APPLIED ==============
 * 1. SINGLE RESPONSIBILITY PRINCIPLE (SRP):
 *    - Has ONE reason to change: when authentication logic changes
 *    - Focused responsibility: verifying credentials
 *    - Does NOT handle user management, permissions, encryption, or persistence
 *    - Does NOT handle UI or session management
 * 
 * 2. ENCAPSULATION:
 *    - Private credentials (ADMIN_USER, ADMIN_PASS) prevent external access
 *    - Single public method (authenticate) provides controlled credential verification
 *    - Future: credentials could be moved to database with proper encryption
 * 
 * NOTE: This is a simple demonstration. In production:
 * - Credentials should be encrypted (BCrypt, Argon2, etc.)
 * - Should integrate with persistence layer (Persistence interface)
 * - Should support role-based access control (RBAC)
 * - Should implement multi-factor authentication
 */
public class AuthService {
    // ============== SRP: Only stores authentication-related data ==============
    private final String ADMIN_USER = "admin";
    private final String ADMIN_PASS = "1234";

    // ============== AUTHENTICATION BUSINESS LOGIC ==============
    // Controller method: validates provided credentials against known values
    // Returns boolean - lets caller decide how to handle result (separation of concerns)
    public boolean authenticate(String username, String password) {
        return ADMIN_USER.equals(username) && ADMIN_PASS.equals(password);
    }
}
