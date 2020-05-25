package project.baonq.dao;

import android.app.Application;

import project.baonq.model.DaoSession;
import project.baonq.service.App;

public class DAO {
    private Application application;

    public DAO(Application application) {
        this.application = application;
    }

    public DaoSession getDaoSession() {
        return ((App) application).getDaoSession();
    }
}
