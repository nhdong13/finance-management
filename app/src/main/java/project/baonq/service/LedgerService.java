package project.baonq.service;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

import project.baonq.dao.LedgerDAO;
import project.baonq.enumeration.LedgerStatus;
import project.baonq.enumeration.TransactionGroupType;
import project.baonq.enumeration.TransactionStatus;
import project.baonq.model.DaoSession;
import project.baonq.model.Ledger;
import project.baonq.model.LedgerDao;
import project.baonq.model.Transaction;
import project.baonq.model.TransactionGroup;

public class LedgerService extends Service {

    private LedgerDAO ledgerDAO;

    public LedgerService(Application application) {
        super(application);
        ledgerDAO = new LedgerDAO(application);
    }


    public Ledger findById(Long id) {
        if (id != null && id != 0L) {
            return new LedgerDAO(application).findById(id);
        }
        return null;
    }

    public Long addLedger(String name, String currency, boolean isReport) {
        Ledger ledger = new Ledger();
        ledger.setName(name);
        ledger.setCurrency(currency);
        ledger.setCounted_on_report(isReport);
        ledger.setStatus(LedgerStatus.ENABLE.getStatus());
        long now = System.currentTimeMillis();
        ledger.setInsert_date(now);
        ledger.setLast_update(now);
        return ledgerDAO.addLedger(ledger);
    }

    public void updateLedger(Ledger ledger) {
        Ledger origin = findById(ledger.getId());
        origin.setStatus(ledger.getStatus());
        origin.setCurrency(ledger.getCurrency());
        origin.setName(ledger.getName());
        origin.setCounted_on_report(ledger.getCounted_on_report());
        origin.setLast_update(System.currentTimeMillis());
        ledgerDAO.updateLedger(origin);
    }

    public List<Ledger> getAll() {
        return ledgerDAO.getAll();
    }

    public Ledger findByServerId(Long id) {
        return ledgerDAO.findByServerId(id);
    }

    public Long getLastUpdateTime() {
        SharedPreferences sharedPreferences = application.getSharedPreferences("sync", Context.MODE_PRIVATE);
        return sharedPreferences.getLong(LedgerSyncService.LEDGER_LASTUPDATE, Long.parseLong("0"));
    }

    public Long getLastUpdateTimeFromDb() {
        Ledger ledger = ledgerDAO.findLastUpdateLedger();
        if (ledger != null) {
            return ledger.getLast_update();
        } else {
            return Long.parseLong("0");
        }
    }

    public void insertOrUpdate(List<Ledger> ledgers) {
        ledgerDAO.insertOrUpdate(ledgers);
    }

    public List<Ledger> loadAll() {
        return ((App) application).getDaoSession().getLedgerDao().loadAll();
    }

    public Ledger getLedgerById(Long id) {
        return new LedgerDAO(application).getledgerById(id);
    }

    public Double findSumOfLedgers() {
        List<Ledger> ledgerList = loadAll();
        TransactionService transactionService = new TransactionService(application);
        double total = 0;
        for (Ledger ledger : ledgerList) {
            List<Transaction> transactionList = transactionService.getByLedgerId(ledger.getId());
            double ledgerSum = 0;
            if (transactionList != null) {
                ledgerSum = sumOfTransaction(transactionList);
            }
            total += ledgerSum;
        }
        return total;
    }

    public Double findSumOfLedger(Long id) {
        if (id != null && id != Long.parseLong("0")) {
            Ledger ledger = findById(id);
            return findSumOfLedger(ledger);
        }
        return findSumOfLedgers();
    }

    public Double findSumOfLedger(Ledger ledger) {
        if (ledger != null) {
            return sumOfTransaction(ledger.getTransaction());
        }
        return Double.parseDouble("0");
    }

    public static double sumOfTransaction(List<Transaction> transactionList) {
        double sum = 0;
        for (Transaction item : transactionList) {
            if (item.getStatus() == TransactionStatus.ENABLE.getStatus()) {
                TransactionGroup transactionGroup = item.getTransactionGroup();
                int transactionGrouptype = transactionGroup.getTransaction_type();
                if (transactionGrouptype == TransactionGroupType.EXPENSE.getType()) {
                    sum -= item.getBalance();
                }
                if (transactionGrouptype == TransactionGroupType.INCOME.getType()) {
                    sum += item.getBalance();
                }
            }
        }
        return sum;
    }
}
