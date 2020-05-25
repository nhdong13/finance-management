package project.baonq.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import project.baonq.enumeration.Currency;
import project.baonq.menu.R;
import project.baonq.model.Ledger;
import project.baonq.model.Transaction;
import project.baonq.service.LedgerService;
import project.baonq.service.TransactionGroupService;
import project.baonq.service.TransactionService;

import static project.baonq.ui.MainActivity.RESET_TITLE;
import static project.baonq.util.ConvertUtil.convertCurrency;
import static project.baonq.util.ConvertUtil.formatMoney;

public class LedgeChoosenActivity extends AppCompatActivity {
    private final static int LAYOUT_INFO = 1;
    private final static int LAYOUT_UPDATE = 2;
    TransactionGroupService transactionGroupService;
    LedgerService ledgerService;
    TransactionService transactionService;
    private View cardSumLayout;
    String totalCashGlobal;
    List<View> layoutList;
    private TextView txtClose;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.NormalSizeAppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ledge_choosen_layout);
        cardSumLayout = findViewById(R.id.cardSumLayout);
        transactionGroupService = new TransactionGroupService(getApplication());
        ledgerService = new LedgerService(getApplication());
        transactionService = new TransactionService(getApplication());
        layoutList = new LinkedList<>();
        //set init for action bar
        initActionBar();
        txtClose = (TextView) findViewById(R.id.closeLedge);
        //set init for menu action
        initMenuAction();
        //set init layout element
        initLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //checkRow(getCurrentLedgerId());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LAYOUT_INFO || requestCode == LAYOUT_UPDATE) {
            if (resultCode == RESET_TITLE) {
                layoutList.forEach(view -> {
                    ((ViewGroup) view.getParent()).removeView(view);
                });
                layoutList = new LinkedList<>();
                initLayout();
            }
            txtClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setResult(RESET_TITLE);
                    finish();
                }
            });
        }
    }

    private void initLayout() {
        initElement();
        initAddLedgeText();
        loadDataFromSessionDao();
    }

    private void initElement() {
        CardView cardView = findViewById(R.id.cardCashSum);
        ImageView imageView = findViewById(R.id.imageCheck);
        if (MainActivity.ledger_id == null) {
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.INVISIBLE);
        }
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LedgeChoosenActivity.this, MainActivity.class);
                MainActivity.ledger_id = null;
                MainActivity.my_money = totalCashGlobal;
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void initAddLedgeText() {
        TextView txtAddLedge = (TextView) findViewById(R.id.txtAddLedge);
        txtAddLedge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LedgeChoosenActivity.this, AddLedgeActivity.class);
                startActivityForResult(intent, LAYOUT_INFO);
            }
        });
    }

    public void loadDataFromSessionDao() {
        List<Ledger> ledgerList = ledgerService.getAll();
        double totalLedgerCash = 0;
        String currency = "";
        //in case this is not have any ledger
        if (ledgerList != null && !ledgerList.isEmpty()) {
            currency = convertCurrency(ledgerList.get(0).getCurrency());
        }
        for (Ledger ledger : ledgerList) {
            List<Transaction> transactionList = transactionService.getByLedgerId(ledger.getId());
            double transactionSum = 0;
            if (transactionList != null) {
                transactionSum = LedgerService.sumOfTransaction(transactionList);
            }
            createNewRowData(ledger, transactionSum);
            totalLedgerCash += transactionSum;
        }
        double finalTmp = totalLedgerCash;
        cardSumLayout.setOnClickListener(v -> {
            MainActivity.ledger_id = null;
            MainActivity.ledgerName = "Tổng cộng";
            MainActivity.my_money = formatMoney(finalTmp) + convertCurrency(Currency.VND.name());
            setResult(RESULT_OK);
            finish();
        });
        setTotalLedgerCash(totalLedgerCash, currency);
    }

    private void createNewRowData(final Ledger ledger, double sum) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View mainView = layoutInflater.inflate(R.layout.activity_main, null);
        View submitLayout = getLayoutInflater().inflate(R.layout.add_ledge_submit_layout, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 2, 0, 0);
        submitLayout.setLayoutParams(layoutParams);
        ImageView imageCheck = (ImageView) submitLayout.findViewById(R.id.imageCheck);
        if (MainActivity.ledger_id != null && MainActivity.ledger_id.compareTo(ledger.getId()) == 0) {
            imageCheck.setVisibility(View.VISIBLE);
        }

        TextView txtTitle = submitLayout.findViewById(R.id.txtTittle);
        TextView txtCash = submitLayout.findViewById(R.id.txtCash);
        if (sum < 0) {
            txtCash.setTextColor(Color.RED);
        }
        txtTitle.setText(ledger.getName());
        String currentBalanceFormat = formatMoney(sum);
        String totalCash = currentBalanceFormat + convertCurrency(ledger.getCurrency());
        txtCash.setText(totalCash);
        submitLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.ledger_id = ledger.getId();
                MainActivity.ledgerName = ledger.getName();
                MainActivity.my_money = totalCash;
                setResult(RESULT_OK);
                finish();
            }
        });

        //create image button
        createImageButton(ledger, sum, submitLayout);
        LinearLayout contentLedgeChosenLayout = (LinearLayout) findViewById(R.id.contentLedgerChosen);
        contentLedgeChosenLayout.addView(submitLayout);
        layoutList.add(submitLayout);
    }


    private void setTotalLedgerCash(double totalLedgerCash, String currency) {
        TextView txtLedgerCashSum = (TextView) findViewById(R.id.txtLedgerCashSum);
        if (totalLedgerCash < 0) {
            txtLedgerCashSum.setTextColor(Color.RED);
        }
        String totalCash = formatMoney(totalLedgerCash);
        txtLedgerCashSum.setText(totalCash + currency);
        totalCashGlobal = totalCash + currency;
        txtLedgerCashSum.setText(totalCashGlobal);
    }


    private void createImageButton(final Ledger ledger, final double sum, View submitLayout) {
        ImageButton imageButton = new ImageButton(this);
        imageButton.setImageResource(R.drawable.ic_edit_black_24dp);
        LinearLayout.LayoutParams imageButtonParam = new LinearLayout.LayoutParams(0, 45);
        imageButtonParam.weight = 0.1f;
        imageButtonParam.setMargins(0, 15, 0, 0);
        imageButton.setLayoutParams(imageButtonParam);
        imageButton.setBackground(getResources().getDrawable(R.color.colorWhite));
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LedgeChoosenActivity.this, AddLedgeActivity.class);
                intent.putExtra("id", ledger.getId());
                intent.putExtra("name", ledger.getName());
                intent.putExtra("currency", ledger.getCurrency());
                intent.putExtra("currentBalance", sum);
                startActivityForResult(intent, LAYOUT_UPDATE);
            }
        });
        LinearLayout submitLayOutLinear = submitLayout.findViewById(R.id.container);
        submitLayOutLinear.addView(imageButton);
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
        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
