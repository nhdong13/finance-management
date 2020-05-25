package project.baonq.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import project.baonq.menu.R;
import project.baonq.model.User;
import project.baonq.service.App;
import project.baonq.service.AuthenticationService;
import project.baonq.service.LedgerSyncService;

public class SettingFragment extends Fragment {
    AuthenticationService authenticationService;

    public SettingFragment() {
    }

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        authenticationService = new AuthenticationService(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //set logout button action
        View settingLayout = inflater.inflate(R.layout.setting_fragment_layout, container, false);
        Button btnLogout = settingLayout.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new LedgerSyncService(getActivity().getApplication())).start();
                clickToLogout();
                ((App) getActivity().getApplication()).removeDb();
                authenticationService.clearAllPreferenceData();
                ((MainActivity) getActivity()).restartApp();
            }
        });
        User user = authenticationService.getUser();
        if (user != null) {
            TextView txtSettingName = settingLayout.findViewById(R.id.txtSettingName);
            txtSettingName.setText(user.getLastName() + " " + user.getFirstName());
        }
        return settingLayout;
    }

    public void clickToLogout() {
        try {
            authenticationService.logout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
