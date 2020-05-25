package project.baonq.service;

import android.app.Application;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import project.baonq.dao.NotificationDAO;
import project.baonq.menu.R;
import project.baonq.model.Notification;
import project.baonq.util.SyncActionImpl;

import static project.baonq.service.BaseAuthService.buildBasicConnection;
import static project.baonq.service.BaseAuthService.read;

public class NotificationService extends Service implements Runnable {
    public String getNotificationUrl;
    public String getNotificationLastUpdateUrl;
    public String checkreadNotificationUrl;
    private NotificationDAO notificationDAO;
    private List<Consumer> toDoIfHasNewNotification = new LinkedList<>();

    public NotificationService(Application application) {
        super(application);
        notificationDAO = new NotificationDAO(application);
        Resources resources = application.getBaseContext().getResources();
        getNotificationUrl = resources.getString(R.string.server_name)
                + resources.getString(R.string.get_notification_url);
        getNotificationLastUpdateUrl = resources.getString(R.string.server_name)
                + resources.getString(R.string.get_notification_lastUpdate_url);
        checkreadNotificationUrl = resources.getString(R.string.server_name)
                + resources.getString(R.string.check_read_notification_url);
    }

    public void addNewNotificationConsumer(Consumer consumer) {
        toDoIfHasNewNotification.add(consumer);
    }

    public Long getServerLastUpdate() throws Exception {
        Long result;
        URL url = new URL(getNotificationLastUpdateUrl);
        HttpURLConnection conn = buildBasicConnection(url, true);
        BufferedReader in = null;
        ObjectMapper om = new ObjectMapper();
        try {
            //read response value
            if (conn.getResponseCode() == 200) {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String tmp = read(in);
                result = om.readValue(tmp, Long.class);
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
    }

    public void checkNotificationRead(List<Long> ids) {
        List<Notification> notifications = notificationDAO.findByIds(ids);
        long currentTime = System.currentTimeMillis();
        //filter read notifications
        notifications = notifications.stream()
                .filter(notification -> {
                    if (!notification.getIs_readed()) {
                        notification.setIs_readed(true);
                        notification.setLast_update(currentTime);
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
        notificationDAO.insertOrUpdate(notifications);
    }

    public void checkAllNotificationRead() {
        List<Long> unreadNotificationIds = getUnreadNotifications().stream()
                .map(notification -> notification.getId()).collect(Collectors.toList());
        checkNotificationRead(unreadNotificationIds);
    }

    public List<Notification> getUnreadNotifications() {
        return notificationDAO.findUnreadNotifications();
    }

    public List<Notification> findAll() {
        return notificationDAO.findAll();
    }

    public Long getLastUpdateTime() {
        Long result = notificationDAO.findLastUpdateTime();
        if (result == null) {
            result = Long.valueOf(0);
        }
        return result;
    }

    public List<Notification> getUpdatableRecords(Long lastUpdate) {
        return notificationDAO.findByLastUpdate(lastUpdate);
    }

    private boolean hasNew = false;

    public void syncWithLocal(List<Notification> syncData) {
        //get server id
        List<Long> serverIds = syncData.stream()
                .filter(notification -> notification.getServer_id() != null)
                .map(notification -> notification.getServer_id()).collect(Collectors.toList());
        //get local data
        List<Notification> localData = notificationDAO.findByServerIds(serverIds);
        Map<Long, Notification> map = new HashMap<>();
        //add sync data to map
        syncData.forEach(notification -> map.put(notification.getServer_id(), notification));
        //add local id to sync data
        localData.forEach(originalNotification -> {
            Notification tmp = map.get(originalNotification.getServer_id());
            tmp.setId(originalNotification.getId());
        });
        if (localData.size() < syncData.size()) {
            hasNew = true;
        }
    }

    @Override
    public void run() {
        FetchNotificationAction fetchAction = new FetchNotificationAction(this);
        CheckReadNotificationAction checkReadAction = new CheckReadNotificationAction(this);
        final App app = (App) application;
        while (true) {
            if (app.isDatabaseExist()) {
                try {
                    if (app.isNetworkConnected()) {
                        try {
                            fetchAction.doAction();
                            checkReadAction.doAction();
                        } finally {
                            Thread.sleep(5000);
                        }
                    } else {
                        Thread.sleep(5000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
    }

    public void insertOrUpdate(List<Notification> notifications) {
        new NotificationDAO(application).insertOrUpdate(notifications);
    }

    public static class FetchNotificationAction extends SyncActionImpl {
        NotificationService notificationService;

        public FetchNotificationAction(NotificationService notificationService) {
            this.notificationService = notificationService;
        }

        @Override
        public void beforeSynchronize() {
        }

        @Override
        public void afterSynchronize() {
            Notification[] syncData = (Notification[]) getSyncData();
            List<Notification> syncDataList = Arrays.asList(syncData);
            if (!syncDataList.isEmpty()) {
                notificationService.syncWithLocal(syncDataList);
                notificationService.insertOrUpdate(syncDataList);
                if (notificationService.hasNew) {
                    //do task if has new notification
                    notificationService
                            .toDoIfHasNewNotification.forEach(consumer -> consumer.accept(null));
                    notificationService.hasNew = false;
                }
            }
        }

        @Override
        public Object synchronize() {
            System.out.println("SENDING REQUEST TO URL:" + notificationService.getNotificationUrl + ", method:GET");
            Long lastUpdate = notificationService.getLastUpdateTime();
            Notification[] result = new Notification[]{};
            URL url = null;
            try {
                url = new URL(notificationService.getNotificationUrl + "?lastUpdate=" + lastUpdate);
                HttpURLConnection conn = buildBasicConnection(url, true);
                BufferedReader in = null;
                ObjectMapper om = new ObjectMapper();
                try {
                    //read response value
                    if (conn.getResponseCode() == 200) {
                        in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String tmp = read(in);
                        result = om.readValue(tmp, new Notification[]{}.getClass());
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

    public static class CheckReadNotificationAction extends SyncActionImpl {

        private NotificationService notificationService;

        public CheckReadNotificationAction(NotificationService notificationService) {
            this.notificationService = notificationService;
        }

        @Override
        public void beforeSynchronize() {

        }

        @Override
        public void afterSynchronize() {

        }

        @Override
        public Object synchronize() {
            try {
                Long lastUpdate = notificationService.getServerLastUpdate();
                List<Notification> updatableRecords = notificationService.getUpdatableRecords(lastUpdate);
                if (updatableRecords != null && !updatableRecords.isEmpty()) {
                    System.out.println("SENDING REQUEST TO URL:" + notificationService.checkreadNotificationUrl + ", method:POST");
                    URL url = new URL(notificationService.checkreadNotificationUrl);
                    HttpURLConnection conn = buildBasicConnection(url, true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    BufferedReader in = null;
                    ObjectMapper om = new ObjectMapper();
                    try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");) {
                        //write data to request
                        wr.write(om.writeValueAsString(updatableRecords));
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
