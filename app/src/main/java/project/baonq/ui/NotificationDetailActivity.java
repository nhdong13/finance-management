package project.baonq.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import project.baonq.menu.R;

public class NotificationDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.NormalSizeAppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            TextView txtTitle = findViewById(R.id.txtTitleDetail);
            txtTitle.setText(bundle.getString("title"));
            TextView txtContent = findViewById(R.id.txtContentDetail);
            txtContent.setText(bundle.getString("content"));
        }
        initActionBar();
        initMenuActivities();
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View mCustomView = layoutInflater.inflate(R.layout.activity_notification_sub_layout, null);
        actionBar.setCustomView(mCustomView);
    }

    private void initMenuActivities() {
        TextView txtCancel = (TextView) findViewById(R.id.btnCloseNotification);
        txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView txtCheckAll = (TextView) findViewById(R.id.btnCheckAllNotification);
        txtCheckAll.setVisibility(View.INVISIBLE);
        TextView txtTitle = (TextView) findViewById(R.id.txtTittle);
        txtTitle.setVisibility(View.INVISIBLE);
    }
}
