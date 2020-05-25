package project.baonq.service;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;

import project.baonq.model.DaoMaster;
import project.baonq.model.DaoSession;

public class App extends Application {

    public static final String DATABASE_NAME = "personal_finance.db";
    private ConnectivityManager connectivityManager;
    DaoMaster daoMaster;
    @Override
    public void onCreate() {
        super.onCreate();
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, DATABASE_NAME);
        Database db = helper.getWritableDb();
        daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        QueryBuilder.LOG_SQL = true;
    }

    public DaoSession getDaoSession() {
        return daoMaster.newSession();
    }

    public void removeDb() {
        getBaseContext().deleteDatabase(DATABASE_NAME);
    }

    public boolean isDatabaseExist() {
        File dbFile = getBaseContext().getDatabasePath(DATABASE_NAME);
        return dbFile.exists();
    }

    public boolean isNetworkConnected() {
        if (connectivityManager == null) {
            connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        return connectivityManager.getActiveNetworkInfo() != null;
    }
}
