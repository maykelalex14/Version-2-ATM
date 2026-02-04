// Declare package: core package contains Model classes for the ATM application
package core;

// Import JPA (Java Persistence API) annotations for database persistence
// @Entity, @Table, @Id, @Column are Hibernate/JPA annotations for ORM (Object-Relational Mapping)
import javax.persistence.*;

/**
 * ============== MODEL (MVC) ==============
 * Account acts as a Model in the MVC architecture - responsible for:
 * - Representing the data structure of a bank account (account number, holder name, balance, PIN)
 * - Persisting data to database using Hibernate/JPA annotations (@Entity, @Column, @Table)
 * - Encapsulating business logic for account operations
 * 
 * ============== SOLID PRINCIPLES APPLIED ==============
 * 1. SINGLE RESPONSIBILITY PRINCIPLE (SRP):
 *    - This class has ONE reason to change: when account data structure or account operations logic changes
 *    - Focused responsibilities: storing account data and performing account-specific operations
 *    - Does NOT handle authentication, persistence, or UI concerns
 * 
 * 2. ENCAPSULATION (Part of SRP):
 *    - Private fields (accountNumber, accountHolder, balance, pin) prevent direct external modification
 *    - Business logic for withdraw/deposit validates constraints before modifying balance
 *    - Pin verification logic is encapsulated within this class
 */
// @Entity annotation: marks this class as a persistent entity (maps to database table)
@Entity
// @Table annotation: specifies the database table name where Account objects are stored
@Table(name = "accounts")
// Class declaration: Account represents a bank account record in the system
public class Account {
    // ============== DATABASE FIELDS ==============
    // accountNumber field - unique identifier for each account
    // @Id annotation: marks this field as the primary key (unique identifier in database)
    @Id
    // Private access: prevents direct modification from outside class; use setter instead
    private String accountNumber;
    
    // accountHolder field - stores the name of the account owner
    // @Column annotation: specifies database column properties (nullable=false means value is required)
    @Column(name = "account_holder", nullable = false)
    // Private access: encapsulates field to maintain data integrity
    private String accountHolder;
    
    // balance field - stores the account's current balance in dollars (double for decimal precision)
    // @Column annotation: maps to 'balance' column in database, requires non-null value
    @Column(name = "balance", nullable = false)
    // Private access: prevents external modification; use withdraw/deposit methods instead
    private double balance;
    
    // pin field - stores the 4-digit PIN for account authentication (stored as String)
    // @Column annotation: maps to 'pin' column in database, requires non-null value
    @Column(name = "pin", nullable = false)
    // Private access: sensitive data must be protected; only accessible through verifyPin() method
    private String pin;

    // ============== CONSTRUCTORS ==============
    // Default constructor - required by Hibernate/JPA for database object instantiation
    // Hibernate uses reflection to create Account objects from database rows
    public Account() {}

    // Parameterized constructor - allows creation of new Account objects with all required data
    // Parameters: accountNumber (unique id), accountHolder (name), balance (initial amount), pin (security code)
    public Account(String accountNumber, String accountHolder, double balance, String pin) {
        // Initialize accountNumber field with provided parameter value
        this.accountNumber = accountNumber;
        // Initialize accountHolder field with provided parameter value
        this.accountHolder = accountHolder;
        // Initialize balance field with provided parameter value
        this.balance = balance;
        // Initialize pin field with provided parameter value
        this.pin = pin;
    }

    // ============== GETTERS (Accessor Methods) ==============
    // getAccountNumber() - retrieve account number (read-only access to ID)
    // Returns: String accountNumber value
    public String getAccountNumber() { 
        // Return the account number
        return accountNumber; 
    }
    // setAccountNumber() - setter to modify account number (rarely used since ID is primary key)
    // Parameter: accountNumber - new account number to set
    public void setAccountNumber(String accountNumber) { 
        // Assign new value to accountNumber field
        this.accountNumber = accountNumber; 
    }
    
    // getAccountHolder() - retrieve account holder name
    // Returns: String accountHolder value (name of account owner)
    public String getAccountHolder() { 
        // Return the account holder name
        return accountHolder; 
    }
    // setAccountHolder() - setter to modify account holder name
    // Parameter: accountHolder - new account holder name to set
    public void setAccountHolder(String accountHolder) { 
        // Assign new value to accountHolder field
        this.accountHolder = accountHolder; 
    }
    
    // getBalance() - retrieve current account balance
    // Returns: double balance value (current money in account)
    public double getBalance() { 
        // Return current balance amount
        return balance; 
    }
    // setBalance() - setter to modify account balance
    // Parameter: balance - new balance amount to set
    // Note: Usually modified through deposit/withdraw, not directly via this setter
    public void setBalance(double balance) { 
        // Assign new value to balance field
        this.balance = balance; 
    }
    
    // getPin() - retrieve account PIN
    // Returns: String pin value (should not be exposed directly; use verifyPin() instead)
    public String getPin() { 
        // Return the PIN string
        return pin; 
    }
    // setPin() - setter to modify account PIN
    // Parameter: pin - new PIN to set
    public void setPin(String pin) { 
        // Assign new value to pin field
        this.pin = pin; 
    }
    
    // ============== SRP: PIN VERIFICATION ==============
    // verifyPin() method - validates entered PIN against stored PIN
    // This is a business logic method: only Account class knows how to verify PIN
    // Parameter: enteredPin - the PIN entered by user (e.g., from ATM keypad)
    // Returns: boolean - true if PIN matches stored PIN, false otherwise
    public boolean verifyPin(String enteredPin) {
        // Compare entered PIN with stored PIN using equals() method (string comparison)
        // Returns true if both PINs are identical, false if they differ
        return this.pin.equals(enteredPin);
    }
    
    // ============== SRP: DEPOSIT OPERATION ==============
    // deposit() method - adds money to account balance
    // This is a business logic method: validates amount before modifying state
    // Parameter: amount - the amount to deposit in dollars (must be positive)
    public void deposit(double amount) {
        // Check if deposit amount is positive (validate business rule: can't deposit negative/zero)
        if (amount > 0) {
            // Add deposit amount to current balance (increase balance by amount)
            this.balance += amount;
            // Note: negative/zero amounts are silently ignored (no exception thrown)
        }
    }
    
    // ============== SRP: WITHDRAWAL OPERATION ==============
    // withdraw() method - removes money from account balance
    // This is a business logic method: validates both amount > 0 AND sufficient funds
    // Parameter: amount - the amount to withdraw in dollars
    // Returns: boolean - true if withdrawal successful, false if failed (insufficient funds or invalid amount)
    public boolean withdraw(double amount) {
        // Check two conditions: (1) amount must be positive AND (2) balance must be >= amount
        // This validates the withdrawal is valid before modifying state
        if (amount > 0 && amount <= this.balance) {
            // Condition passed: amount is valid and funds are sufficient
            // Subtract withdrawal amount from current balance (decrease balance by amount)
            this.balance -= amount;
            // Return true to indicate successful withdrawal
            return true;
        }
        // Condition failed: either negative amount or insufficient funds
        // Return false to indicate withdrawal failed (Controller will display error message)
        return false;
    }
}
