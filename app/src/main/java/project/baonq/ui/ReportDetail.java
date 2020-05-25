package project.baonq.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.baonq.menu.R;
import project.baonq.util.ConvertUtil;

public class ReportDetail extends AppCompatActivity {
    HashMap<String, Double> hm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.NormalSizeAppTheme);
        setContentView(R.layout.activity_report_detail);

        Intent intent = getIntent();
        int transactionGroup = intent.getIntExtra("transactionGroup", -1);
        initActionBar(transactionGroup);
        if (transactionGroup == 1) {
            hm = ReportFragment.hmIncome;
        } else {
            hm = ReportFragment.hmExpand;
        }


        initChart(hm);

        for (String key : hm.keySet()) {
            addNewRowData(key, ConvertUtil.convertCashFormat(hm.get(key)) + "đ", transactionGroup);
        }
    }

    private void addNewRowData(String key, String value, int transactionGroup) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View tmpLayout = layoutInflater.inflate(R.layout.fragment_report_detail_sub_layout, null);
        TextView txtGroupName = tmpLayout.findViewById(R.id.txtGroupName);
        TextView txtCash = tmpLayout.findViewById(R.id.txtCash);
        ImageView imageView = (ImageView) tmpLayout.findViewById(R.id.imageView);
        imageView.setImageResource(ConvertUtil.mapIcon(key));
        txtGroupName.setText(key);
        txtCash.setText(value);
        if (transactionGroup == 1) {
            txtCash.setTextColor(getColor(R.color.colorGreen));
        } else {
            txtCash.setTextColor(getColor(R.color.color_red));
        }
        LinearLayout mainLayout = findViewById(R.id.containAllReport);
        mainLayout.addView(tmpLayout);

    }

    private void initActionBar(int transactionGroup) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View customView = layoutInflater.inflate(R.layout.ledge_choosen_sub_layout, null);
        TextView textView = customView.findViewById(R.id.ledgeTittle);
        TextView close = customView.findViewById(R.id.closeLedge);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (transactionGroup == 1) {
            textView.setText("Thu nhập");
        } else {
            textView.setText("Chi tiêu");
        }

        actionBar.setCustomView(customView);
    }

    private void initChart(HashMap<String, Double> hm) {
        PieChart pieChart = (PieChart) findViewById(R.id.inComePieChart);
        setUpPieChart(hm, pieChart);
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
        chart.setDrawSliceText(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.setData(pieData);
        chart.invalidate();
    }

}
