package project.baonq.enumeration;

public enum NotificationStatus {
    DISABLE(0), ENABLE(1);
    private int status;

    NotificationStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
