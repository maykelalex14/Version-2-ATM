package interfaces;

import core.PaperTank;
import core.InkTank;
import core.Account;
import core.BankNote;
import java.util.List;

/**
 * ============== DEPENDENCY INVERSION PRINCIPLE (DIP) ==============
 * This interface represents the abstraction layer for data persistence.
 * High-level modules (ATMService, BankTechnician) depend on this Persistence
 * abstraction, NOT on concrete implementations (JDBCHandler, SqliteHandler).
 * 
 * ============== SOLID PRINCIPLES APPLIED ==============
 * 1. DEPENDENCY INVERSION PRINCIPLE (DIP):
 *    - ATMService depends on Persistence interface, not concrete JDBCHandler
 *    - BankTechnician depends on Persistence interface, not concrete implementations
 *    - Allows swapping implementations (JDBC, JPA, XML, REST API) without changing business logic
 * 
 * 2. INTERFACE SEGREGATION PRINCIPLE (ISP):
 *    - This interface is focused on ATM persistence concerns only
 *    - Does NOT include unrelated methods (user authentication, UI operations)
 *    - Clients using this interface only see persistence-related methods
 * 
 * 3. OPEN/CLOSED PRINCIPLE (OCP):
 *    - Open for extension: new Persistence implementations can be created without modifying this interface
 *    - Closed for modification: existing code using Persistence doesn't need changes
 *    - Multiple implementations (JDBCHandler, SqliteHandler) extend this contract
 * 
 * IMPLEMENTATION PATTERN:
 * - JDBCHandler implements Persistence using JDBC/SQLite
 * - SqliteHandler could be another implementation using pure SQLite
 * - Future implementations could use REST API, NoSQL, or cloud storage
 */
public interface Persistence {
    // ============== ATM State Persistence Methods ==============
    List<Account> loadAccounts();
    double loadATMCash();
    PaperTank loadPaperTank();
    InkTank loadInkTank();
    void saveATMState(List<Account> accounts, double atmCash, PaperTank paperTank, InkTank inkTank);
    
    // ============== Bank Notes Persistence Methods ==============
    List<BankNote> loadBankNotes();
    void saveBankNotes(List<BankNote> bankNotes);
}
