package project.baonq.util;

public abstract class SyncActionImpl implements SyncAction {

    private Object syncData;

    @Override
    public void doAction() {
        beforeSynchronize();
        syncData = synchronize();
        afterSynchronize();
    }

    @Override
    public final Object getSyncData() {
        return syncData;
    }
}
