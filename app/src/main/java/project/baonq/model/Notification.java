package project.baonq.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

@Entity(nameInDb = "notification")
public class Notification {
    @Id(autoincrement = true)
    @JsonProperty("local_id")
    private Long id;
    @JsonProperty("server_id")
    private Long server_id;

    private String title;

    private String content;
    @JsonProperty("isSystemNotification")
    private boolean is_system_notification;
    @JsonProperty("insertDate")
    private Long insert_date;
    @JsonProperty("lastUpdate")
    private Long last_update;

    private int status;
    @JsonProperty("isReaded")
    private boolean is_readed;
    @Generated(hash = 1974057942)
    public Notification(Long id, Long server_id, String title, String content,
            boolean is_system_notification, Long insert_date, Long last_update,
            int status, boolean is_readed) {
        this.id = id;
        this.server_id = server_id;
        this.title = title;
        this.content = content;
        this.is_system_notification = is_system_notification;
        this.insert_date = insert_date;
        this.last_update = last_update;
        this.status = status;
        this.is_readed = is_readed;
    }
    @Generated(hash = 1855225820)
    public Notification() {
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
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getContent() {
        return this.content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public boolean getIs_system_notification() {
        return this.is_system_notification;
    }
    public void setIs_system_notification(boolean is_system_notification) {
        this.is_system_notification = is_system_notification;
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
    public boolean getIs_readed() {
        return this.is_readed;
    }
    public void setIs_readed(boolean is_readed) {
        this.is_readed = is_readed;
    }


}
