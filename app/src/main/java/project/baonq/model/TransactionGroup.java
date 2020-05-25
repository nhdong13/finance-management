package project.baonq.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.List;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity(nameInDb = "transaction_group")
public class TransactionGroup {
    @Id(autoincrement = true)
    @JsonProperty("local_id")
    private Long id;
    @JsonProperty("server_id")
    private Long server_id;

    @JsonIgnore
    private Long ledger_id;
    
    @ToOne(joinProperty = "ledger_id")
    private Ledger ledger;
    @JsonIgnore
    private int parent_group_id;

    private String name;
    @JsonProperty("transactionType")
    private int transaction_type;
    @JsonProperty("insertDate")
    private Long insert_date;
    @JsonProperty("lastUpdate")
    private Long last_update;

    private int status;
    @JsonIgnore
    private Long group_id;
    @JsonIgnore
    @ToMany(referencedJoinProperty = "group_id")
    private List<Transaction> transaction;


    public Ledger ledgerWithoutContext(){
        return ledger;
    }
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1407111705)
    private transient TransactionGroupDao myDao;

    @Generated(hash = 2038902191)
    public TransactionGroup(Long id, Long server_id, Long ledger_id,
                            int parent_group_id, String name, int transaction_type,
                            Long insert_date, Long last_update, int status, Long group_id) {
        this.id = id;
        this.server_id = server_id;
        this.ledger_id = ledger_id;
        this.parent_group_id = parent_group_id;
        this.name = name;
        this.transaction_type = transaction_type;
        this.insert_date = insert_date;
        this.last_update = last_update;
        this.status = status;
        this.group_id = group_id;
    }

    @Generated(hash = 1038074587)
    public TransactionGroup() {
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

    public Long getLedger_id() {
        return this.ledger_id;
    }

    public void setLedger_id(Long ledger_id) {
        this.ledger_id = ledger_id;
    }

    public int getParent_group_id() {
        return this.parent_group_id;
    }

    public void setParent_group_id(int parent_group_id) {
        this.parent_group_id = parent_group_id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTransaction_type() {
        return this.transaction_type;
    }

    public void setTransaction_type(int transaction_type) {
        this.transaction_type = transaction_type;
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

    public Long getGroup_id() {
        return this.group_id;
    }

    public void setGroup_id(Long group_id) {
        this.group_id = group_id;
    }

    @Generated(hash = 811996760)
    private transient Long ledger__resolvedKey;

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 795162030)
    public Ledger getLedger() {
        Long __key = this.ledger_id;
        if (ledger__resolvedKey == null || !ledger__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            LedgerDao targetDao = daoSession.getLedgerDao();
            Ledger ledgerNew = targetDao.load(__key);
            synchronized (this) {
                ledger = ledgerNew;
                ledger__resolvedKey = __key;
            }
        }
        return ledger;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 40980851)
    public void setLedger(Ledger ledger) {
        synchronized (this) {
            this.ledger = ledger;
            ledger_id = ledger == null ? null : ledger.getId();
            ledger__resolvedKey = ledger_id;
        }
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1047815563)
    public List<Transaction> getTransaction() {
        if (transaction == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TransactionDao targetDao = daoSession.getTransactionDao();
            List<Transaction> transactionNew = targetDao
                    ._queryTransactionGroup_Transaction(id);
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
    @Generated(hash = 1149813621)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTransactionGroupDao() : null;
    }


}
