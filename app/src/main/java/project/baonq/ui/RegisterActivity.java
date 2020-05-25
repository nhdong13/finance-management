package project.baonq.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import project.baonq.dto.UserDto;
import project.baonq.menu.R;
import project.baonq.service.App;
import project.baonq.service.AuthenticationService;

public class RegisterActivity extends AppCompatActivity {
    static CharsetEncoder asciiEncoder =
            Charset.forName("US-ASCII").newEncoder();
    AuthenticationService authenticationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();
        emptyError();
        authenticationService = new AuthenticationService(this);
    }

    public void clickToRegister(View view) {
        TextView txtUsername = findViewById(R.id.txtUsername);
        TextView txtPassword = findViewById(R.id.txtPassword);
        TextView txtFirstname = findViewById(R.id.txtFirstname);
        TextView txtLastname = findViewById(R.id.txtLastname);
        UserDto user = new UserDto();
        user.setUsername(txtUsername.getText().toString().trim());
        user.setPassword(txtPassword.getText().toString());
        user.setFirstName(txtFirstname.getText().toString().trim());
        user.setLastName(txtLastname.getText().toString().trim());
        System.out.println(user.getUsername());
        System.out.println(user.getPassword());
        if (validateInfo(user)) {
            if (((App) getApplication()).isNetworkConnected()) {
                try {
                    authenticationService.register(user);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    ((TextView) findViewById(R.id.txtUsernameError)).setText("This username is already existed!");
                }
            } else {
                Toast.makeText(this, "Network is not available!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    boolean validateInfo(UserDto user) {
        emptyError();
        boolean result = true;
        if ("".equals(user.getUsername()) || user.getUsername().contains(" ")) {
            result = result && false;
            ((TextView) findViewById(R.id.txtUsernameError)).setText("Username can not blank or contain spaces");
        } else if (!asciiEncoder.canEncode(user.getUsername())) {
            result = result && false;
            ((TextView) findViewById(R.id.txtUsernameError)).setText("Username only includes English characters");
        }
        if ("".equals(user.getPassword()) || user.getPassword().length() < 6) {
            result = result && false;
            ((TextView) findViewById(R.id.txtPasswordError))
                    .setText("Password can not blank and must has at least 6 chars");
        } else if (!asciiEncoder.canEncode(user.getPassword())) {
            result = result && false;
            ((TextView) findViewById(R.id.txtPasswordError)).setText("Password only includes English characters");
        }
        if ("".equals(user.getFirstName())) {
            result = result && false;
            ((TextView) findViewById(R.id.txtFirstnameError)).setText("Firstname can not blank");
        }
        if ("".equals(user.getLastName())) {
            result = result && false;
            ((TextView) findViewById(R.id.txtLastnameError)).setText("Lastname can not blank");
        }
        return result;
    }

    void emptyError() {
        ((TextView) findViewById(R.id.txtUsernameError)).setText("");
        ((TextView) findViewById(R.id.txtPasswordError)).setText("");
        ((TextView) findViewById(R.id.txtFirstnameError)).setText("");
        ((TextView) findViewById(R.id.txtLastnameError)).setText("");
    }
}
