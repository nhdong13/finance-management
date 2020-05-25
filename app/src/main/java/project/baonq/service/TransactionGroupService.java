package project.baonq.service;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

import project.baonq.dao.TransactionGroupDAO;
import project.baonq.enumeration.TransactionGroupStatus;
import project.baonq.model.TransactionGroup;
import project.baonq.model.TransactionGroupDao;

public class TransactionGroupService extends Service {

    private TransactionGroupDAO groupDAO;

    public TransactionGroupService(Application application) {
        super(application);
        groupDAO = new TransactionGroupDAO(application);
    }

    public Long addTransactionGroup(Long ledgerId, String name, int transactionType) {
        TransactionGroup transactionGroup = new TransactionGroup();
        transactionGroup.setLedger_id(ledgerId);
        transactionGroup.setName(name);
        transactionGroup.setTransaction_type(transactionType);
        transactionGroup.setStatus(TransactionGroupStatus.ENABLE.getStatus());
        Long lastUpdate = System.currentTimeMillis();
        transactionGroup.setInsert_date(lastUpdate);
        transactionGroup.setLast_update(lastUpdate);
        return groupDAO.addTransactionGroup(transactionGroup);
    }

    public Long getTransactionGroupID(Long ledger_id, int transaction_type, String name) {
        return groupDAO.getTransactionGroupID(ledger_id, transaction_type, name);
    }

    public TransactionGroup getTransactionGroupByID(Long id) {
        return groupDAO.getTransactionGroupByID(id);
    }

    public Long getLastUpdateTime() {
        SharedPreferences sharedPreferences = application.getSharedPreferences("sync", Context.MODE_PRIVATE);
        return sharedPreferences.getLong(TransactionGroupSyncService.TRANC_GROUP_LASTUPDATE, Long.parseLong("0"));
    }

    public Long getLastUpdateTimeFromDb() {
        TransactionGroup group = groupDAO.findLastUpdateGroup();
        if (group != null) {
            return group.getLast_update();
        } else {
            return Long.parseLong("0");
        }
    }

    public void insertOrUpdate(List<TransactionGroup> groups) {
        groupDAO.insertOrUpdate(groups);
    }

    public List<TransactionGroup> getAll() {
        TransactionGroupDao transactionGroupDao = new TransactionGroupDAO(application)
                .getDaoSession().getTransactionGroupDao();
        return transactionGroupDao.loadAll();
    }

    public List<TransactionGroup> findExpenseGroupByLedgerId(Long id) {
        return groupDAO.findExpenseGroupByLedgerId(id);
    }

    public List<TransactionGroup> findIncomeGroupByLedgerId(Long id) {
        return groupDAO.findIncomeGroupByLedgerId(id);
    }

    public TransactionGroup findById(Long id) {
        return groupDAO.findById(id);
    }

    public TransactionGroup findByServerId(Long id) {
        return groupDAO.findByServerId(id);
    }
}
