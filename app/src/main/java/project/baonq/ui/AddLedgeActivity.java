package project.baonq.ui;

import android.app.Application;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import project.baonq.enumeration.Currency;
import project.baonq.enumeration.TransactionGroupStatus;
import project.baonq.enumeration.TransactionGroupType;
import project.baonq.menu.R;
import project.baonq.model.DaoSession;
import project.baonq.model.Ledger;
import project.baonq.service.App;
import project.baonq.service.LedgerService;
import project.baonq.service.TransactionGroupService;
import project.baonq.service.TransactionService;
import project.baonq.util.ConvertUtil;

import static project.baonq.ui.MainActivity.RESET_TITLE;

public class AddLedgeActivity extends AppCompatActivity {

    private boolean isUpdate = false;
    private Long id = null;
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    TransactionGroupService transactionGroupService;
    TransactionService transactionService;
    LedgerService ledgerService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.NormalSizeAppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_ledge_layout);
        transactionGroupService = new TransactionGroupService(getApplication());
        transactionService = new TransactionService(getApplication());
        ledgerService = new LedgerService(getApplication());
        //init action bar
        initActionBar();
        //init spiner
        initSpiner();
        //load form for update
        loadUpdateForm();
        //init menu element
        initMenuElement();
        //init form element
        initFormElement();
    }

    private void initBodyElement(String name, String currency, double currentBalance) {
        TextView txtCash = (TextView) findViewById(R.id.txtCash);
        Spinner spCurrency = (Spinner) findViewById(R.id.spinerCurrency);
        TextView txtCurrentBalance = (TextView) findViewById(R.id.txtCurrentBalance);
        txtCash.setText(name);
        txtCurrentBalance.setText(ConvertUtil.convertCashFormat(currentBalance).replaceAll(",", ""));
        spCurrency.setSelection(getIndexOfSpinner(spCurrency, currency));
    }

    private void initMenuElement() {
        initSubmitText();
        initCancelText();
    }

    private void initCancelText() {
        TextView txtCancel = (TextView) findViewById(R.id.closeAddLedge);
        txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initFormElement() {
        final TextView txtCurentBalance = findViewById(R.id.txtCurrentBalance);
        if(!isUpdate){
            txtCurentBalance.setText("0");
        }
    }

    private void loadUpdateForm() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        // in case this is not update request
        if (bundle != null) {
            isUpdate = true;
            id = intent.getLongExtra("id", -1);
            String currency = intent.getStringExtra("currency");
            double currentBalance = intent.getDoubleExtra("currentBalance", -1);
            String name = intent.getStringExtra("name");
            //init body element
            initBodyElement(name, currency, currentBalance);
        }
    }

    private Long addLedger(String name, String currency, boolean isChecked) {
        return ledgerService.addLedger(name, currency, isChecked);
    }

    private void addDefaultTransactionGroup(Long ledgerId) {
        String[] defaultIncomeName = {"Lương", "Tiền chuyển đến", "Được tặng", "Lãi ngân hàng", "Khác"};
        for (String name : defaultIncomeName) {
            transactionGroupService.addTransactionGroup(ledgerId, name, TransactionGroupType.INCOME.getType());
        }
        String[] defaultPurchaseName = {"Ăn uống", "Hóa đơn", "Mua sắm", "Di chuyển", "Khác"};
        for (String name : defaultPurchaseName) {
            transactionGroupService.addTransactionGroup(ledgerId, name, TransactionGroupType.EXPENSE.getType());
        }
    }


    private void updateLedger(Long id, String name, String currency, boolean isChecked) {
        boolean isChanged = false;
        name = name != null ? name.trim() : null;
        Ledger ledger = ledgerService.findById(id);
        if (!ledger.getName().equals(name)
                || !ledger.getCurrency().equals(currency) || ledger.getCounted_on_report() && isChecked) {
            System.out.println("UPDATE LEDGER WITH ID:" + id);
            Ledger ledger1 = new Ledger();
            ledger1.setId(id);
            ledger1.setName(name);
            ledger1.setCurrency(currency);
            ledger1.setCounted_on_report(isChecked);
            ledgerService.updateLedger(ledger1);
        }
    }

    private void initSubmitText() {
        TextView txtTitle = (TextView) findViewById(R.id.ledgeTittle);
        TextView txtSubmit = (TextView) findViewById(R.id.submitAddLedge);
        final EditText edtName = (EditText) findViewById(R.id.txtCash);
        final EditText edtCurrentBalance = (EditText) findViewById(R.id.txtCurrentBalance);
        final Spinner spCurrency = (Spinner) findViewById(R.id.spinerCurrency);
        final CheckBox cbReport = (CheckBox) findViewById(R.id.cb_report);
        final Intent intent = getIntent();

        if (!isUpdate) {
            txtTitle.setText("Thêm ví");
            txtSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isValidate;
                    String name = edtName.getText().toString();
                    isValidate = validate(name, "Tên ví không được để trống");
                    //in case name is empty
                    if (!isValidate) {
                        edtName.requestFocus();
                    }
                    String currentBalance = edtCurrentBalance.getText().toString();
                    Long groupId = 0L;
                    double balance = Double.parseDouble(!currentBalance.isEmpty() ? currentBalance : "0");
                    String currency = spCurrency.getSelectedItem().toString();
                    boolean isChecked = cbReport.isChecked();
                    if (isValidate) {
                        Long ledgerId = addLedger(name, currency, isChecked);
                        //add default transaction group
                        addDefaultTransactionGroup(ledgerId);
                        //in case current is > 0
                        String tdate = sdf.format(new Date());
                        String note = "Điều chỉnh số dư";
                        if (balance > 0) {
                            groupId = transactionGroupService.getTransactionGroupID(ledgerId, TransactionGroupType.INCOME.getType(), "Khác");
                            transactionService.addTransaction(ledgerId, groupId, balance, tdate, note);
                        } else if (balance < 0) {
                            groupId = transactionGroupService.getTransactionGroupID(ledgerId, TransactionGroupType.EXPENSE.getType(), "Khác");
                            balance = Math.abs(balance);
                            transactionService.addTransaction(ledgerId, groupId, balance, tdate, note);
                        }
                        setResult(RESET_TITLE, intent);
                        finish();
                    }
                }
            });
        } else {
            txtTitle.setText("Cập nhật ví");
            edtCurrentBalance.setEnabled(false);
            txtSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = edtName.getText().toString();
                    String currentBalance = edtCurrentBalance.getText().toString();
                    currentBalance = currentBalance.replaceAll(",", "");
                    String currency = spCurrency.getSelectedItem().toString();
                    boolean isChecked = cbReport.isChecked();
                    updateLedger(id, name, currency, isChecked);
//                    updateTransaction(id, 1, "Khác", Double.parseDouble(currentBalance));
                    setResult(RESET_TITLE, intent);
                    finish();
                }
            });
        }
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View mCustomView = layoutInflater.inflate(R.layout.add_ledge_sub_layout, null);
        actionBar.setCustomView(mCustomView);
    }

    private void initSpiner() {
        Spinner spinner = (Spinner) findViewById(R.id.spinerCurrency);
        String[] items = new String[Currency.values().length];
        for (int i = 0; i < Currency.values().length; i++) {
            items[i] = Currency.values()[i].name();
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(AddLedgeActivity.this, android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(arrayAdapter);
    }

    //get index of spinner
    private int getIndexOfSpinner(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    private boolean validate(String value, String message) {
        if (value.isEmpty()) {
            Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
