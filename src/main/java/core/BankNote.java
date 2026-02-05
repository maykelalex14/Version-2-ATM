package core;

/**
 * ============== BANK NOTE MODEL ==============
 * BankNote represents a physical currency denomination in the ATM
 * Tracks available cash for each denomination to enable exact change dispensing
 * 
 * USED FOR:
 * - Allowing customers to select specific bank note combinations during withdrawal
 * - Tracking ATM inventory of each denomination
 * - Recording which notes were dispensed in transaction history
 * - Ensuring the ATM can dispense exact amounts efficiently
 */
public class BankNote {
    // ============== ATTRIBUTES ==============
    // denomination: The face value of the bank note in dollars (e.g., 5, 10, 20, 50, 100)
    private int denomination;
    
    // quantity: Number of notes of this denomination available in the ATM
    private int quantity;

    // ============== CONSTRUCTOR ==============
    /**
     * Constructor to create a BankNote with denomination and quantity
     * @param denomination - The face value of the note (e.g., 5, 10, 20, 50, 100)
     * @param quantity - Number of notes of this denomination available
     */
    public BankNote(int denomination, int quantity) {
        this.denomination = denomination;
        this.quantity = quantity;
    }

    // ============== GETTERS ==============
    /**
     * Get the denomination (face value) of this bank note
     * @return The denomination value in dollars
     */
    public int getDenomination() {
        return denomination;
    }

    /**
     * Get the quantity of notes available for this denomination
     * @return Number of notes available
     */
    public int getQuantity() {
        return quantity;
    }

    // ============== SETTERS ==============
    /**
     * Set the quantity of notes available for this denomination
     * @param quantity - New quantity value
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Decrease the quantity when notes are dispensed
     * @param count - Number of notes to dispense
     * @return true if dispense was successful, false if not enough notes available
     */
    public boolean dispense(int count) {
        if (count > quantity) {
            return false;
        }
        quantity -= count;
        return true;
    }

    /**
     * Add notes to the ATM (during technician refill)
     * @param count - Number of notes to add
     */
    public void refill(int count) {
        quantity += count;
    }

    /**
     * Calculate total value of all notes of this denomination
     * @return Total value in dollars
     */
    public double getTotalValue() {
        return denomination * quantity;
    }

    // ============== DISPLAY METHOD ==============
    @Override
    public String toString() {
        return String.format("$%-3d (%2d notes) = $%6.2f", denomination, quantity, getTotalValue());
    }
}
