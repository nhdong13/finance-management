package project.baonq.service;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.widget.Toast;

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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import project.baonq.dao.LedgerDAO;
import project.baonq.menu.R;
import project.baonq.model.Ledger;
import project.baonq.util.SyncActionImpl;

import static project.baonq.service.BaseAuthService.buildBasicConnection;
import static project.baonq.service.BaseAuthService.read;

public class LedgerSyncService extends LedgerService implements Runnable {
    private LedgerDAO ledgerDAO;
    public String ledgerUrl;
    public static final String LEDGER_LASTUPDATE = "ledger_lastUpdate";
    private TransactionGroupSyncService transactionGroupSyncService;
    private TransactionSyncService transactionSyncService;
    List<Consumer> toDoAfterSync = new LinkedList<>();

    public LedgerSyncService(Application application) {
        super(application);
        ledgerDAO = new LedgerDAO(application);
        transactionGroupSyncService = new TransactionGroupSyncService(application);
        transactionSyncService = new TransactionSyncService(application);
        Resources resources = application.getBaseContext().getResources();
        ledgerUrl = resources.getString(R.string.server_name)
                + resources.getString(R.string.get_create_update_ledger_url);
    }

    public void addConsumer(Consumer consumer) {
        toDoAfterSync.add(consumer);
    }

    @Override
    public void run() {
        final App app = (App) application;
        if (app.isNetworkConnected()) {
            try {
                FetchLedgerAction fetchLedgerAction = new FetchLedgerAction(this);
                CreateNewLedgerAction createNewLedgerAction = new CreateNewLedgerAction(this);
                UpdateLedgerAction updateLedgerAction = new UpdateLedgerAction(this);
                //update first
                createNewLedgerAction.doAction();
                updateLedgerAction.doAction();
                //fetch after
                fetchLedgerAction.doAction();
                transactionGroupSyncService.run();
                transactionSyncService.run();
                toDoAfterSync.forEach(consumer -> consumer.accept(null));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void syncWithLocal(List<Ledger> ledgers) {
        LedgerDAO ledgerDAO = new LedgerDAO(application);
        //get origin ledger from db
        List<Ledger> origin = ledgerDAO.findByServerId(ledgers.stream()
                .filter(ledger -> ledger.getServer_id() != null && ledger.getId() == null)
                .map(ledger -> ledger.getServer_id()).collect(Collectors.toList()));
        //update local id (for fetch and update action)
        Map<Long, Ledger> tmp = new HashMap<>();
        ledgers.forEach(ledger -> tmp.put(ledger.getServer_id(), ledger));
        origin.forEach(org -> {
            Ledger tmpLedger = tmp.get(org.getServer_id());
            tmpLedger.setId(org.getId());
        });
    }

    public List<Ledger> findCreatableLedgers() {
        return ledgerDAO.findCreatableLedgers();
    }

    public List<Ledger> findUpdatableLedgers() {
        Long lastUpdate = getLastUpdateTime();
        return ledgerDAO.findUpdatableLedgers(lastUpdate);
    }

    public void sync(List<Ledger> ledgers) {
        sync(ledgers, false);
    }

    public void sync(List<Ledger> ledgers, boolean isFetch) {
        syncWithLocal(ledgers);
        insertOrUpdate(ledgers);
        //save last update time to preference
        if (isFetch) {
            Long lastUpdate = getLastUpdateTimeFromDb();
            SharedPreferences sharedPreferences = application
                    .getSharedPreferences("sync", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(LedgerSyncService.LEDGER_LASTUPDATE, lastUpdate);
            editor.commit();
        }
    }

    public static class FetchLedgerAction extends SyncActionImpl {
        LedgerSyncService ledgerSyncService;

        public FetchLedgerAction(LedgerSyncService ledgerSyncService) {
            this.ledgerSyncService = ledgerSyncService;
        }

        @Override
        public void beforeSynchronize() {
        }

        @Override
        public void afterSynchronize() {
            Ledger[] syncData = (Ledger[]) getSyncData();
            List<Ledger> syncDataList = Arrays.asList(syncData);
            ledgerSyncService.sync(syncDataList, true);
        }

        @Override
        public Object synchronize() {
            System.out.println("SENDING REQUEST TO URL:" + ledgerSyncService.ledgerUrl + ", method:GET");
            Long lastUpdate = ledgerSyncService.getLastUpdateTime();
            Ledger[] result = new Ledger[]{};
            URL url = null;
            try {
                url = new URL(ledgerSyncService.ledgerUrl + "?lastUpdate=" + lastUpdate);
                HttpURLConnection conn = buildBasicConnection(url, true);
                BufferedReader in = null;
                ObjectMapper om = new ObjectMapper();
                try {
                    //read response value
                    if (conn.getResponseCode() == 200) {
                        in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String tmp = read(in);
                        result = om.readValue(tmp, new Ledger[]{}.getClass());
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

    public static class CreateNewLedgerAction extends SyncActionImpl {
        LedgerSyncService ledgerSyncService;

        public CreateNewLedgerAction(LedgerSyncService ledgerSyncService) {
            this.ledgerSyncService = ledgerSyncService;
        }

        @Override
        public void beforeSynchronize() {

        }

        @Override
        public void afterSynchronize() {
            Ledger[] syncData = (Ledger[]) getSyncData();
            List<Ledger> syncDataList = Arrays.asList(syncData);
            ledgerSyncService.sync(syncDataList);
        }

        @Override
        public Object synchronize() {
            Ledger[] result = new Ledger[]{};
            List<Ledger> creatableRecords = ledgerSyncService.findCreatableLedgers();
            try {
                if (creatableRecords != null && !creatableRecords.isEmpty()) {
                    System.out.println("SENDING REQUEST TO URL:" + ledgerSyncService.ledgerUrl + ", method:POST");
                    URL url = new URL(ledgerSyncService.ledgerUrl);
                    HttpURLConnection conn = buildBasicConnection(url, true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    BufferedReader in = null;
                    ObjectMapper om = new ObjectMapper();
                    try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");) {
                        //write to request body
                        wr.write(om.writeValueAsString(creatableRecords));
                        wr.flush();
                        conn.connect();
                        //read response value
                        if (conn.getResponseCode() == 200) {
                            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            String tmp = read(in);
                            result = om.readValue(tmp, new Ledger[]{}.getClass());
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

    public static class UpdateLedgerAction extends SyncActionImpl {
        LedgerSyncService ledgerSyncService;

        public UpdateLedgerAction(LedgerSyncService ledgerSyncService) {
            this.ledgerSyncService = ledgerSyncService;
        }

        @Override
        public void beforeSynchronize() {

        }

        @Override
        public void afterSynchronize() {
        }

        @Override
        public Object synchronize() {
            List<Ledger> updatableLedgers = ledgerSyncService.findUpdatableLedgers();
            try {
                if (updatableLedgers != null && !updatableLedgers.isEmpty()) {
                    System.out.println("SENDING REQUEST TO URL:" + ledgerSyncService.ledgerUrl + ", method:PUT");
                    URL url = new URL(ledgerSyncService.ledgerUrl);
                    HttpURLConnection conn = buildBasicConnection(url, true);
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    BufferedReader in = null;
                    ObjectMapper om = new ObjectMapper();
                    try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");) {
                        //write to request body
                        String entity = om.writeValueAsString(updatableLedgers);
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
