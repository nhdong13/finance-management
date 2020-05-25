package project.baonq.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import project.baonq.menu.R;
import project.baonq.model.Notification;
import project.baonq.service.App;
import project.baonq.service.LedgerService;
import project.baonq.service.NotificationService;
import project.baonq.util.SyncActionImpl;

import static project.baonq.service.NotificationService.FetchNotificationAction;

public class NotificationActivity extends AppCompatActivity {

    NotificationService notificationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.NormalSizeAppTheme);
        super.onCreate(savedInstanceState);
        notificationService = new NotificationService(getApplication());
        setContentView(R.layout.activity_notification);
        initActionBar();
        initMenuActivities();
        loadNotification();
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
        txtCheckAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAll();
            }
        });
    }

    private List<Notification> getNofitications() {
        List<Notification> rs = null;
        try {
            rs = notificationService.findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    private void loadNotification() {
        List<Notification> notifications = getNofitications();
        if (notifications != null) {
            notifications.forEach(this::createNewRowData);
        }
    }

    @SuppressLint("ResourceAsColor")
    private void createNewRowData(Notification notification) {
        LinearLayout notiView = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 5);
        notiView.setPadding(5, 5, 5, 5);
        notiView.setOrientation(LinearLayout.VERTICAL);
        notiView.setBackgroundColor(notification.getIs_readed() ? Color.parseColor("#CCCCCC") : Color.parseColor("#ffffff"));
        notiView.setLayoutParams(layoutParams);

        TextView title = new TextView(this);
        title.setText(notification.getTitle());
        title.setTextColor(Color.parseColor("#000000"));
        title.setTextSize(18);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setSingleLine(false);

        TextView content = new TextView(this);
        content.setText(notification.getContent());
        content.setTextColor(Color.parseColor("#000000"));
        content.setTextSize(15);
        content.setGravity(Gravity.CENTER);
        content.setTypeface(Typeface.DEFAULT);
        content.setSingleLine(false);
        content.setMaxLines(5);

        notiView.addView(title);
        notiView.addView(content);

        LinearLayout contentLedgeChosenLayout = (LinearLayout) findViewById(R.id.cardNotifications);
        contentLedgeChosenLayout.addView(notiView);
        notiView.setOnClickListener(v -> {
            //check view
            List<Long> tmp = new ArrayList<>();
            tmp.add(notification.getId());
            notificationService.checkNotificationRead(tmp);
            //show
            Intent intent = new Intent(this, NotificationDetailActivity.class);
            Bundle b = new Bundle();
            b.putString("title",notification.getTitle());
            b.putString("content",notification.getContent());
            intent.putExtras(b);
            startActivity(intent);
            notiView.setBackgroundColor(Color.parseColor("#CCCCCC"));
        });
    }

    private void checkAll() {
        LinearLayout cardNotifications = findViewById(R.id.cardNotifications);
        int quantity = cardNotifications.getChildCount();
        //ui
        for (int i = 0; i < quantity; i++) {
            LinearLayout childCard = (LinearLayout) cardNotifications.getChildAt(i);
            childCard.setBackgroundColor(Color.parseColor("#CCCCCC"));
        }
        //db
        notificationService.checkAllNotificationRead();
    }
}
