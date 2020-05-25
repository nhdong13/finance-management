package project.baonq.AddTransaction;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import project.baonq.dao.TransactionDAO;
import project.baonq.model.Ledger;
import project.baonq.model.TransactionGroup;
import project.baonq.service.App;
import project.baonq.menu.R;
import project.baonq.model.DaoSession;
import project.baonq.model.Transaction;
import project.baonq.model.TransactionDao;
import project.baonq.service.LedgerService;
import project.baonq.service.TransactionGroupService;
import project.baonq.service.TransactionService;
import project.baonq.ui.MainActivity;
import project.baonq.util.ConvertUtil;

import static android.widget.Toast.LENGTH_SHORT;
import static project.baonq.ui.MainActivity.RESET_TITLE;
import static project.baonq.ui.MainActivity.ledger_id;

public class AddTransaction extends AppCompatActivity {

    final Calendar myCalendar = Calendar.getInstance();
    private TransactionService transactionService;
    private LedgerService ledgerService;
    private TransactionGroupService groupService;
    private Long ledgerId;
    private Long groupId;
    private DatePickerDialog.OnDateSetListener date;
    private Ledger ledger;
    private TransactionGroup group;
    private Long isUpdate = 0L;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        setTheme(R.style.NormalSizeAppTheme);
        super.onCreate(savedInstanceState);
        Intent intent = this.getIntent();
        isUpdate = intent.getLongExtra("transactionID", 0L);

