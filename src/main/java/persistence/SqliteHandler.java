package persistence;

import core.*;
import interfaces.Persistence;
import java.util.List;

/**
 * Legacy SQLite handler - kept for reference but not actively used
 * The system now uses JDBCHandler for all database operations
 */
public class SqliteHandler implements Persistence {

    @Override
    public List<Account> loadAccounts() {
        return null;
    }

    @Override
    public double loadATMCash() {
        return 0;
    }

    @Override
    public PaperTank loadPaperTank() {
        return null;
    }

    @Override
    public InkTank loadInkTank() {
        return null;
    }

    @Override
    public void saveATMState(List<Account> accounts, double atmCash, PaperTank paperTank, InkTank inkTank) {
    }

    @Override
    public List<BankNote> loadBankNotes() {
        return null;
    }

    @Override
    public void saveBankNotes(List<BankNote> bankNotes) {
    }

    public void closeConnection() {
        System.out.println("[INFO] Database connection closed");
    }
}
