package project.baonq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;

import java.text.SimpleDateFormat;
import java.util.Date;

import project.baonq.model.DaoSession;
import project.baonq.model.Ledger;
import project.baonq.model.LedgerDao;
import project.baonq.model.Transaction;
import project.baonq.model.TransactionDao;
import project.baonq.model.TransactionGroup;
import project.baonq.model.TransactionGroupDao;

public class TransactionDto {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

    @JsonProperty("local_id")
    private Long id;
    @JsonProperty("server_id")
    private Long serverId;

    private Ledger ledger;

    private TransactionGroup transactionGroup;

    private double balance;

    private Date date;

    private String note;

    private boolean countedOnReport;

    private Long insertDate;

    private Long lastUpdate;

    private int status;

    public Transaction transaction() {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setServer_id(serverId);
        transaction.setLedger(ledger);
        transaction.setTransactionGroup(transactionGroup);
        transaction.setBalance(balance);
        transaction.setTdate(sdf.format(date));
        transaction.setNote(note);
        transaction.setCounted_on_report(countedOnReport);
        transaction.setInsert_date(insertDate);
        transaction.setLast_update(lastUpdate);
        transaction.setStatus(status);
        return transaction;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public Ledger getLedger() {
        return ledger;
    }

    public void setLedger(Ledger ledger) {
        this.ledger = ledger;
    }

    public TransactionGroup getTransactionGroup() {
        return transactionGroup;
    }

    public void setTransactionGroup(TransactionGroup transactionGroup) {
        this.transactionGroup = transactionGroup;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isCountedOnReport() {
        return countedOnReport;
    }

    public void setCountedOnReport(boolean countedOnReport) {
        this.countedOnReport = countedOnReport;
    }

    public Long getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(Long insertDate) {
        this.insertDate = insertDate;
    }

    public Long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