        setContentView(R.layout.activity_add_transaction);
        initActionBar();
        initMenuActivities();
        transactionService = new TransactionService(getApplication());
        ledgerService = new LedgerService(getApplication());
        groupService = new TransactionGroupService(getApplication());
        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        EditText txt = findViewById(R.id.txtDate);
        txt.setFocusable(false);
        txt.setBackgroundResource(android.R.color.transparent);
        ((EditText) findViewById(R.id.nmAmount)).setBackgroundResource(android.R.color.transparent);
        ((EditText) findViewById(R.id.txtNote)).setBackgroundResource(android.R.color.transparent);
        removeData();
        if (isUpdate != 0L) {
            initEditLayout();
            Button button = findViewById(R.id.btnDeleteTransaction);
            button.setVisibility(View.VISIBLE);
        }
    }

    private void initEditLayout() {
        Transaction e = new TransactionDAO(this.getApplication()).findById(isUpdate);
        ledgerId = (e.getLedger_id());
        groupId = (e.getGroup_id());
        SharedPreferences pre = getSharedPreferences("transaction_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putString("walletId", ledgerId.toString());
        editor.putString("catId", groupId.toString());
        editor.commit();
        ((EditText) findViewById(R.id.nmAmount)).setText(String.valueOf(e.getBalance()));
        ((EditText) findViewById(R.id.txtNote)).setText(String.valueOf(e.getNote()));
        ((EditText) findViewById(R.id.txtDate)).setText(String.valueOf(e.getTdate()));
    }

    public Long getLedgerId() {
        SharedPreferences pre = getSharedPreferences("transaction_data", MODE_PRIVATE);
        String walletIdString = pre.getString("walletId", "");
        if (!walletIdString.equals("")) {
            ledgerId = Long.parseLong(walletIdString);
            ledger = ledgerService.findById(ledgerId);
        } else {
            ledgerId = null;
            ledger = null;
        }
        return ledgerId;
    }

    public Long getGroupId() {
        SharedPreferences pre = getSharedPreferences("transaction_data", MODE_PRIVATE);
        String groupIdString = pre.getString("catId", "");

        if (!groupIdString.equals("")) {
            groupId = Long.parseLong(groupIdString);
            group = groupService.findById(groupId);
        } else {
            groupId = null;
            group = null;
        }
        return groupId;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getLedgerId() != null) {
            ((Button) findViewById(R.id.btnWallet)).setText(ledger.getName());
        } else {
            ((Button) findViewById(R.id.btnWallet)).setText("Chọn ví");
        }
        if (getGroupId() != null) {
            ((Button) findViewById(R.id.btnCategory)).setText(group.getName());
        } else {
            ((Button) findViewById(R.id.btnCategory)).setText("Chọn nhóm");
        }
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
                removeData();
                finish();
            }
        });
        TextView txtTitle = (TextView) findViewById(R.id.txtTittle);
        txtTitle.setText("");
        TextView txtSave = (TextView) findViewById(R.id.btnCheckAllNotification);
        txtSave.setText("Lưu");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 0);
        txtSave.setLayoutParams(params);
        txtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUpdate == 0L) {
                    clickToSave();
                } else {
                    clickToUpdate();
                }
            }
        });
    }

    private void clickToUpdate() {
        EditText txtAmount = (EditText) findViewById(R.id.nmAmount);
        String txtNote = ((EditText) findViewById(R.id.txtNote))
                .getText().toString();
        String date = ((EditText) findViewById(R.id.txtDate)).getText().toString();

        if (txtAmount.getText().toString().isEmpty() || date.isEmpty() ||
                getLedgerId() == null || getGroupId() == null) {
            new AlertDialog.Builder(AddTransaction.this)
                    .setTitle("Oops")
                    .setMessage("Xin điền vào tất cả các trường!")
                    .setNegativeButton("OK", null)
                    .show();
        } else {
            double amount = Double.parseDouble(txtAmount.getText().toString());
            if (amount <= 0) {
                new AlertDialog.Builder(AddTransaction.this)
                        .setTitle("Oops")
                        .setMessage("Số tiền luôn là dương!")
                        .setNegativeButton("OK", null)
                        .show();
            } else {
                Transaction e = new Transaction();
                e.setId(isUpdate);
                e.setGroup_id(getGroupId());
                e.setLedger_id(getLedgerId());
                e.setBalance(amount);
                e.setTdate(date);
                e.setNote(txtNote);
                transactionService.updateTransaction(e);
                removeData();
                setResult(RESET_TITLE);
                finish();
            }
        }
    }

    private void clickToSave() {
        EditText txtAmount = (EditText) findViewById(R.id.nmAmount);
        String txtNote = ((EditText) findViewById(R.id.txtNote))
                .getText().toString();
        String date = ((EditText) findViewById(R.id.txtDate)).getText().toString();

        if (txtAmount.getText().toString().isEmpty() || date.isEmpty() ||
                getLedgerId() == null || getGroupId() == null) {
            new AlertDialog.Builder(AddTransaction.this)
                    .setTitle("Oops")
                    .setMessage("Xin điền vào tất cả các trường!")
                    .setNegativeButton("OK", null)
                    .show();
        } else {
            double amount = Double.parseDouble(txtAmount.getText().toString());
            if (amount <= 0) {
                new AlertDialog.Builder(AddTransaction.this)
                        .setTitle("Oops")
                        .setMessage("Số tiền luôn là dương!")
                        .setNegativeButton("OK", null)
                        .show();
            } else {
                transactionService
                        .addTransaction(getLedgerId(), getGroupId(), amount, date, txtNote);
                removeData();
                setResult(RESET_TITLE);
                finish();
            }
        }
    }

    public void clickToChooseWallet(View view) {
        removeData();
        Intent intent = new Intent(this, ChooseLedger.class);
        startActivity(intent);
    }

    public void clickToChooseCatogory(View view) {
        if (getLedgerId() != null) {
            //remove category id before choose
            SharedPreferences pre = getSharedPreferences("transaction_data", MODE_PRIVATE);
            SharedPreferences.Editor editor = pre.edit();
            editor.remove("catId");
            editor.commit();
            Intent intent = new Intent(this, SelectCategory.class);
            intent.putExtra("ledgerId", ledgerId);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Xin chọn ví trước", Toast.LENGTH_SHORT).show();
        }
    }

    public void clickToPickDate(View view) {
        new DatePickerDialog(this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        EditText txt = findViewById(R.id.txtDate);
        txt.setText(sdf.format(myCalendar.getTime()));
    }

    public void removeData() {
        SharedPreferences pre = getSharedPreferences("transaction_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.remove("catId");
        editor.remove("walletId");
        editor.commit();
    }

    public void clickToDeleteTransaction(View view) {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Activity")
                .setMessage("Are you sure to delte this transaction?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isUpdate != null && isUpdate != 0L) {
                            transactionService.deleteTransaction(isUpdate);
                        }
                        setResult(RESET_TITLE);
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
