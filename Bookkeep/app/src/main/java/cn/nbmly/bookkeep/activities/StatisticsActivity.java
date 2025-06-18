package cn.nbmly.bookkeep.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.nbmly.bookkeep.R;
import cn.nbmly.bookkeep.db.BillDao;
import cn.nbmly.bookkeep.models.Bill;

public class StatisticsActivity extends AppCompatActivity {

    private PieChart pieChart;
    private BarChart barChart;
    private Spinner spinnerStatisticsType;
    private BillDao billDao;
    private int loggedInUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        pieChart = findViewById(R.id.pie_chart);
        barChart = findViewById(R.id.bar_chart);
        spinnerStatisticsType = findViewById(R.id.spinner_statistics_type);

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        loggedInUserId = sharedPref.getInt("loggedInUserId", -1);

        billDao = new BillDao(this);
        billDao.open();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.statistics_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatisticsType.setAdapter(adapter);

        spinnerStatisticsType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // 按类别统计
                    displayCategoryStatistics();
                } else { // 按月份统计
                    displayMonthlyStatistics();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Initial display
        displayCategoryStatistics();
    }

    private void displayCategoryStatistics() {
        pieChart.setVisibility(View.VISIBLE);
        barChart.setVisibility(View.GONE);

        List<Bill> bills = billDao.getAllBillsByUserId(loggedInUserId);
        Map<String, Float> categoryExpenses = new HashMap<>();

        for (Bill bill : bills) {
            if (bill.getType().equals("支出")) {
                String category = bill.getCategory();
                float amount = (float) bill.getAmount();
                categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0f) + amount);
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryExpenses.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "按类别支出");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.getDescription().setEnabled(false);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void displayMonthlyStatistics() {
        pieChart.setVisibility(View.GONE);
        barChart.setVisibility(View.VISIBLE);

        List<Bill> bills = billDao.getAllBillsByUserId(loggedInUserId);
        Map<String, Float> monthlyExpenses = new HashMap<>();

        for (Bill bill : bills) {
            if (bill.getType().equals("支出")) {
                String date = bill.getDate(); // YYYY-MM-DD
                String month = date.substring(0, 7); // YYYY-MM
                float amount = (float) bill.getAmount();
                monthlyExpenses.put(month, monthlyExpenses.getOrDefault(month, 0f) + amount);
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Float> entry : monthlyExpenses.entrySet()) {
            entries.add(new BarEntry(i++, entry.getValue()));
            labels.add(entry.getKey());
        }

        BarDataSet dataSet = new BarDataSet(entries, "按月份支出");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(labels.size());

        barChart.getAxisRight().setEnabled(false);
        barChart.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        billDao.close();
    }
}

