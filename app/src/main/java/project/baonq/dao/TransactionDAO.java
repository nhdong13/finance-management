package project.baonq.dao;

import android.app.Application;
import android.database.Cursor;

import java.util.LinkedList;
import java.util.List;

import project.baonq.enumeration.TransactionStatus;
import project.baonq.model.DaoSession;
import project.baonq.model.Transaction;
import project.baonq.model.TransactionDao;
import project.baonq.model.TransactionGroup;
import project.baonq.model.TransactionGroupDao;

public class TransactionDAO extends DAO {


    public TransactionDAO(Application application) {
        super(application);
    }

    public Long addTransaction(Transaction transaction) {
        DaoSession daoSession = getDaoSession();
        TransactionDao transactionDao = daoSession.getTransactionDao();
        return transactionDao.insert(transaction);
    }


    public void updateTransaction(Transaction transaction) {
        DaoSession daoSession = getDaoSession();
        TransactionDao transactionDao = daoSession.getTransactionDao();
        transactionDao.update(transaction);
    }

    public List<Transaction> getByLedgerId(Long ledger_id) {
        DaoSession daoSession = getDaoSession();
        TransactionDao transactionDao = daoSession.getTransactionDao();
        return transactionDao.queryBuilder().where(TransactionDao.Properties.Ledger_id.eq(ledger_id)).list();
    }
    public List<Transaction> getActiveByLedgerId(Long ledger_id) {
        return getDaoSession().getTransactionDao().queryBuilder()
                .where(TransactionDao.Properties.Status.eq(TransactionStatus.ENABLE.getStatus()),
                        TransactionDao.Properties.Ledger_id.eq(ledger_id)).list();
    }

    public List<Transaction> getAll() {
        DaoSession daoSession = getDaoSession();
        TransactionDao transactionDao = daoSession.getTransactionDao();
        return transactionDao.loadAll();
    }

    public List<Transaction> getAllActive() {
        return getDaoSession().getTransactionDao().queryBuilder()
                .where(TransactionDao.Properties.Status.eq(TransactionStatus.ENABLE.getStatus())).list();
    }

    public Transaction getTransactionNeedForUpdate(Long ledger_id, Long group_id) {
        DaoSession daoSession = getDaoSession();
        TransactionDao transactionDao = daoSession.getTransactionDao();
        Transaction result = transactionDao.queryBuilder()
                .where(TransactionDao.Properties.Ledger_id.eq(ledger_id), TransactionDao.Properties.Group_id.eq(group_id))
                .unique();
        return result;
    }

    public Transaction findLastUpdateGroup() {
        return getDaoSession().getTransactionDao().queryBuilder()
                .orderDesc(TransactionDao.Properties.Last_update).limit(1).unique();
    }

    public Transaction findById(Long id) {
        DaoSession daoSession = getDaoSession();
        TransactionDao transactionDao = daoSession.getTransactionDao();
        Transaction result = transactionDao.queryBuilder().where(TransactionDao.Properties.Id.eq(id)).unique();
        return result;
    }

    public List<Transaction> insertOrUpdate(List<Transaction> groups) {
        if (groups != null && !groups.isEmpty()) {
            TransactionDao transactionDao = getDaoSession().getTransactionDao();
            for (Transaction transaction : groups) {
                long id = transactionDao.insertOrReplace(transaction);
                transaction.setId(id);
            }
            return groups;
        } else {
            return new LinkedList<>();
        }
    }

    public List<Transaction> findByServerId(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            return getDaoSession().getTransactionDao().queryBuilder()
                    .where(TransactionDao.Properties.Server_id.in(ids)).list();
        } else {
            return new LinkedList<>();
        }
    }

    public List<Transaction> findCreatableTransactions() {
        return getDaoSession().getTransactionDao().queryBuilder()
                .where(TransactionDao.Properties.Server_id.isNull()).list();
    }

    public List<Transaction> findUpdatableTransactions(Long lastUpdate) {
        return getDaoSession().getTransactionDao().queryBuilder()
                .where(TransactionDao.Properties.Server_id.isNotNull(),
                        TransactionDao.Properties.Last_update.gt(lastUpdate)).list();
    }
}
