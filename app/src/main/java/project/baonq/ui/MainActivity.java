package project.baonq.ui;


import android.app.Activity;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.savvi.rangedatepicker.CalendarPickerView;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import project.baonq.AddTransaction.AddTransaction;
import project.baonq.enumeration.Currency;
import project.baonq.menu.R;
import project.baonq.model.Ledger;
import project.baonq.service.App;
import project.baonq.service.AuthenticationService;
import project.baonq.service.LedgerSyncService;
import project.baonq.service.NotificationService;
import project.baonq.service.RecyclerItemClickListener;
import project.baonq.service.TransactionService;
import project.baonq.util.ConvertUtil;

import static project.baonq.util.ConvertUtil.convertCurrency;


public class MainActivity extends AppCompatActivity {
    public static final int TRANSACTION_ACTION = 10;
    public static final int RESET_TITLE = 10;
    CalendarPickerView calendar;
    public static Long startTime;
    public static Long endTime;
    public static Long ledger_id = null;
    public static String my_money = "0,00đ";
    public static String ledgerName;
    AuthenticationService authService;
    Thread notificationService;
    LedgerSyncService ledgerSyncService;
    public static final boolean GET_NOTIFICATION = false;
    private TransactionService transactionService;
    private double sumledgerMoney = 0;
    private View mCustomView;
    public static Activity activity;
    public Fragment currentFragment;
    BottomNavigationView bottomNavigationView;


    /////////////////////////////////////
    private ArrayList<String> mItems;
    private RecyclerView mRecentRecyclerView;
    private LinearLayoutManager mRecentLayoutManager;
    private RecyclerView.Adapter<CustomViewHolder> mAdapter;
    public static Date dateFrom = new Date(LocalDate.now().getYear(), LocalDate.now().getMonthValue() - 1, 1);
    public static Date dateTo = new Date(LocalDate.now().getYear(), LocalDate.now().getMonthValue() - 1, 31);
    //////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        authService = new AuthenticationService(this);
        ledgerSyncService = new LedgerSyncService(getApplication());
        activity = this;
        ledgerSyncService.addConsumer(c -> {
            activity.runOnUiThread(() -> {
                refreshTitleData();
                setActionBarLayout();
                if (getCurrentFragment() instanceof LedgeFragment) {
                    setCurrentFragment(LedgeFragment.newInstance());
                } else if (getCurrentFragment() instanceof ReportFragment) {
                    setCurrentFragment(ReportFragment.newInstance());
                }
                Toast.makeText(activity, "Finish sync with server!", Toast.LENGTH_SHORT).show();
            });
        });
        transactionService = new TransactionService(getApplication());
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        if (!authService.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        //update value hold name and amount
        refreshTitleData();
        //set date picker
        setActionBarLayout();
        //set float action button
        initFloatActionButton();
        //set botttom navigation bar activities
        setFragmentBottomNavigationBarActivities();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (GET_NOTIFICATION) {
            if (authService.isLoggedIn() && notificationService == null) {
                System.out.println("INIT NOTIFICATION SERVICE-------");
                NotificationService service = new NotificationService(getApplication());
                service.addNewNotificationConsumer(c -> {
                    activity.runOnUiThread(() -> {
                        Toast.makeText(activity, "You has new notification!", Toast.LENGTH_SHORT).show();
                    });
                });
                notificationService = new Thread(service);
                notificationService.start();
            }
        }
        setActionBarLayout();
    }

    public void restartApp() {
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                PendingIntent.getActivity(this.getBaseContext(),
                        0, new Intent(getIntent()), getIntent().getFlags()));
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void imageClick(View view) {
        Intent intent = new Intent(MainActivity.this, LedgeChoosenActivity.class);
        startActivity(intent);
    }

