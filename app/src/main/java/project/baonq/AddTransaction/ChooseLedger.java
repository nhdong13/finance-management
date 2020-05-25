package project.baonq.AddTransaction;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import project.baonq.menu.R;
import project.baonq.model.Ledger;
import project.baonq.model.Transaction;
import project.baonq.service.LedgerService;
import project.baonq.ui.AddLedgeActivity;
import project.baonq.util.ConvertUtil;

public class ChooseLedger extends AppCompatActivity {
    private final static int LAYOUT_INFO = 1;
    private final static int LAYOUT_UPDATE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.NormalSizeAppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_ledger);
        //set init for action bar
        initActionBar();
        //set init for menu action
        initMenuAction();
        getLedgerData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LAYOUT_INFO || requestCode == LAYOUT_UPDATE) {
            if (resultCode == RESULT_OK) {
                finish();
                startActivity(getIntent());
            }
        }
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
        TextView txtClose = (TextView) findViewById(R.id.closeLedge);
        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getLedgerData() {
        List<Ledger> ledgerList = new LedgerService(getApplication()).getAll();
        for (Ledger ledger : ledgerList) {
            createNewDataRow(ledger, 0);
        }
    }


    private void createNewDataRow(final Ledger ledger, double sum) {
        View submitLayout = getLayoutInflater().inflate(R.layout.add_ledge_submit_layout, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 2, 0, 0);
        submitLayout.setLayoutParams(layoutParams);
        TextView txtTitle = submitLayout.findViewById(R.id.txtTittle);
        TextView txtCash = submitLayout.findViewById(R.id.txtCash);
        txtTitle.setText(ledger.getName());
        String currentBalanceFormat = ConvertUtil.convertCashFormat(sum);
        txtCash.setText(currentBalanceFormat + ConvertUtil.convertCurrency(ledger.getCurrency()));
        submitLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pre = getSharedPreferences("transaction_data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pre.edit();
                editor.putString("walletId", ledger.getId() + "");
                editor.commit();
                finish();
            }
        });

        LinearLayout contentLedgeChosenLayout = (LinearLayout) findViewById(R.id.abcd);
        contentLedgeChosenLayout.addView(submitLayout);
    }
}
