package project.baonq.model;

import java.util.Date;

public class UserSetting {
    private int userId;
    private int monthStartDate = 1;
    private String timeFormat = "dd/mm/yyyy";
    private Date lastUpdate;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMonthStartDate() {
        return monthStartDate;
    }

    public void setMonthStartDate(int monthStartDate) {
        this.monthStartDate = monthStartDate;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
