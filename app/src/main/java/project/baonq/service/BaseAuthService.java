package project.baonq.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import project.baonq.dto.UserDto;
import project.baonq.menu.R;
import project.baonq.model.User;

public class BaseAuthService {

    private static String jwt = null;
    private static User user = null;
    private Context context;
    public String userUrl;
    ObjectMapper om = new ObjectMapper();
    public static final String AUTH_PREFERENCE = "auth";

    public BaseAuthService(Context context) {
        this.context = context;
        loadAuthenticationInfo();
        Resources resources = context.getResources();
        userUrl = resources.getString(R.string.server_name)
                + resources.getString(R.string.get_user_url);
    }

    public User getUserInfoFromServer() throws Exception {
        User user = null;
        URL url = new URL(userUrl);
        HttpURLConnection conn = buildBasicConnection(url, true);
        BufferedReader in = null;
        try {
            //read response value
            if (conn.getResponseCode() == 200) {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String tmp = read(in);
                user = om.readValue(tmp, User.class);
            } else {
                in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                throw new Exception(read(in));
            }
            this.user = user;
            saveUserInfoToPreference(user);
            return user;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public static HttpURLConnection buildBasicConnection(URL url) {
        return buildBasicConnection(url, false);
    }

    public static HttpURLConnection buildBasicConnection(URL url, boolean authenticated) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(false);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        if (authenticated) {
            conn.setRequestProperty("Authorization", jwt);
        }
        conn.setReadTimeout(60000);
        conn.setConnectTimeout(60000);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "utf-8");
        return conn;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public User getUser() {
        loadAuthenticationInfo();
        return user;
    }

    public static void setUser(User user) {
        BaseAuthService.user = user;
    }

    public static String read(BufferedReader br) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    protected void loadAuthenticationInfo() {
        String jwt = null;
        setJwt(null);
        setUser(null);
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(AUTH_PREFERENCE, Context.MODE_PRIVATE);
        jwt = sharedPreferences.getString("jwt", null);
        if (jwt != null && !"".equals(jwt)) {
            setJwt(jwt);
            if (!sharedPreferences.getString("username", "").equals("")) {
                User user = new User();
                user.setUsername(sharedPreferences.getString("username", ""));
                user.setFirstName(sharedPreferences.getString("firstname", ""));
                user.setLastName(sharedPreferences.getString("lastname", ""));
                user.setBirthday(new Date(sharedPreferences.getLong("birthday", Long.parseLong("0"))));
                user.setInsertDate(new Date(sharedPreferences.getLong("insertDate", Long.parseLong("0"))));
                user.setLastUpdate(new Date(sharedPreferences.getLong("lastUpdate", Long.parseLong("0"))));
                setUser(user);
            }
        }
    }

    protected void saveUserInfoToPreference(User user) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(AUTH_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (user != null) {
            editor.putString("username", user.getUsername());
            editor.putString("firstname", user.getFirstName());
            editor.putString("lastname", user.getLastName());
            editor.putLong("birthday", user.getBirthday() != null
                    ? user.getBirthday().getTime() : Long.parseLong("0"));
            editor.putLong("insertDate", user.getInsertDate().getTime());
            editor.putLong("lastUpdate", user.getLastUpdate().getTime());
        }
        editor.commit();
    }

    protected void saveJWT(String jwt) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AUTH_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if ("".equals(jwt) || jwt == null) {
            setJwt(null);
            editor.remove("jwt");
        } else {
            setJwt(jwt);
            editor.putString("jwt", jwt);
        }
        editor.commit();
    }

    public void clearAllPreferenceData() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AUTH_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("jwt");
        editor.remove("username");
        editor.remove("firstname");
        editor.remove("lastname");
        editor.remove("birthday");
        editor.remove("insertDate");
        editor.remove("lastUpdate");
        editor.commit();
        sharedPreferences = context.getSharedPreferences("sync", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.remove("tranc_group_lastUpdate");
        editor.remove("tranc_lastUpdate");
        editor.remove("ledger_lastUpdate");
        editor.commit();
    }

}