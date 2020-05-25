package project.baonq.AddTransaction;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import project.baonq.menu.R;
import project.baonq.model.TransactionGroup;

public class SelectCategory extends AppCompatActivity {

    private Long ledgerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.NormalSizeAppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);
        //set init for action bar
        initActionBar();
        //set init for menu action
        initMenuAction();
        ledgerId = getIntent().getLongExtra("ledgerId", 0);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        // Add Fragments to adapter one by one
        adapter.addFragment(new ExpenseFragment(), "Expense1");
        adapter.addFragment(new IncomeFragment(), "Expense2");
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        SharedPreferences pre = getSharedPreferences("transaction_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.remove("catId");
        editor.commit();
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View customView = layoutInflater.inflate(R.layout.ledge_choosen_sub_layout, null);
        actionBar.setCustomView(customView);
    }

    private void initMenuAction() {
        TextView txtTitle = findViewById(R.id.ledgeTittle);
        txtTitle.setText("Chọn nhóm");
        TextView txtClose = (TextView) findViewById(R.id.closeLedge);
        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public Long getLedgerId() {
        return ledgerId;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add("income");
        }

        @Override
        public CharSequence getPageTitle(int position) {
//            if (position == 0) {
//                return String.valueOf("Expense1");
//            } else  {
//                return String.valueOf("Expense1");
//            }
//            Log.i("title",String.valueOf(position));
//            String txt = .toString();
//            if (txt.equals("Expense1")) return "Expense";
//                return  mFragmentTitleList.get(position);
//            if (position ==0) return "income";
            return "expense";
        }
    }

}
