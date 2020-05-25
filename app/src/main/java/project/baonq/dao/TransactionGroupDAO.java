package project.baonq.dao;

import android.app.Application;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.LinkedList;
import java.util.List;

import project.baonq.enumeration.TransactionGroupType;
import project.baonq.model.DaoSession;
import project.baonq.model.TransactionGroup;
import project.baonq.model.TransactionGroupDao;

public class TransactionGroupDAO extends DAO {

    public TransactionGroupDAO(Application application) {
        super(application);
    }

    public Long addTransactionGroup(TransactionGroup transactionGroup) {
        DaoSession daoSession = getDaoSession();
        TransactionGroupDao transactionGroupDao = daoSession.getTransactionGroupDao();
        return transactionGroupDao.insert(transactionGroup);
    }

    public Long getTransactionGroupID(Long ledger_id, int transaction_type, String name) {
        DaoSession daoSession = getDaoSession();
        TransactionGroupDao transactionGroupDao = daoSession.getTransactionGroupDao();
        QueryBuilder<TransactionGroup> queryBuilder = transactionGroupDao.queryBuilder();
        return queryBuilder.where(TransactionGroupDao.Properties.Ledger_id.eq(ledger_id),
                TransactionGroupDao.Properties.Transaction_type.eq(transaction_type),
                TransactionGroupDao.Properties.Name.eq(name))
                .unique()
                .getId();
    }

    public TransactionGroup getTransactionGroupByID(Long id) {
        DaoSession daoSession = getDaoSession();
        TransactionGroupDao transactionGroupDao = daoSession.getTransactionGroupDao();
        return transactionGroupDao.load(id);
    }

    public List<TransactionGroup> insertOrUpdate(List<TransactionGroup> groups) {
        if (groups != null && !groups.isEmpty()) {
            TransactionGroupDao groupDao = getDaoSession().getTransactionGroupDao();
            for (TransactionGroup group : groups) {
                long id = groupDao.insertOrReplace(group);
                group.setId(id);
            }
            return groups;
        } else {
            return new LinkedList<>();
        }
    }

    public List<TransactionGroup> findByServerId(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            return getDaoSession().getTransactionGroupDao().queryBuilder()
                    .where(TransactionGroupDao.Properties.Server_id.in(ids)).list();
        } else {
            return new LinkedList<>();
        }
    }

    public TransactionGroup findByServerId(Long id) {
            return getDaoSession().getTransactionGroupDao().queryBuilder()
                    .where(TransactionGroupDao.Properties.Server_id.eq(id)).unique();
    }

    public List<TransactionGroup> findByIds(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            return getDaoSession().getTransactionGroupDao().queryBuilder()
                    .where(TransactionGroupDao.Properties.Id.in(ids)).list();
        } else {
            return new LinkedList<>();
        }
    }

    public TransactionGroup findLastUpdateGroup() {
        return getDaoSession().getTransactionGroupDao().queryBuilder()
                .orderDesc(TransactionGroupDao.Properties.Last_update).limit(1).unique();
    }

    public List<TransactionGroup> findCreatableGroups() {
        return getDaoSession().getTransactionGroupDao().queryBuilder()
                .where(TransactionGroupDao.Properties.Server_id.isNull()).list();
    }

    public List<TransactionGroup> findUpdatableGroups(Long lastUpdate) {
        return getDaoSession().getTransactionGroupDao().queryBuilder()
                .where(TransactionGroupDao.Properties.Server_id.isNotNull(),
                        TransactionGroupDao.Properties.Last_update.gt(lastUpdate)).list();
    }

    public List<TransactionGroup> findExpenseGroupByLedgerId(Long id) {
        return getDaoSession().getTransactionGroupDao().queryBuilder()
                .where(TransactionGroupDao.Properties.Ledger_id.eq(id),
                        TransactionGroupDao.Properties.Transaction_type.eq(TransactionGroupType.EXPENSE.getType()))
                .list();
    }

    public List<TransactionGroup> findIncomeGroupByLedgerId(Long id) {
        return getDaoSession().getTransactionGroupDao().queryBuilder()
                .where(TransactionGroupDao.Properties.Ledger_id.eq(id),
                        TransactionGroupDao.Properties.Transaction_type.eq(TransactionGroupType.INCOME.getType()))
                .list();
    }

    public TransactionGroup findById(Long id) {
        return getDaoSession().getTransactionGroupDao().queryBuilder()
                .where(TransactionGroupDao.Properties.Id.eq(id)).unique();
    }
}
