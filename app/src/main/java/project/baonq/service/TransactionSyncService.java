package project.baonq.service;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import project.baonq.dao.TransactionDAO;
import project.baonq.dto.LedgerTransaction;
import project.baonq.dto.TransactionDto;
import project.baonq.menu.R;
import project.baonq.model.DaoSession;
import project.baonq.model.Ledger;
import project.baonq.model.Transaction;
import project.baonq.model.TransactionGroup;
import project.baonq.util.SyncActionImpl;

import static project.baonq.service.BaseAuthService.buildBasicConnection;
import static project.baonq.service.BaseAuthService.read;

public class TransactionSyncService extends TransactionService implements Runnable {
    public String transactionUrl;
    public static final String TRANC_LASTUPDATE = "tranc_lastUpdate";
    private TransactionDAO transactionDAO;
    LedgerService ledgerService;
    TransactionGroupService groupService;

    public TransactionSyncService(Application application) {
        super(application);
        transactionDAO = new TransactionDAO(application);
        ledgerService = new LedgerService(application);
        groupService = new TransactionGroupService(application);
        Resources resources = application.getBaseContext().getResources();
        transactionUrl = resources.getString(R.string.server_name)
                + resources.getString(R.string.get_create_update_tranc_url);
    }

    @Override
    public void run() {
        try {
            FetchTransactionAction fetchTransactionAction = new FetchTransactionAction(this);
            CreateNewTransactionAction createNewTransactionAction = new CreateNewTransactionAction(this);
            UpdateTransactionAction updateTransactionAction = new UpdateTransactionAction(this);
            //update first
            createNewTransactionAction.doAction();
            updateTransactionAction.doAction();
            //fetch after
            fetchTransactionAction.doAction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void syncWithLocal(List<Transaction> transactions) {
        //--SYNC LOCAL ID
        //get origin group from db
        List<Transaction> origin = transactionDAO.findByServerId(transactions.stream()
                .filter(transaction -> {
                    //sync ledger (for fetch)
                    if (transaction.ledgerWithoutContext() != null && transaction.getLedger_id() == null) {
                        Ledger ledger = ledgerService.findByServerId(transaction.ledgerWithoutContext().getServer_id());
                        if (ledger != null) {
                            transaction.setLedger(ledger);
                            transaction.setLedger_id(ledger.getId());
                        }
                    }
                    if (transaction.groupWithoutContext() != null && transaction.getGroup_id() == null) {
                        TransactionGroup group = groupService.findByServerId(transaction.groupWithoutContext().getServer_id());
                        if (group != null) {
                            transaction.setTransactionGroup(group);
                            transaction.setGroup_id(group.getId());
                        }
                    }
                    if (transaction.getServer_id() != null && transaction.getId() == null) {
                        return true;
                    }
                    return false;
                })
                .map(group -> group.getServer_id()).collect(Collectors.toList()));
        //update local id (for create)
        Map<Long, Transaction> tmp = new HashMap<>();
        transactions.forEach(group -> tmp.put(group.getServer_id(), group));
        origin.forEach(org -> {
            Transaction tmpTransaction = tmp.get(org.getServer_id());
            tmpTransaction.setId(org.getId());
        });
    }

    public List<Transaction> findCreatableTransactions() {
        return transactionDAO.findCreatableTransactions();
    }

    public List<Transaction> findUpdatableTransactions() {
        Long lastUpdate = getLastUpdateTime();
        return transactionDAO.findUpdatableTransactions(lastUpdate);
    }

    public void sync(List<Transaction> transactions) {
        sync(transactions, false);
    }

    public void sync(List<Transaction> transactions, boolean isFetch) {
        syncWithLocal(transactions);
        insertOrUpdate(transactions);
        //save last update time to preference
        if (isFetch) {
            Long lastUpdate = getLastUpdateTimeFromDb();
            SharedPreferences sharedPreferences = application
                    .getSharedPreferences("sync", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(TransactionSyncService.TRANC_LASTUPDATE, lastUpdate);
            editor.commit();
        }
    }

    public static class FetchTransactionAction extends SyncActionImpl {
        TransactionSyncService groupSyncService;

        public FetchTransactionAction(TransactionSyncService groupSyncService) {
            this.groupSyncService = groupSyncService;
        }

        @Override
        public void beforeSynchronize() {
        }

        @Override
        public void afterSynchronize() {
            TransactionDto[] syncData = (TransactionDto[]) getSyncData();
            List<Transaction> syncDataList = Arrays.asList(syncData).stream()
                    .map(transactionDto -> transactionDto.transaction())
                    .collect(Collectors.toList());
            groupSyncService.sync(syncDataList, true);
        }

        @Override
        public Object synchronize() {
            System.out.println("SENDING REQUEST TO URL:" + groupSyncService.transactionUrl + ", method:GET");
            Long lastUpdate = groupSyncService.getLastUpdateTime();
            TransactionDto[] result = new TransactionDto[]{};
            URL url = null;
            try {
                url = new URL(groupSyncService.transactionUrl + "?lastUpdate=" + lastUpdate);
                HttpURLConnection conn = buildBasicConnection(url, true);
                BufferedReader in = null;
                ObjectMapper om = new ObjectMapper();
                try {
                    //read response value
                    if (conn.getResponseCode() == 200) {
                        in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String tmp = read(in);
                        result = om.readValue(tmp, new TransactionDto[]{}.getClass());
                    } else {
                        in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                        throw new Exception(read(in));
                    }
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
                return result;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static List<LedgerTransaction> convertToTransactionView(List<Transaction> origin) {
        Map<Long, LedgerTransaction> rs = new HashMap<>();
        origin.forEach(transaction -> {
            LedgerTransaction lt = rs.get(transaction.getLedger().getServer_id());
            if (lt == null) {
                lt = new LedgerTransaction();
                lt.setServerId(transaction.getLedger().getServer_id());
                lt.setTransactionDtos(new LinkedList<>());
                rs.put(transaction.getLedger().getServer_id(), lt);
            }
            TransactionDto dto = transaction.dto();
            lt.getTransactionDtos().add(dto);
            dto.setLedger(null);
        });
        return rs.values().stream().collect(Collectors.toList());
    }

    public static class CreateNewTransactionAction extends SyncActionImpl {
        TransactionSyncService groupSyncService;

        public CreateNewTransactionAction(TransactionSyncService groupSyncService) {
            this.groupSyncService = groupSyncService;
        }

        @Override
        public void beforeSynchronize() {

        }

        @Override
        public void afterSynchronize() {
            TransactionDto[] syncData = (TransactionDto[]) getSyncData();
            List<Transaction> syncDataList = Arrays.asList(syncData).stream()
                    .map(dto -> dto.transaction()).collect(Collectors.toList());
            groupSyncService.sync(syncDataList);
        }

        @Override
        public Object synchronize() {
            TransactionDto[] result = new TransactionDto[]{};
            List<Transaction> creatableRecords = groupSyncService.findCreatableTransactions();
            try {
                if (creatableRecords != null && !creatableRecords.isEmpty()) {
                    System.out.println("SENDING REQUEST TO URL:" + groupSyncService.transactionUrl + ", method:POST");
                    URL url = new URL(groupSyncService.transactionUrl);
                    HttpURLConnection conn = buildBasicConnection(url, true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    BufferedReader in = null;
                    ObjectMapper om = new ObjectMapper();
                    try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");) {
                        //write to request body
                        String entity = om.writeValueAsString(convertToTransactionView(creatableRecords));
                        wr.write(entity);
                        wr.flush();
                        conn.connect();
                        //read response value
                        if (conn.getResponseCode() == 200) {
                            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            String tmp = read(in);
                            result = om.readValue(tmp, new TransactionDto[]{}.getClass());
                        } else {
                            in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                            throw new Exception(read(in));
                        }
                    } finally {
                        if (in != null) {
                            in.close();
                        }
                    }
                }
                return result;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class UpdateTransactionAction extends SyncActionImpl {
        TransactionSyncService groupSyncService;

        public UpdateTransactionAction(TransactionSyncService groupSyncService) {
            this.groupSyncService = groupSyncService;
        }

        @Override
        public void beforeSynchronize() {

        }

        @Override
        public void afterSynchronize() {
        }

        @Override
        public Object synchronize() {
            List<Transaction> updatableTransactions = groupSyncService.findUpdatableTransactions();
            try {
                if (updatableTransactions != null && !updatableTransactions.isEmpty()) {
                    System.out.println("SENDING REQUEST TO URL:" + groupSyncService.transactionUrl + ", method:PUT");
                    URL url = new URL(groupSyncService.transactionUrl);
                    HttpURLConnection conn = buildBasicConnection(url, true);
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    BufferedReader in = null;
                    ObjectMapper om = new ObjectMapper();
                    try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");) {
                        //write to request body
                        final DaoSession daoSession = ((App)groupSyncService.application).getDaoSession();
                        String entity = om.writeValueAsString(updatableTransactions.stream()
                                .map(transaction -> {
                                    transaction.__setDaoSession(daoSession);
                                    return transaction.dto();
                                }).collect(Collectors.toList()));
                        wr.write(entity);
                        wr.flush();
                        conn.connect();
                        //read response value
                        if (conn.getResponseCode() != 200) {
                            in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                            throw new Exception(read(in));
                        }
                    } finally {
                        if (in != null) {
                            in.close();
                        }
                    }
                }
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
