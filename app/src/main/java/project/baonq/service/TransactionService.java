package project.baonq.service;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

import project.baonq.dao.TransactionDAO;
import project.baonq.enumeration.TransactionStatus;
import project.baonq.model.Transaction;
import project.baonq.model.TransactionDao;


public class TransactionService extends Service {

    private TransactionDAO transactionDAO;

    public TransactionService(Application application) {
        super(application);
        transactionDAO = new TransactionDAO(application);
    }

    public Long addTransaction(Long ledger_id, Long group_id, double balance, String tdate, String note) {
        Transaction transaction = new Transaction();
        transaction.setLedger_id(ledger_id);
        transaction.setGroup_id(group_id);
        transaction.setBalance(balance);
        transaction.setNote(note);
        transaction.setTdate(tdate);
        //insert date and last update time
        long insert_date = System.currentTimeMillis();
        transaction.setInsert_date(insert_date);
        transaction.setLast_update(insert_date);
        //status
        transaction.setStatus(TransactionStatus.ENABLE.getStatus());
        return transactionDAO.addTransaction(transaction);
    }

    public void updateTransaction(Transaction transaction) {
        Transaction origin = new TransactionDAO(application).findById(transaction.getId());
        origin.setGroup_id(transaction.getGroup_id());
        origin.setLedger_id(transaction.getLedger_id());
        origin.setBalance(transaction.getBalance());
        origin.setTdate(transaction.getTdate());
        origin.setNote(transaction.getNote());
//        origin.setCounted_on_report(transaction.getCounted_on_report());
        //insert date and last update time
        long lastUpdate = System.currentTimeMillis();
        origin.setLast_update(lastUpdate);
        transactionDAO.updateTransaction(origin);
    }

    public List<Transaction> getByLedgerId(Long ledger_id) {
        return transactionDAO.getByLedgerId(ledger_id);
    }

    public List<Transaction> getActiveByLedgerId(Long ledger_id) {
        return transactionDAO.getActiveByLedgerId(ledger_id);
    }


    public Long getLastUpdateTime() {
        SharedPreferences sharedPreferences = application.getSharedPreferences("sync", Context.MODE_PRIVATE);
        return sharedPreferences.getLong(TransactionSyncService.TRANC_LASTUPDATE, Long.parseLong("0"));
    }

    public Long getLastUpdateTimeFromDb() {
        Transaction transaction = transactionDAO.findLastUpdateGroup();
        if (transaction != null) {
            return transaction.getLast_update();
        } else {
            return Long.parseLong("0");
        }
    }

    public void insertOrUpdate(List<Transaction> groups) {
        transactionDAO.insertOrUpdate(groups);
    }

    private Transaction getTransactionNeedForUpdate(Long ledger_id, Long group_id) {
        TransactionDao transactionDao = ((App) application).getDaoSession().getTransactionDao();
        return transactionDao.queryBuilder()
                .where(TransactionDao.Properties.Ledger_id.eq(ledger_id), TransactionDao.Properties.Group_id.eq(group_id))
                .unique();
    }

    public List<Transaction> getAll() {
        return transactionDAO.getAll();
    }

    public List<Transaction> getAllActive() {
        return transactionDAO.getAllActive();
    }

    public Transaction getTransactionByID(Long id) {
        System.out.println(id);
        return null;
    }

    public boolean deleteTransaction(Long id) {
        Transaction transaction = transactionDAO.findById(id);
        if (transaction != null) {
            transaction.setStatus(TransactionStatus.DISABLE.getStatus());
            transaction.setLast_update(System.currentTimeMillis());
            transactionDAO.updateTransaction(transaction);
            return true;
        } else
            return false;
    }
}
