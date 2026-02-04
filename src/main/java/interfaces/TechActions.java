package interfaces;

/**
 * ============== INTERFACE SEGREGATION PRINCIPLE (ISP) ==============
 * This interface defines technician-specific actions, segregated from general persistence concerns.
 * Clients implementing this interface only see technician-related methods.
 * 
 * ============== SOLID PRINCIPLES APPLIED ==============
 * 1. INTERFACE SEGREGATION PRINCIPLE (ISP):
 *    - Defines a focused contract for technician actions (collectIncome, refillInventory, etc.)
 *    - Separated from Persistence interface to avoid "fat interfaces"
 *    - BankTechnician implements ONLY the methods it actually uses
 *    - Other services don't need to know about technician operations
 * 
 * 2. SINGLE RESPONSIBILITY PRINCIPLE (SRP):
 *    - This interface focuses ONLY on technician role responsibilities
 *    - Does NOT include account operations, persistence, or authentication
 *    - Defines a role/contract, not a utility class with unrelated methods
 * 
 * 3. LISKOV SUBSTITUTION PRINCIPLE (LSP):
 *    - Any implementation of TechActions can be substituted for another
 *    - Implementations must satisfy the contract defined here
 *    - Example: ATMTechnician or BankTechnician both implement the same interface
 *    - Main.java can swap implementations without changing behavior
 * 
 * FUTURE USE:
 * - V2 functionalities (marked below) can be implemented in subclasses
 * - Allows extensibility without modifying existing contracts
 */
public interface TechActions {
    // ============== CORE TECHNICIAN OPERATIONS ==============
    void displayMachineStatus();
    
    // ============== V2 EXTENDED FUNCTIONALITY ==============
    // These represent future capabilities that can be implemented:
    void collectIncome();       // V2: Collect fees/income from ATM
    void refillInventory();     // V2: Manage inventory beyond paper/ink
    void refillPaper();         // V2: Alternative paper management method
    void updateFirmware();      // V2: System firmware updates
}