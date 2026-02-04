// Declare package: core package contains Model classes for the ATM application
package core;

/**
 * ============== MODEL (MVC) ==============
 * PaperTank acts as a Model in the MVC architecture - responsible for:
 * - Representing the data structure for paper inventory in the ATM printer
 * - Tracking paper sheet quantity and consumption
 * 
 * ============== SOLID PRINCIPLES APPLIED ==============
 * 1. SINGLE RESPONSIBILITY PRINCIPLE (SRP):
 *    - This class has ONE reason to change: when paper inventory logic changes
 *    - Focused responsibility: managing paper sheet quantity
 *    - Does NOT handle UI, persistence, or business orchestration
 * 
 * 2. ENCAPSULATION:
 *    - Private field (currentSheets) prevents direct external modification
 *    - Public methods (useSheet, refill) provide controlled access
 *    - Ensures paper count cannot become negative
 * 
 * 3. OPEN/CLOSED PRINCIPLE (OCP):
 *    - Open for extension: new methods can be added without modifying existing code
 *    - Closed for modification: internal state is protected
 */
// Class declaration: PaperTank manages paper inventory for the ATM printer
public class PaperTank {
    // ============== FIELD ==============
    // currentSheets field - tracks the quantity of paper sheets remaining (private to protect state)
    // Private access: external classes cannot directly modify; must use methods (useSheet, refill)
    private int currentSheets;

    // ============== CONSTRUCTOR ==============
    // PaperTank() constructor - initializes paper tank with starting quantity
    // Parameter: sheets - the initial quantity of paper sheets available
    public PaperTank(int sheets) {
        // Set currentSheets to the provided initial paper sheet amount
        this.currentSheets = sheets;
    }

    // ============== SRP: Paper availability check ==============
    // hasPaper() method - checks if paper is available (quantity > 0)
    // This is a business logic method: only PaperTank knows how to determine paper availability
    // Returns: boolean - true if paper available (currentSheets > 0), false if empty
    public boolean hasPaper() { 
        // Check if currentSheets is greater than zero
        // Returns true if paper available, false if paper depleted
        return currentSheets > 0; 
    }

    // ============== SRP: Paper consumption ==============
    // useSheet() method - consumes (removes) one paper sheet from the tank
    // This is a business logic method: validates that paper exists before consuming
    // Parameter: none - always uses exactly 1 sheet per call
    public void useSheet() {
        // Check if paper is available by calling hasPaper() method
        if (hasPaper()) {
            // Condition passed: at least one paper sheet available
            // Subtract 1 from currentSheets (deplete inventory by one sheet)
            currentSheets--;
        }
        // If condition fails: no paper available - method silently returns (no exception thrown)
        // Printer should have checked hasPaper() first before calling useSheet()
    }

    // ============== GETTER ==============
    // getCurrentSheets() method - accessor to get current paper sheet quantity
    // Returns: int - the current number of paper sheets remaining in tank
    public int getCurrentSheets() { 
        // Return the current paper sheet quantity (allows read-only access)
        return currentSheets; 
    }
    
    // ============== SRP: Paper refill ==============
    // refill() method - adds paper sheets to the tank (restocks inventory)
    // This is a business logic method: safely increases paper sheet count
    // Parameter: amount - the number of paper sheets to add to the tank
    public void refill(int amount) { 
        // Add amount to currentSheets (increase inventory by specified amount)
        // Note: no validation on amount (assumes technician provides valid amount)
        this.currentSheets += amount; 
    }
}