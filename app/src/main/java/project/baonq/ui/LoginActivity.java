package project.baonq.ui;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import project.baonq.menu.R;
import project.baonq.service.App;
import project.baonq.service.AuthenticationService;
import project.baonq.service.BaseAuthService;

public class LoginActivity extends AppCompatActivity {
    TextView txtError;
    TextView txtRegister;
    AuthenticationService authenticationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //check if user is logged in
        authenticationService = new AuthenticationService(this);
        if (authenticationService.isLoggedIn()) {
            finish();
        }
        System.out.println("Original JWT: " + authenticationService.getJwt());
        //if user has not been logged in
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        txtError = findViewById(R.id.txtLoginError);
        setErrorMessage("");
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        txtRegister = findViewById(R.id.txtViewRegister);
        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setErrorMessage(String message) {
        txtError.setText(message);
    }

    public void clickToLogin(View view) {
        if (((App) getApplication()).isNetworkConnected()) {
            String username = ((TextView) findViewById(R.id.txtLoginUsername)).getText().toString();
            String password = ((TextView) findViewById(R.id.txtLoginPassword)).getText().toString();
            try {
                String jwt = authenticationService.login(username, password);
                if (authenticationService.isLoggedIn()) {
                    new Thread(() -> {
                        try {
                            authenticationService.getUserInfoFromServer();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                    finish();
                } else {
                    setErrorMessage("Wrong username and password");
                }
            } catch (Exception e) {
                e.printStackTrace();
                setErrorMessage(e.getMessage());
            }
        } else {
            Toast.makeText(this, "Network is not available!", Toast.LENGTH_SHORT).show();
        }
    }
}
