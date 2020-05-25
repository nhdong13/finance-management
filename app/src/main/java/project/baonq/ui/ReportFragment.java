package project.baonq.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import project.baonq.menu.R;
import project.baonq.model.Transaction;
import project.baonq.model.TransactionGroup;
import project.baonq.service.TransactionGroupService;
import project.baonq.service.TransactionService;
import project.baonq.util.ConvertUtil;


public class ReportFragment extends Fragment {
    MainActivity mainActivity;
    List<String> nameList;
    List<Double> valueList;
    HashMap<String, Double> hm;
    public static HashMap<String, Double> hmIncome;
    public static HashMap<String, Double> hmExpand;
    Long startTime;
    Long endTime;
    Long ledger_id;

    public ReportFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ReportFragment newInstance() {
        ReportFragment fragment = new ReportFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initDateFromMain();
        initChart();
        ledger_id = MainActivity.ledger_id;
        if (startTime != null && endTime != null) {
            hmIncome = getDataForPie(ledger_id, 1);
            PieChart chartIncome = (PieChart) getView().findViewById(R.id.inComePieChart);
            TextView txtIncome = (TextView) getView().findViewById(R.id.txtIncomeBalance);
            setUpPieChart(hmIncome, chartIncome);
            setUpTotal(txtIncome);

            hmExpand = getDataForPie(ledger_id, 2);
            PieChart chartExpand = (PieChart) getView().findViewById(R.id.expandPieChart);
            TextView txtExpand = (TextView) getView().findViewById(R.id.txtExpandBalance);
            setUpPieChart(hmExpand, chartExpand);
            setUpTotal(txtExpand);
        }
    }

    private void initChart() {
        PieChart chartIncome = (PieChart) getView().findViewById(R.id.inComePieChart);
        bindPieChartEvent(chartIncome, 1);
        PieChart chartExpand = (PieChart) getView().findViewById(R.id.expandPieChart);
        bindPieChartEvent(chartExpand, 2);
    }

    private void bindPieChartEvent(PieChart pieChart, int transactionGroup) {
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Intent intent = new Intent(getContext(), ReportDetail.class);
                intent.putExtra("transactionGroup", transactionGroup);
                startActivity(intent);
            }

            @Override
            public void onNothingSelected() {
                Intent intent = new Intent(getContext(), ReportDetail.class);
                intent.putExtra("transactionGroup", transactionGroup);
                startActivity(intent);
            }
        });
    }

    private void initDateFromMain() {
        String first1 = String.valueOf(MainActivity.dateFrom.getMonth() + 1) + "/" + String.valueOf(MainActivity.dateFrom.getDate()) + "/" + String.valueOf(MainActivity.dateFrom.getYear());
        String last1 = String.valueOf(MainActivity.dateTo.getMonth() + 1) + "/" + String.valueOf(MainActivity.dateTo.getDate()) + "/" + String.valueOf(MainActivity.dateTo.getYear());
        Date dateFirst = null;
        Date dateLast = null;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {
            dateFirst = sdf.parse(first1);
            dateLast = sdf.parse(last1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String s = dateFirst.toString();
        startTime = dateFirst.getTime();
        endTime = dateLast.getTime();
    }

    private void setUpTotal(TextView textView) {
        if (hm.isEmpty()) {
            textView.setText("0.00đ");
        }
        float total = 0;

        for (String key : hm.keySet()) {
            total += hm.get(key);
        }
        textView.setText(ConvertUtil.convertCashFormat(total) + ConvertUtil.convertCurrency("VNĐ"));
    }

    private void setUpPieChart(HashMap<String, Double> map, PieChart pieChart) {
        List<PieEntry> pieEntries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            pieEntries.add(new PieEntry(Float.parseFloat(entry.getValue().toString()), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, "");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(16);
        PieData pieData = new PieData(dataSet);
        PieChart chart = pieChart;
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.setDrawSliceText(false);
        chart.setData(pieData);
        chart.invalidate();
    }

    private HashMap<String, Double> getDataForPie(Long ledger_id, int typeAspect) {
        hm = new HashMap<>();
        Application application = mainActivity.getApplication();
        List<Transaction> transactionList;
        if (ledger_id != null) {
            transactionList = new TransactionService(application).getActiveByLedgerId(ledger_id);
        } else {
            transactionList = new TransactionService(application).getAllActive();
        }

        for (Transaction item : transactionList) {
            if (compareTransaction(item) != null && !item.getCounted_on_report()) {
                Long groupId = item.getGroup_id();
                TransactionGroup transactionGroup = new TransactionGroupService(application).getTransactionGroupByID(groupId);
                int type = transactionGroup.getTransaction_type();
                if (type == typeAspect) {
                    String key = transactionGroup.getName();
                    if (hm.containsKey(key)) {
                        Double value = hm.get(key);
                        value += item.getBalance();
                        hm.replace(key, value);
                    } else {
                        hm.put(key, item.getBalance());
                    }
                }
            }
        }
        return hm;
    }

    private Transaction compareTransaction(Transaction transaction) {
        if ((startTime > endTime)) {
            endTime = Long.MAX_VALUE;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
        Long dateInMili = 0L;
        try {
            String tmp = transaction.getTdate().toString().replace("//", "/");
            Date date = dateFormat.parse(tmp);
            dateInMili = date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (dateInMili.compareTo(startTime) >= 0 && dateInMili.compareTo(endTime) <= 0) {
            return transaction;
        }
        return null;
    }
}
