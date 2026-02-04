// Declare package: core package contains Model classes for the ATM application
package core;

// Import JPA (Java Persistence API) annotations for database persistence
// These annotations map Java objects to database tables (Object-Relational Mapping)
import javax.persistence.*;

// ============== MODEL (MVC) ==============
// MachineState acts as a Model - represents the data structure for:
// - The ATM machine's state (cash available, paper sheets, ink units)
// - Persisted to database using Hibernate/JPA annotations
// - Tracks physical resources in the ATM machine

// @Entity annotation: marks this class as a persistent entity (maps to database table)
@Entity
// @Table annotation: specifies the database table name where MachineState objects are stored
@Table(name = "machine_state")
// Class declaration: MachineState represents ATM hardware state information
public class MachineState {
    // ============== DATABASE FIELDS ==============
    // id field - unique identifier for machine state record
    // @Id annotation: marks this field as the primary key (unique identifier in database)
    @Id
    // @GeneratedValue annotation: automatically generates ID value when new record inserted
    // GenerationType.IDENTITY: uses database auto-increment feature for ID generation
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Private access: encapsulates ID to prevent external modification
    private Long id;
    
    // atmCash field - represents total cash available in the ATM machine (in dollars)
    // Stored as double for decimal precision (though currency typically uses 2 decimals)
    // @Column annotation: maps to 'atm_cash' column in database, nullable=false means required
    @Column(name = "atm_cash", nullable = false)
    // Private access: prevents direct modification from outside; use setter instead
    private double atmCash;
    
    // paperSheets field - represents number of paper sheets remaining in printer
    // Stored as int since paper quantity is always whole numbers (no fractions)
    // @Column annotation: maps to 'paper_sheets' column in database, nullable=false means required
    @Column(name = "paper_sheets", nullable = false)
    // Private access: encapsulates field to maintain data integrity
    private int paperSheets;
    
    // inkUnits field - represents number of ink units remaining in printer
    // Stored as int since ink units are discrete quantities
    // @Column annotation: maps to 'ink_units' column in database, nullable=false means required
    @Column(name = "ink_units", nullable = false)
    // Private access: encapsulates field to maintain data integrity
    private int inkUnits;

    // ============== CONSTRUCTORS ==============
    // Default constructor - required by Hibernate/JPA for database object instantiation
    // Hibernate uses reflection to create MachineState objects from database rows
    public MachineState() {}

    // Parameterized constructor - allows creation of new MachineState objects with all required data
    // Parameters: atmCash (total cash), paperSheets (paper inventory), inkUnits (ink inventory)
    public MachineState(double atmCash, int paperSheets, int inkUnits) {
        // Initialize atmCash field with provided parameter value
        this.atmCash = atmCash;
        // Initialize paperSheets field with provided parameter value
        this.paperSheets = paperSheets;
        // Initialize inkUnits field with provided parameter value
        this.inkUnits = inkUnits;
    }

    // ============== GETTERS AND SETTERS ==============
    
    // getId() - retrieve the unique ID for this machine state record
    // Returns: Long - the database-generated ID value
    public Long getId() { 
        // Return the ID value (read-only access to primary key)
        return id; 
    }
    // setId() - setter to modify the ID (rarely used since ID is auto-generated)
    // Parameter: id - new ID value to set
    public void setId(Long id) { 
        // Assign new value to id field
        this.id = id; 
    }

    // getAtmCash() - retrieve total cash available in the ATM
    // Returns: double - the amount of cash in dollars
    public double getAtmCash() { 
        // Return the ATM cash amount
        return atmCash; 
    }
    // setAtmCash() - setter to modify total ATM cash
    // Parameter: atmCash - new cash amount to set
    public void setAtmCash(double atmCash) { 
        // Assign new value to atmCash field
        this.atmCash = atmCash; 
    }

    // getPaperSheets() - retrieve quantity of paper sheets remaining
    // Returns: int - the number of paper sheets in the printer
    public int getPaperSheets() { 
        // Return the paper sheet quantity
        return paperSheets; 
    }
    // setPaperSheets() - setter to modify paper sheet quantity
    // Parameter: paperSheets - new paper sheet count to set
    public void setPaperSheets(int paperSheets) { 
        // Assign new value to paperSheets field
        this.paperSheets = paperSheets; 
    }

    // getInkUnits() - retrieve quantity of ink units remaining
    // Returns: int - the number of ink units in the printer
    public int getInkUnits() { 
        // Return the ink unit quantity
        return inkUnits; 
    }
    // setInkUnits() - setter to modify ink unit quantity
    // Parameter: inkUnits - new ink unit count to set
    public void setInkUnits(int inkUnits) { 
        // Assign new value to inkUnits field
        this.inkUnits = inkUnits; 
    }
}
