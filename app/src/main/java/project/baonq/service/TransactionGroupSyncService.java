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

import project.baonq.dao.TransactionGroupDAO;
import project.baonq.dto.LedgerTransactionGroup;
import project.baonq.menu.R;
import project.baonq.model.Ledger;
import project.baonq.model.TransactionGroup;
import project.baonq.util.SyncActionImpl;

import static project.baonq.service.BaseAuthService.buildBasicConnection;
import static project.baonq.service.BaseAuthService.read;

public class TransactionGroupSyncService extends TransactionGroupService implements Runnable {
    public String groupUrl;
    public static final String TRANC_GROUP_LASTUPDATE = "tranc_group_lastUpdate";
    private TransactionGroupDAO groupDAO;
    LedgerService ledgerService;

    public TransactionGroupSyncService(Application application) {
        super(application);
        groupDAO = new TransactionGroupDAO(application);
        ledgerService = new LedgerService(application);
        Resources resources = application.getBaseContext().getResources();
        groupUrl = resources.getString(R.string.server_name)
                + resources.getString(R.string.get_create_update_tranc_group_url);
    }

    @Override
    public void run() {
        try {
            FetchGroupAction fetchGroupAction = new FetchGroupAction(this);
            CreateNewGroupAction createNewGroupAction = new CreateNewGroupAction(this);
            UpdateGroupAction updateGroupAction = new UpdateGroupAction(this);
            //update first
            createNewGroupAction.doAction();
            updateGroupAction.doAction();
            //fetch after
            fetchGroupAction.doAction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void syncWithLocal(List<TransactionGroup> groups) {
        //--SYNC LOCAL ID
        //get origin group from db
        List<TransactionGroup> origin = groupDAO.findByServerId(groups.stream()
                .filter(group -> {
                    //sync ledger (for fetch)
                    if (group.ledgerWithoutContext() != null && group.getLedger_id() == null) {
                        Ledger ledger = ledgerService.findByServerId(group.ledgerWithoutContext().getServer_id());
                        if (ledger != null) {
                            group.setLedger(ledger);
                            group.setLedger_id(ledger.getId());
                        }
                    }
                    if (group.getServer_id() != null && group.getId() == null) {
                        return true;
                    }
                    return false;
                })
                .map(group -> group.getServer_id()).collect(Collectors.toList()));
        //update local id (for create)
        Map<Long, TransactionGroup> tmp = new HashMap<>();
        groups.forEach(group -> tmp.put(group.getServer_id(), group));
        origin.forEach(org -> {
            TransactionGroup tmpGroup = tmp.get(org.getServer_id());
            tmpGroup.setId(org.getId());
        });
    }

    public List<TransactionGroup> findCreatableGroups() {
        return groupDAO.findCreatableGroups();
    }

    public List<TransactionGroup> findUpdatableGroups() {
        Long lastUpdate = getLastUpdateTime();
        return groupDAO.findUpdatableGroups(lastUpdate);
    }

    public void sync(List<TransactionGroup> groups) {
        sync(groups, false);
    }

    public void sync(List<TransactionGroup> groups, boolean isFetch) {
        syncWithLocal(groups);
        insertOrUpdate(groups);
        //save last update time to preference
        if (isFetch) {
            Long lastUpdate = getLastUpdateTimeFromDb();
            SharedPreferences sharedPreferences = application
                    .getSharedPreferences("sync", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(TransactionGroupSyncService.TRANC_GROUP_LASTUPDATE, lastUpdate);
            editor.commit();
        }
    }

    public static class FetchGroupAction extends SyncActionImpl {
        TransactionGroupSyncService groupSyncService;

        public FetchGroupAction(TransactionGroupSyncService groupSyncService) {
            this.groupSyncService = groupSyncService;
        }

        @Override
        public void beforeSynchronize() {
        }

        @Override
        public void afterSynchronize() {
            TransactionGroup[] syncData = (TransactionGroup[]) getSyncData();
            List<TransactionGroup> syncDataList = Arrays.asList(syncData);
            groupSyncService.sync(syncDataList, true);
        }

        @Override
        public Object synchronize() {
            System.out.println("SENDING REQUEST TO URL:" + groupSyncService.groupUrl + ", method:GET");
            Long lastUpdate = groupSyncService.getLastUpdateTime();
            TransactionGroup[] result = new TransactionGroup[]{};
            URL url = null;
            try {
                url = new URL(groupSyncService.groupUrl + "?lastUpdate=" + lastUpdate);
                HttpURLConnection conn = buildBasicConnection(url, true);
                BufferedReader in = null;
                ObjectMapper om = new ObjectMapper();
                try {
                    //read response value
                    if (conn.getResponseCode() == 200) {
                        in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String tmp = read(in);
                        result = om.readValue(tmp, new TransactionGroup[]{}.getClass());
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

    public static List<LedgerTransactionGroup> convertToTransactionGroupView(List<TransactionGroup> origin) {
        Map<Long, LedgerTransactionGroup> rs = new HashMap<>();
        origin.forEach(group -> {
            LedgerTransactionGroup ltg = rs.get(group.getLedger().getServer_id());
            if (ltg == null) {
                ltg = new LedgerTransactionGroup();
                ltg.setServerId(group.getLedger().getServer_id());
                ltg.setTransactionGroups(new LinkedList<>());
                rs.put(group.getLedger().getServer_id(), ltg);
            }
            ltg.getTransactionGroups().add(group);
            group.setLedger(null);
        });
        return rs.values().stream().collect(Collectors.toList());
    }

    public static class CreateNewGroupAction extends SyncActionImpl {
        TransactionGroupSyncService groupSyncService;

        public CreateNewGroupAction(TransactionGroupSyncService groupSyncService) {
            this.groupSyncService = groupSyncService;
        }

        @Override
        public void beforeSynchronize() {

        }

        @Override
        public void afterSynchronize() {
            TransactionGroup[] syncData = (TransactionGroup[]) getSyncData();
            List<TransactionGroup> syncDataList = Arrays.asList(syncData);
            groupSyncService.sync(syncDataList);
        }

        @Override
        public Object synchronize() {
            TransactionGroup[] result = new TransactionGroup[]{};
            List<TransactionGroup> creatableRecords = groupSyncService.findCreatableGroups();
            try {
                if (creatableRecords != null && !creatableRecords.isEmpty()) {
                    System.out.println("SENDING REQUEST TO URL:" + groupSyncService.groupUrl + ", method:POST");
                    URL url = new URL(groupSyncService.groupUrl);
                    HttpURLConnection conn = buildBasicConnection(url, true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    BufferedReader in = null;
                    ObjectMapper om = new ObjectMapper();
                    try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");) {
                        //write to request body
                        String entity = om.writeValueAsString(convertToTransactionGroupView(creatableRecords));
                        wr.write(entity);
                        wr.flush();
                        conn.connect();
                        //read response value
                        if (conn.getResponseCode() == 200) {
                            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            String tmp = read(in);
                            result = om.readValue(tmp, new TransactionGroup[]{}.getClass());
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

    public static class UpdateGroupAction extends SyncActionImpl {
        TransactionGroupSyncService groupSyncService;

        public UpdateGroupAction(TransactionGroupSyncService groupSyncService) {
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
            List<TransactionGroup> updatableGroups = groupSyncService.findUpdatableGroups();
            try {
                if (updatableGroups != null && !updatableGroups.isEmpty()) {
                    System.out.println("SENDING REQUEST TO URL:" + groupSyncService.groupUrl + ", method:PUT");
                    URL url = new URL(groupSyncService.groupUrl);
                    HttpURLConnection conn = buildBasicConnection(url, true);
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    BufferedReader in = null;
                    ObjectMapper om = new ObjectMapper();
                    try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");) {
                        //write to request body
                        String entity = om.writeValueAsString(updatableGroups);
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
