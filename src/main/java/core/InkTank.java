// Declare package: core package contains Model classes for the ATM application
package core;

/**
 * ============== MODEL (MVC) ==============
 * InkTank acts as a Model in the MVC architecture - responsible for:
 * - Representing the data structure for ink inventory in the ATM printer
 * - Tracking ink unit quantity and consumption
 * 
 * ============== SOLID PRINCIPLES APPLIED ==============
 * 1. SINGLE RESPONSIBILITY PRINCIPLE (SRP):
 *    - This class has ONE reason to change: when ink inventory logic changes
 *    - Focused responsibility: managing ink units
 *    - Does NOT handle UI, persistence, or business orchestration
 * 
 * 2. ENCAPSULATION:
 *    - Private field (currentInk) prevents direct external modification
 *    - Public methods (useInk, refill) provide controlled access
 *    - useInk() validates amount before consumption
 * 
 * 3. OPEN/CLOSED PRINCIPLE (OCP):
 *    - Open for extension: new methods can be added without modifying existing code
 *    - Closed for modification: internal state is protected
 * 
 * NOTE: InkTank and PaperTank follow the same pattern - demonstrating SRP consistency
 * across similar resource management models.
 */
// Class declaration: InkTank manages ink inventory for the ATM printer
public class InkTank {
    // ============== FIELD ==============
    // currentInk field - tracks the quantity of ink units remaining (private to protect state)
    // Private access: external classes cannot directly modify; must use methods (useInk, refill)
    private int currentInk;

    // ============== CONSTRUCTOR ==============
    // InkTank() constructor - initializes ink tank with starting quantity
    // Parameter: ink - the initial quantity of ink units available
    public InkTank(int ink) {
        // Set currentInk to the provided initial ink amount
        this.currentInk = ink;
    }

    // ============== SRP: Ink availability check ==============
    // hasInk() method - checks if ink is available (quantity > 0)
    // This is a business logic method: only InkTank knows how to determine ink availability
    // Returns: boolean - true if ink available (currentInk > 0), false if empty
    public boolean hasInk() { 
        // Check if currentInk is greater than zero
        // Returns true if ink available, false if ink depleted
        return currentInk > 0; 
    }

    // ============== SRP: Ink consumption ==============
    // useInk() method - consumes (removes) ink from the tank
    // This is a business logic method: validates that enough ink exists before consuming
    // Parameter: amount - the number of ink units to consume
    public void useInk(int amount) {
        // Check if currentInk quantity is sufficient (>= amount requested)
        if (currentInk >= amount) {
            // Condition passed: enough ink available to consume
            // Subtract amount from currentInk (deplete ink by requested amount)
            currentInk -= amount;
        }
        // If condition fails: not enough ink - method silently returns (no exception thrown)
        // Printer should have checked hasInk() first before calling useInk()
    }

    // ============== GETTER ==============
    // getCurrentInk() method - accessor to get current ink quantity
    // Returns: int - the current number of ink units remaining in tank
    public int getCurrentInk() { 
        // Return the current ink quantity (allows read-only access)
        return currentInk; 
    }
    
    // ============== SRP: Ink refill ==============
    // refill() method - adds ink to the tank (restocks inventory)
    // This is a business logic method: safely increases ink count
    // Parameter: amount - the number of ink units to add to the tank
    public void refill(int amount) { 
        // Add amount to currentInk (increase inventory by specified amount)
        // Note: no validation on amount (assumes technician provides valid amount)
        this.currentInk += amount; 
    }
}
