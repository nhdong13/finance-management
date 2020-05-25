package project.baonq.dao;

import android.app.Application;

import org.greenrobot.greendao.database.Database;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import project.baonq.enumeration.NotificationStatus;
import project.baonq.model.DaoSession;
import project.baonq.model.Notification;
import project.baonq.model.NotificationDao;

import static project.baonq.model.NotificationDao.Properties.Last_update;
import static project.baonq.model.NotificationDao.Properties.Insert_date;
import static project.baonq.model.NotificationDao.Properties.Status;

import project.baonq.service.App;

public class NotificationDAO extends DAO {

    public NotificationDAO(Application application) {
        super(application);
    }

    public List<Notification> insertOrUpdate(List<Notification> notifications) {
        if (notifications != null && !notifications.isEmpty()) {
            DaoSession daoSession = getDaoSession();
            NotificationDao notificationDao;
            notificationDao = daoSession.getNotificationDao();
            for (Notification notification : notifications) {
                long id = notificationDao.insertOrReplace(notification);
                notification.setId(id);
            }
            return notifications;
        } else {
            return new LinkedList<>();
        }
    }


    public List<Notification> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new LinkedList<>();
        }
        DaoSession daoSession = getDaoSession();
        NotificationDao notificationDao = daoSession.getNotificationDao();
        List<Notification> rs = notificationDao
                .queryBuilder().where(NotificationDao.Properties.Id.in(ids)).build().list();
        return rs;
    }

    public List<Notification> findByServerIds(List<Long> serverIds) {
        if (serverIds == null || serverIds.isEmpty()) {
            return new LinkedList<>();
        }
        DaoSession daoSession = getDaoSession();
        NotificationDao notificationDao = daoSession.getNotificationDao();
        List<Notification> rs = notificationDao
                .queryBuilder().where(NotificationDao.Properties.Server_id.in(serverIds)).build().list();
        return rs;
    }

    public List<Notification> findAll() {
        List<Notification> rs = getDaoSession()
                .getNotificationDao().queryBuilder()
                .orderDesc(Insert_date).list();
        return rs;
    }

    public List<Notification> findUnreadNotifications() {
        List<Notification> rs = getDaoSession()
                .getNotificationDao().queryBuilder()
                .where(NotificationDao.Properties.Is_readed.eq(false)
                        , Status.eq(NotificationStatus.ENABLE.getStatus())).list();
        return rs;
    }

    public Long findLastUpdateTime() {
        Notification notification = getDaoSession()
                .getNotificationDao().queryBuilder()
                .orderDesc(Last_update)
                .limit(1).build().unique();
        if (notification != null) {
            return notification.getLast_update();
        }
        return null;
    }

    public List<Notification> findByLastUpdate(Long lastUpdate) {
        return getDaoSession()
                .getNotificationDao().queryBuilder()
                .where(Last_update.gt(lastUpdate)).list();
    }
}