    private void initFloatActionButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddTransaction.class);
                removeData();
                startActivityForResult(intent, TRANSACTION_ACTION);
            }
        });
    }

    public void removeData() {
        SharedPreferences pre = getSharedPreferences("transaction_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.remove("Note");
        editor.remove("Date");
        editor.remove("balance");
        editor.remove("catId");
        editor.remove("walletId");
        editor.commit();
    }


    private void testDatePicker(final View mView, final AlertDialog dialog) {
        final Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 10);

        final Calendar lastYear = Calendar.getInstance();
        lastYear.add(Calendar.YEAR, -10);

        calendar = (CalendarPickerView) mView.findViewById(R.id.calendar_view);
        ArrayList<Integer> list = new ArrayList<>();
        list.add(0);

        calendar.deactivateDates(list);
        //this array use for high line important date
        ArrayList<Date> arrayList = new ArrayList<>();
        final SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy");
        calendar.init(lastYear.getTime(), nextYear.getTime(), new SimpleDateFormat("MM, YYYY", Locale.getDefault())) //
                .inMode(CalendarPickerView.SelectionMode.RANGE) //
                .withSelectedDate(new Date())
                .withDeactivateDates(list)
                .withHighlightedDates(arrayList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem btnViewNotification = menu.findItem(R.id.btnViewNotification);
        btnViewNotification.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnSynchonize:
                if (((App) getApplication()).isNetworkConnected()) {
                    Toast.makeText(activity, "Start to sync!", Toast.LENGTH_SHORT).show();
                    new Thread(ledgerSyncService).start();
                } else {
                    Toast.makeText(this, "Network is not available to sync!", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setFragmentBottomNavigationBarActivities() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavigationView.setBackgroundColor(Color.parseColor("#ffffff"));
        bottomNavigationView.setItemTextColor(ColorStateList.valueOf(Color.parseColor("#7f7f7f")));
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.action_item1:
                        selectedFragment = LedgeFragment.newInstance();
                        setCurrentFragment(selectedFragment);
                        break;
                    case R.id.action_item2:
                        selectedFragment = ReportFragment.newInstance();
                        setCurrentFragment(selectedFragment);
                        break;
                    case R.id.action_item4:
                        selectedFragment = SettingFragment.newInstance();
                        setCurrentFragment(selectedFragment);
                        break;
                }
//                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                transaction.replace(R.id.frame_layout, selectedFragment);
//                transaction.addToBackStack(null);
//                transaction.commit();
                return true;
            }
        });
        currentFragment = LedgeFragment.newInstance();
        //Manually displaying the first fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, currentFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    public void setActionBarLayout() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        mCustomView = mInflater.inflate(R.layout.activity_main_menu_layout, null);
        TextView mTitleTextView = (TextView) mCustomView.findViewById(R.id.title_text);
        TextView mCashTextView = (TextView) mCustomView.findViewById(R.id.txtCash);

        initData();
        initRecyclerView();

        CircleImageView circleImage = (CircleImageView) mCustomView.findViewById(R.id.circleImage);
        if (ledger_id == null) {
            circleImage.setImageResource(R.drawable.global_icon);
        } else {
            circleImage.setImageResource(R.drawable.wallet);
        }
        circleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LedgeChoosenActivity.class);
                intent.putExtra("ledger_id", ledger_id);
                startActivityForResult(intent, 1);
            }
        });
        mTitleTextView.setText(ledgerName);
        mCashTextView.setText(my_money);
        actionBar.setCustomView(mCustomView);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 || requestCode == TRANSACTION_ACTION) {
            if (resultCode == RESET_TITLE) {
                refreshTitleData();
            }
            if (resultCode == RESULT_OK || resultCode == RESET_TITLE || requestCode == TRANSACTION_ACTION) {
                setActionBarLayout();
                refreshFragment();
            }
        }
    }

    public void refreshTitleData() {
        Ledger ledger = ledgerSyncService.findById(ledger_id);
        if (ledger != null) {
            ledgerName = ledger.getName();
        } else {
            ledgerName = "Tổng cộng";
        }
        double total = ledgerSyncService.findSumOfLedger(ledger_id);
        my_money = formatMoney(total) + convertCurrency(Currency.VND.name());
    }

    public void refreshFragment() {
        if (getCurrentFragment() instanceof LedgeFragment) {
            setCurrentFragment(LedgeFragment.newInstance());
        } else if (getCurrentFragment() instanceof ReportFragment) {
            setCurrentFragment(ReportFragment.newInstance());
        }
    }

    public String formatMoney(double amount) {
        return (amount < 0 ? "-" : "") + ConvertUtil.convertCashFormat(Math.abs(amount));
    }

    private Long atStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private Long atEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    public Fragment getCurrentFragment() {
        return currentFragment;
    }

    public void setCurrentFragment(Fragment currentFragment) {
        this.currentFragment = currentFragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, currentFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
///RecyclerView /////////////////////////////////////////////////////////////////////////////////////////////


    public Date getDateFrom() {

        return dateFrom;
    }

    private void initData() {
        mItems = new ArrayList<String>();
        mItems.add("THIS YEAR");
        mItems.add("LAST 3 MONTHS");
        mItems.add("LAST MONTH");
        mItems.add("THIS MONTH");
        mItems.add("FUTURE");
    }

    private void initRecyclerView() {
        mRecentRecyclerView = (RecyclerView) mCustomView.findViewById(R.id.recentrecyclerView);
        mRecentRecyclerView.setHasFixedSize(true);
        mRecentLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecentRecyclerView.setLayoutManager(mRecentLayoutManager);
        mAdapter = new RecyclerView.Adapter<CustomViewHolder>() {
            @Override
            public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.wrap_recycler_item
                        , viewGroup, false);
                return new CustomViewHolder(view);
            }

            @Override
            public void onBindViewHolder(CustomViewHolder viewHolder, int i) {
                viewHolder.noticeSubject.setText(mItems.get(i));
                if (mItems.get(i).toString().equals("THIS MONTH")) {
                    viewHolder.noticeSubject.setText(mItems.get(i));
                    viewHolder.noticeSubject.setTypeface(null, Typeface.BOLD);
                    viewHolder.noticeSubject.setTextColor(Color.parseColor("#000000"));
                }
            }

            @Override
            public int getItemCount() {
                return mItems.size();
            }

        };
        mRecentRecyclerView.setAdapter(mAdapter);
        mRecentRecyclerView.scrollToPosition(4);
        mRecentRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, mRecentRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        resetRecycler(1);
                        ((TextView) view.findViewById(R.id.recyclerItem)).setTypeface(null, Typeface.BOLD);
                        ((TextView) view.findViewById(R.id.recyclerItem)).setTextColor(Color.parseColor("#000000"));
                        String tabString = ((TextView) view.findViewById(R.id.recyclerItem)).getText().toString();
                        setDate(tabString);
                        mRecentRecyclerView.scrollToPosition(position);
                        //reset data in fragment
                        refreshFragment();
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );

        // ((TextView)mRecentLayoutManager.findViewByPosition(0).findViewById(R.id.recyclerItem)).setTypeface(null, Typeface.BOLD);
    }

    public void setDate(String nameTab) {
        if (nameTab.equals("FUTURE")) {
            dateFrom = new Date(LocalDate.now().getYear(), LocalDate.now().getMonthValue() - 1, LocalDate.now().getDayOfMonth() + 1);
            dateTo = new Date(LocalDate.now().getYear() + 100, 2, 1);
        }
        if (nameTab.equals("THIS MONTH")) {
            dateFrom = new Date(LocalDate.now().getYear(), LocalDate.now().getMonthValue() - 1, 1);
            dateTo = new Date(LocalDate.now().getYear(), LocalDate.now().getMonthValue() - 1, 31);
        }
        if (nameTab.equals("LAST MONTH")) {
            dateFrom = new Date(LocalDate.now().getYear(), LocalDate.now().getMonthValue() - 1 - 1, 1);
            dateTo = new Date(LocalDate.now().getYear(), LocalDate.now().getMonthValue() - 1 - 1, 31);
        }
        if (nameTab.equals("LAST 3 MONTHS")) {
            dateFrom = new Date(LocalDate.now().getYear(), LocalDate.now().getMonthValue() - 1 - 2, 1);
            dateTo = new Date(LocalDate.now().getYear(), LocalDate.now().getMonthValue() - 1, 31);

        }
        if (nameTab.equals("THIS YEAR")) {
            dateFrom = new Date(LocalDate.now().getYear(), 1 - 1, 1);
            dateTo = new Date(LocalDate.now().getYear(), 12 - 1, 31);
            Log.i("fro1m", dateTo.toString());
        }
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        private TextView noticeSubject;

        public CustomViewHolder(View itemView) {
            super(itemView);

            noticeSubject = (TextView) itemView.findViewById(R.id.recyclerItem);
        }
    }

    private void resetRecycler(int tab) {
        for (int i = 0; i < 6; i++)
            if (mRecentLayoutManager.findViewByPosition(i) != null) {
                ((TextView) mRecentLayoutManager.findViewByPosition(i).findViewById(R.id.recyclerItem)).setTypeface(null, Typeface.NORMAL);
                ((TextView) mRecentLayoutManager.findViewByPosition(i).findViewById(R.id.recyclerItem)).setTextColor(Color.parseColor("#FF989292"));
            }
    }
}
