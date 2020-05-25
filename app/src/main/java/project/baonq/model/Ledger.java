package project.baonq.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity(nameInDb = "ledger")
public class Ledger {
    @Id(autoincrement = true)
    @JsonProperty("local_id")
    private Long id;
    @JsonProperty("server_id")
    private Long server_id;

    private String name;

    private String currency;

    @JsonProperty("countedOnReport")
    private boolean counted_on_report;

    @JsonProperty("insertDate")
    private Long insert_date;

    @JsonProperty("lastUpdate")
    private Long last_update;

    private int status;

    @JsonIgnore
    @ToMany(referencedJoinProperty = "ledger_id")
    private List<Transaction> transaction;

    @JsonIgnore
    @ToMany(referencedJoinProperty = "ledger_id")
    private List<TransactionGroup> transactionGroup;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 330045250)
    private transient LedgerDao myDao;

    @Generated(hash = 1629661636)
    public Ledger(Long id, Long server_id, String name, String currency,
                  boolean counted_on_report, Long insert_date, Long last_update,
                  int status) {
        this.id = id;
        this.server_id = server_id;
        this.name = name;
        this.currency = currency;
        this.counted_on_report = counted_on_report;
        this.insert_date = insert_date;
        this.last_update = last_update;
        this.status = status;
    }

    @Generated(hash = 749187879)
    public Ledger() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServer_id() {
        return this.server_id;
    }

    public void setServer_id(Long server_id) {
        this.server_id = server_id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean getCounted_on_report() {
        return this.counted_on_report;
    }

    public void setCounted_on_report(boolean counted_on_report) {
        this.counted_on_report = counted_on_report;
    }

    public Long getInsert_date() {
        return this.insert_date;
    }

    public void setInsert_date(Long insert_date) {
        this.insert_date = insert_date;
    }

    public Long getLast_update() {
        return this.last_update;
    }

    public void setLast_update(Long last_update) {
        this.last_update = last_update;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 874397327)
    public List<Transaction> getTransaction() {
        if (transaction == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TransactionDao targetDao = daoSession.getTransactionDao();
            List<Transaction> transactionNew = targetDao
                    ._queryLedger_Transaction(id);
            synchronized (this) {
                if (transaction == null) {
                    transaction = transactionNew;
                }
            }
        }
        return transaction;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 727791598)
    public synchronized void resetTransaction() {
        transaction = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 140434134)
    public List<TransactionGroup> getTransactionGroup() {
        if (transactionGroup == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TransactionGroupDao targetDao = daoSession.getTransactionGroupDao();
            List<TransactionGroup> transactionGroupNew = targetDao
                    ._queryLedger_TransactionGroup(id);
            synchronized (this) {
                if (transactionGroup == null) {
                    transactionGroup = transactionGroupNew;
                }
            }
        }
        return transactionGroup;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 1298992400)
    public synchronized void resetTransactionGroup() {
        transactionGroup = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 689260878)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getLedgerDao() : null;
    }


}