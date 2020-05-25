package project.baonq.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import project.baonq.dto.TransactionDto;

@Entity(nameInDb = "transaction")
public class Transaction {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

    @Id(autoincrement = true)
    @JsonProperty("local_id")
    private Long id;
    @JsonProperty("server_id")
    private Long server_id;

    private Long ledger_id;
    @ToOne(joinProperty = "ledger_id")
    private Ledger ledger;

    private Long group_id;
    @ToOne(joinProperty = "group_id")
    private TransactionGroup transactionGroup;

    private double balance;

    private String tdate;

    private String note;

    private boolean counted_on_report;

    private Long insert_date;

    private Long last_update;

    private int status;

    public Ledger ledgerWithoutContext() {
        return ledger;
    }

    public TransactionGroup groupWithoutContext() {
        return transactionGroup;
    }

    public TransactionDto dto() {
        TransactionDto dto = new TransactionDto();
        dto.setId(id);
        dto.setServerId(server_id);
        dto.setLedger(getLedger());
        dto.setTransactionGroup(getTransactionGroup());
        dto.setBalance(balance);
        try {
            dto.setDate(sdf.parse(tdate));
        } catch (ParseException e) {
        }
        dto.setNote(note);
        dto.setCountedOnReport(counted_on_report);
        dto.setInsertDate(insert_date);
        dto.setLastUpdate(last_update);
        dto.setStatus(status);
        return dto;
    }

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 947191939)
    private transient TransactionDao myDao;

    @Generated(hash = 1860488464)
    public Transaction(Long id, Long server_id, Long ledger_id, Long group_id,
                       double balance, String tdate, String note, boolean counted_on_report,
                       Long insert_date, Long last_update, int status) {
        this.id = id;
        this.server_id = server_id;
        this.ledger_id = ledger_id;
        this.group_id = group_id;
        this.balance = balance;
        this.tdate = tdate;
        this.note = note;
        this.counted_on_report = counted_on_report;
        this.insert_date = insert_date;
        this.last_update = last_update;
        this.status = status;
    }

    @Generated(hash = 750986268)
    public Transaction() {
    }

    @Generated(hash = 811996760)
    private transient Long ledger__resolvedKey;

    @Generated(hash = 921422356)
    private transient Long transactionGroup__resolvedKey;

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

    public Long getGroup_id() {
        return this.group_id;
    }

    public void setGroup_id(Long group_id) {
        this.group_id = group_id;
    }

    public double getBalance() {
        return this.balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getTdate() {
        return this.tdate;
    }

    public void setTdate(String tdate) {
        this.tdate = tdate;
    }

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
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
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 1803766632)
    public TransactionGroup getTransactionGroup() {
        Long __key = this.group_id;
        if (transactionGroup__resolvedKey == null
                || !transactionGroup__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TransactionGroupDao targetDao = daoSession.getTransactionGroupDao();
            TransactionGroup transactionGroupNew = targetDao.load(__key);
            synchronized (this) {
                transactionGroup = transactionGroupNew;
                transactionGroup__resolvedKey = __key;
            }
        }
        return transactionGroup;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1987925296)
    public void setTransactionGroup(TransactionGroup transactionGroup) {
        synchronized (this) {
            this.transactionGroup = transactionGroup;
            group_id = transactionGroup == null ? null : transactionGroup.getId();
            transactionGroup__resolvedKey = group_id;
        }
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
    @Generated(hash = 511087935)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTransactionDao() : null;
    }


}
