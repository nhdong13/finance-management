package project.baonq.util;

import java.util.List;
import java.util.Map;

public interface SyncAction {

    Object getSyncData();

    void beforeSynchronize();

    void afterSynchronize();

    Object synchronize();

    void doAction();

}
