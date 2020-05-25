package project.baonq.AddTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import project.baonq.menu.R;

public class AddCategory extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        Button btn = (Button) findViewById(R.id.btnSelectWallet);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddCategory.this, ChooseLedger.class);
                startActivity(intent);
            }
        });


    }

    public void saveData() {
        SharedPreferences pre = getSharedPreferences("transaction_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();

        EditText tmp = (EditText) findViewById(R.id.editText);
        editor.putString("name", tmp.getText().toString());
        Switch sw = (Switch) findViewById(R.id.switch2);
        editor.putBoolean("sw", sw.isChecked());
        editor.commit();
    }

    public void loadData() {
        SharedPreferences pre = getSharedPreferences("transaction_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        ((EditText) findViewById(R.id.editText)).setText(pre.getString("name", ""));
        ((Switch) findViewById(R.id.switch2)).setChecked(pre.getBoolean("sw", false));

    }
}
