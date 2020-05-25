package project.baonq.enumeration;

public enum LedgerStatus {
    DISABLE(0), ENABLE(1);
    private int status;

    LedgerStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
